JOBID=%jobid.content

#CHECK IF MSA generation is required or not
SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)
if [ "%max_hhblits_iter.content" = "0" ] && [ $SEQ_COUNT -gt "1" ] ; then
    #Use user MSA to build HMM

    $HMMERPATH/hmmbuild --cpu %THREADS \
             -n "${JOBID}" \
             ../results/${JOBID}.hmm \
             %alignment.path
else
    #MSA generation required; generation by HHblits
    hhblits -cpu %THREADS \
            -v 2 \
            -i %alignment.path \
            -d %UNIPROT  \
            -o ../results/${JOBID}.hhblits \
            -oa3m ../results/${JOBID}.a3m \
            -n %max_hhblits_iter.content \
            -mact 0.35

    #Filter down to a maximum 90% pairwise sequence identity
    hhfilter -i ../results/${JOBID}.a3m \
             -o ../results/${JOBID}.reduced.a3m \
             -id 90

    #Convert to fasta format
    reformat.pl a3m fas ../results/${JOBID}.a3m $(readlink -f ../results/${JOBID}.fas)

    $HMMERPATH/hmmbuild --cpu %THREADS \
             -n "${JOBID}" \
             ../results/${JOBID}.hmm \
             ../results/${JOBID}.fas
fi

    $HMMERPATH/hmmsearch --cpu %THREADS \
          -E %eval_cutoff.content \
          --tblout ../results/${JOBID}.tbl \
          --domtblout ../results/${JOBID}.domtbl \
          -o ../results/${JOBID}.outfile \
          -A ../results/${JOBID}.msa_sto \
          ../results/${JOBID}.hmm  %STANDARD/%standarddb.content


    prepareForHMMER.py ../results/${JOBID}.outfile ../results/${JOBID}.outfilefl


    hmmer2json.py -i ../results/${JOBID}.outfilefl \
                  -o ../results/${JOBID}.json
                  