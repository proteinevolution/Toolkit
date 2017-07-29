JOBID=%jobid.content
SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)
FORMAT=$(head -1 ../params/alignment | egrep "^CLUSTAL" | wc -l)

if [ ${CHAR_COUNT} -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      updateProcessLog
      false
fi

if [ ${SEQ_COUNT} = "0" ] && [ ${FORMAT} = "0" ] ; then
      sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
      CHAR_COUNT=$(wc -m < ../params/alignment1)

      if [ ${CHAR_COUNT} -gt "10000" ] ; then
            echo "#Single protein sequence inputs may not contain more than 10000 characters." >> ../results/process.log
            updateProcessLog
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
    updateProcessLog
    false
fi

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.fas | wc -l)

if [ ${SEQ_COUNT} -gt "10000" ] ; then
      echo "#Input contains more than 10000 sequences." >> ../results/process.log
      updateProcessLog
      false
fi

if [ ${SEQ_COUNT} -gt "1" ] ; then
       echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
       updateProcessLog
else
       echo "#Query is a single protein sequence." >> ../results/process.log
       updateProcessLog
fi
echo "done" >> ../results/process.log
updateProcessLog


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
    updateProcessLog
    ${HMMERPATH}/hmmbuild --cpu %THREADS \
             -n "${JOBID}" \
             ../results/${JOBID}.hmm \
             ../results/${JOBID}.fas
else
    echo "#MSA generation required." >> ../results/process.log
    updateProcessLog
    echo "done" >> ../results/process.log
    updateProcessLog
    echo "#Running HHblits for query MSA generation." >> ../results/process.log
    updateProcessLog

    # Generate Query in JSON

    #MSA generation required; generation by HHblits
    hhblits -cpu %THREADS \
            -v 2 \
            -i ../results/${JOBID}.fas \
            -M first \
            -d %UNIPROT  \
            -oa3m ../results/${JOBID}.a3m \
            -n %max_hhblits_iter.content \
            -mact 0.35

    #Convert to fasta format
    reformat_hhsuite.pl a3m fas ../results/${JOBID}.a3m $(readlink -f ../results/${JOBID}.fas)

    $HMMERPATH/hmmbuild --cpu %THREADS \
             -n "${JOBID}" \
             ../results/${JOBID}.hmm \
             ../results/${JOBID}.fas

    rm ../results/*.hhr
    rm ../results/*.a3m

fi

echo "done" >> ../results/process.log
updateProcessLog


echo "#Running hmmsearch against the %hmmerdb.content DB." >> ../results/process.log
updateProcessLog

    ${HMMERPATH}/hmmsearch --cpu %THREADS \
          -E %evalue.content \
          --incE %evalue.content \
          -o ../results/${JOBID}.outfile \
          -A ../results/${JOBID}.msa_sto \
          ../results/${JOBID}.hmm  %STANDARD/%hmmerdb.content


echo "done" >> ../results/process.log
updateProcessLog


echo "#Preparing output." >> ../results/process.log
updateProcessLog

    #Convert to fasta format
    reformat_hhsuite.pl sto fas ../results/${JOBID}.msa_sto $(readlink -f ../results/${JOBID}.msa_fas)

    #remove tmp sto file
    rm ../results/${JOBID}.msa_sto

    prepareForHMMER.py ../results/${JOBID}.outfile ../results/${JOBID}.outfilefl

    hmmer2json.py -i ../results/${JOBID}.outfilefl \
                  -o ../results/${JOBID}.json \
                  -m %desc.content \
                  -e %evalue.content


manipulate_json.py -k 'db' -v '%hmmerdb.content' ../results/${JOBID}.json
#create tab separated file to feed into blastviz
hmmerJson2tab.py ../results/${JOBID}.json ../results/query.json ../results/${JOBID}.tab
blastviz_json.pl ../results/${JOBID}.tab %jobid.content ../results/ ../results/ >> ../logs/blastviz.log

# add transmembrane prediction info to json
manipulate_json.py -k 'TMPRED' -v "${TMPRED}" ../results/${JOBID}.json

# add coiled coil prediction info to json
manipulate_json.py -k 'COILPRED' -v "${COILPRED}" ../results/${JOBID}.json



# Generate MSA in JSON
fasta2json.py ../results/${JOBID}.msa_fas ../results/alignment.json

cd ../results
rm *.hmm *.outfile

echo "done" >> ../results/process.log
updateProcessLog