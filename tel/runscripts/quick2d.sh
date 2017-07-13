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
if [ "%quick_iters.content" = "0" ] && [ ${SEQ_COUNT} -gt "1" ] ; then
        echo "#No MSA generation required for building A3M." >> ../results/process.log
        updateProcessLog
else
    #MSA generation required
    #Check what method to use (PSI-BLAST? HHblits?)

        echo "#Query MSA generation required." >> ../results/process.log
        updateProcessLog
        echo "done" >> ../results/process.log
        updateProcessLog
    #MSA generation by PSI-BLAST
        echo "#Running PSI-BLAST for query MSA and A3M generation." >> ../results/process.log
        updateProcessLog
        #Check if input is a single sequence or an MSA
        INPUT="query"
        if [ ${SEQ_COUNT} -gt 1 ] ; then
            INPUT="in_msa"
        fi

        psiblast -db ${STANDARD}/nr_euk70 \
                 -num_iterations %quick_iters.content \
                 -evalue %hhpred_incl_eval.content \
                 -inclusion_ethresh 0.001 \
                 -num_threads %THREADS \
                 -num_descriptions 20000 \
                 -num_alignments 20000 \
                 -${INPUT} ../results/${JOBID}.fas \
                 -out ../results/output_psiblastp.html

        #keep results only of the last iteration
        shorten_psiblast_output.pl ../results/output_psiblastp.html ../results/output_psiblastp.html

        #extract MSA in a3m format
        alignhits_html.pl   ../results/output_psiblastp.html ../results/${JOBID}.aln \
                    -Q ../results/${JOBID}.fas \
                    -e %hhpred_incl_eval.content \
                    -cov 50 \
                    -fas \
                    -no_link \
                    -blastplus
        echo "done" >> ../results/process.log
        updateProcessLog
fi

reformat_hhsuite.pl fas a3m \
         $(readlink -f ../results/${JOBID}.aln) \
         $(readlink -f ../results/${JOBID}.a3m) \
         -d 160

addss.pl ../results/${JOBID}.a3m

fasta2json.py ../results/${JOBID}.fas ../results/query.json
echo "{}" > ../results/${JOBID}.json

PSIPRED="$(sed -n '2{p;q;}' ../results/${JOBID}.a3m)"
manipulate_json.py -k 'psipred' -v "$PSIPRED" ../results/${JOBID}.json

PSIPREDCONF="$(sed -n '4{p;q;}' ../results/${JOBID}.a3m)"
manipulate_json.py -k 'psipred_conf' -v "$PSIPREDCONF" ../results/${JOBID}.json
