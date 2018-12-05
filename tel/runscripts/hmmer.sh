SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [ ${CHAR_COUNT} -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      false
fi

if [ ${SEQ_COUNT} = "0" ] && [ ${FORMAT} = "0" ] ; then
      sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
      CHAR_COUNT=$(wc -m < ../params/alignment1)

      if [ ${CHAR_COUNT} -gt "10000" ] ; then
            echo "#Single protein sequence inputs may not contain more than 10000 characters." >> ../results/process.log
            false
      else
            sed -i "1 i\>${JOBID}" ../params/alignment1
            mv ../params/alignment1 ../params/alignment
      fi
fi

if [ ${FORMAT} = "1" ] ; then
      reformatValidator.pl clu fas \
            $(readlink -f %alignment.path) \
            $(readlink -f ../results/${JOBID}.fas) \
            -d 160 -uc -l 32000
else
      reformatValidator.pl fas fas \
            $(readlink -f %alignment.path) \
            $(readlink -f ../results/${JOBID}.fas) \
            -d 160 -uc -l 32000
fi

if [ ! -f ../results/${JOBID}.fas ]; then
    echo "#Input is not in aligned FASTA/CLUSTAL format." >> ../results/process.log
    false
fi

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.fas | wc -l)

if [ ${SEQ_COUNT} -gt "10000" ] ; then
      echo "#Input contains more than 10000 sequences." >> ../results/process.log
      false
fi

if [ ${SEQ_COUNT} -gt "1" ] ; then
       echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
else
       echo "#Query is a single protein sequence." >> ../results/process.log
fi
echo "done" >> ../results/process.log

head -n 2 ../results/${JOBID}.fas > ../results/firstSeq0.fas
sed 's/[\.\-]//g' ../results/firstSeq0.fas > ../results/firstSeq.fas

TMPRED=`tmhmm ../results/firstSeq.fas -short`

run_Coils -c -min_P 0.8 < ../results/firstSeq.fas >& ../results/firstSeq.cc
COILPRED=$(egrep ' 0 in coil' ../results/firstSeq.cc | wc -l)

rm ../results/firstSeq0.fas ../results/firstSeq.cc


fasta2json.py ../results/firstSeq.fas ../results/query.json

if [ "%max_hhblits_iter.content" = "0" ] && [ $SEQ_COUNT -gt "1" ] ; then
    #Use user MSA to build HMM
    echo "#MSA generation not required." >> ../results/process.log
    ${HMMERPATH}/hmmbuild --cpu %THREADS \
             -n "${JOBID}" \
             ../results/${JOBID}.hmm \
             ../results/${JOBID}.fas
else
    echo "#MSA generation required." >> ../results/process.log
    echo "done" >> ../results/process.log
    echo "#Running HHblits for query MSA generation." >> ../results/process.log

    reformat_hhsuite.pl fas a3m \
                        $(readlink -f ../results/${JOBID}.fas) \
                        $(readlink -f ../results/${JOBID}.in.a3m)

    #MSA generation required; generation by HHblits
    hhblits -cpu %THREADS \
            -v 2 \
            -i ../results/${JOBID}.in.a3m \
            -M first \
            -d %UNICLUST  \
            -oa3m ../results/${JOBID}.a3m \
            -n %max_hhblits_iter.content \
            -mact 0.35 \
            -cov 20 \
            -qid 20

    #Convert to fasta format
    reformat_hhsuite.pl a3m fas ../results/${JOBID}.a3m $(readlink -f ../results/${JOBID}.fas)

    $HMMERPATH/hmmbuild --cpu %THREADS \
             -n "${JOBID}" \
             ../results/${JOBID}.hmm \
             ../results/${JOBID}.fas

fi

echo "done" >> ../results/process.log

echo "#Running hmmsearch against the %hmmerdb.content DB." >> ../results/process.log

    ${HMMERPATH}/hmmsearch --cpu %THREADS \
          -E %evalue.content \
          --incE %evalue.content \
          --incdomE 100000000 \
          -o ../results/${JOBID}.outfile \
          -A ../results/${JOBID}.msa_sto \
          ../results/${JOBID}.hmm  %STANDARD/%hmmerdb.content


echo "done" >> ../results/process.log

echo "#Preparing output." >> ../results/process.log

if [ -s "../results/${JOBID}.msa_sto" ]; then

    #Convert to fasta format
    reformat_hhsuite.pl sto a3m ../results/${JOBID}.msa_sto $(readlink -f ../results/${JOBID}.msa_a3m)

    prepareForHMMER.py ../results/${JOBID}.outfile ../results/${JOBID}.outfilefl

    hmmer2json.py -i ../results/${JOBID}.outfilefl \
                  -o ../results/${JOBID}.json \
                  -m %desc.content \
                  -e %evalue.content > ../results/${JOBID}.list

    extractFasta.py ../results/${JOBID}.msa_a3m ../results/${JOBID}.list

    reformat_hhsuite.pl a3m fas ../results/${JOBID}.msa_a3m.subset $(readlink -f ../results/output.aln_fas) -uc -l 32000

    manipulate_json.py -k 'db' -v '%hmmerdb.content' ../results/${JOBID}.json
    #create tab separated file to feed into blastviz
    hmmerJson2tab.py ../results/${JOBID}.json ../results/query.json ../results/${JOBID}.tab
    blastviz_json.pl ../results/${JOBID}.tab %jobid.content ../results/ ../results/ >> ../logs/blastviz.log

    # add transmembrane prediction info to json
    manipulate_json.py -k 'TMPRED' -v "${TMPRED}" ../results/${JOBID}.json

    # add coiled coil prediction info to json
    manipulate_json.py -k 'COILPRED' -v "${COILPRED}" ../results/${JOBID}.json

    # Generate MSA in JSON
    fasta2json.py ../results/output.aln_fas ../results/alignment.json
fi
cd ../results
rm -f *.hmm *.outfile* *.list *.msa_* ${JOBID}.fas firstSeq.fas

echo "done" >> ../results/process.log