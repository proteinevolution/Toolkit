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
            sed -i "1 i\>Q_${JOBID}" ../params/alignment1
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


#CHECK IF MSA generation is required or not
if [ "%msa_gen_max_iter.content" = "0" ] && [ ${SEQ_COUNT} -gt "1" ] ; then
        echo "#No MSA generation required for building A3M." >> ../results/process.log
        updateProcessLog
        reformat_hhsuite.pl fas a3m ../results/${JOBID}.fas ${JOBID}.a3m -M first
        mv ${JOBID}.a3m ../results/${JOBID}.a3m
        hhfilter -i ../results/${JOBID}.a3m \
                 -o ../results/${JOBID}.a3m \
                 -cov %min_cov.content\
                 -qid %min_seqid_query.content

        echo "done" >> ../results/process.log
        updateProcessLog
else
    #MSA generation required
    #Check what method to use (PSI-BLAST? HHblits?)

        echo "#Query MSA generation required." >> ../results/process.log
        updateProcessLog
        echo "done" >> ../results/process.log
        updateProcessLog

    #MSA generation by HHblits
    if [ "%msa_gen_method.content" = "hhblits" ] ; then
        echo "#Running HHblits for query MSA and A3M generation." >> ../results/process.log
        updateProcessLog
        hhblits -cpu %THREADS \
                -v 2 \
                -e %hhpred_incl_eval.content \
                -i ../results/${JOBID}.fas \
                -d %UNIPROT  \
                -oa3m ../results/${JOBID}.a3m \
                -n %msa_gen_max_iter.content \
                -qid %min_seqid_query.content \
                -cov %min_cov.content \
                -mact 0.35

        echo "done" >> ../results/process.log
        updateProcessLog

    fi
    #MSA generation by PSI-BLAST
    if [ "%msa_gen_method.content" = "psiblast" ] ; then

        echo "#Running PSI-BLAST for query MSA and A3M generation." >> ../results/process.log
        updateProcessLog
        #Check if input is a single sequence or an MSA
        INPUT="query"
        if [ ${SEQ_COUNT} -gt 1 ] ; then
            INPUT="in_msa"
        fi

        psiblast -db ${STANDARD}/nre70 \
                 -num_iterations %msa_gen_max_iter.content \
                 -evalue 0.001 \
                 -inclusion_ethresh %hhpred_incl_eval.content\
                 -num_threads %THREADS \
                 -num_descriptions 20000 \
                 -num_alignments 20000 \
                 -${INPUT} ../results/${JOBID}.fas \
                 -out ../results/output_psiblastp.html

        #keep results only of the last iteration
        shorten_psiblast_output.pl ../results/output_psiblastp.html ../results/output_psiblastp.html

        #extract MSA in a3m format
        alignhits_html.pl   ../results/output_psiblastp.html ../results/${JOBID}.a3m \
                    -Q ../results/${JOBID}.fas \
                    -e %hhpred_incl_eval.content \
                    -cov %min_cov.content \
                    -a3m \
                    -no_link \
                    -blastplus
        echo "done" >> ../results/process.log
        updateProcessLog
    fi
fi

#Generate representative MSA for forwarding

hhfilter -i ../results/${JOBID}.a3m \
         -o ../results/reduced.fas \
         -diff 100

reformat_hhsuite.pl a3m fas \
         $(readlink -f ../results/reduced.fas) \
         $(readlink -f ../results/reduced.fas) \
         -d 160

addss.pl ../results/${JOBID}.a3m


echo "#Searching profile HMM database(s)." >> ../results/process.log
updateProcessLog

${HHOMPPATH}/hhmake -v 1 -cov 20 -qid 0 -diff 100 \
                    -i ../results/${JOBID}.a3m \
                    -o ../results/${JOBID}.hhm

${HHOMPPATH}/hhomp -cpu %THREADS \
                   -i ../results/${JOBID}.hhm \
                   -d ${HHOMPPATH}/cal.hhm \
                   -cal -local

${HHOMPPATH}/hhomp -cpu %THREADS \
                   -i ../results/${JOBID}.hhm \
                   -d ${HHOMPDBPATH}/%hhompdb.content \
                   -o ../results/${JOBID}.hhr \
                   -p %pmin.content \
                   -P %pmin.content \
                   -Z %desc.content \
                   -%alignmode.content \
                   -B %desc.content \
                   -seq 1 \
                   -b 1

echo "done" >> ../results/process.log
updateProcessLog

echo "#Preparing output." >> ../results/process.log
updateProcessLog

hhviz.pl ${JOBID} ../results/ ../results/  &> /dev/null

#create full alignment json; use for forwarding
fasta2json.py ../results/reduced.fas ../results/reduced.json


# Generate Hitlist in JSON for hhrfile
${HHOMPPATH}/hhomp_hhr2json.py "$(readlink -f ../results/${JOBID}.hhr)" > ../results/${JOBID}.json

# add DB to json
manipulate_json.py -k 'db' -v '%hhompdb.content' ../results/${JOBID}.json

# Generate Query in JSON
fasta2json.py ../results/${JOBID}.fas ../results/query.json

echo "done" >> ../results/process.log
updateProcessLog