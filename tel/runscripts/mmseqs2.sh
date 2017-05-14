JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

if [ $SEQ_COUNT -gt "20000" ] ; then

      echo "#Input contains more than 20000 sequences." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      echo "error" >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      $?=1
fi

THREADS_TO_USE=%THREADS

echo "#Input contains ${SEQ_COUNT} sequences." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

if [ $SEQ_COUNT -lt "100" ] ; then
    THREADS_TO_USE=1
fi

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Clustering down input set." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

#convert input sequences into the MMseqs database format
mmseqs createdb %alignment.path \
                ../results/${JOBID}_seqDB \
                --max-seq-len 30000

#create temp directory
mkdir ../results/tmp

#Let the results of clustering be written in the results directory
#cd ../results

#use linear clustering mode
mmseqs cluster  ../results/${JOBID}_seqDB \
                ../results/${JOBID}_clu \
                ../results/tmp \
                --min-seq-id %min_seqid.content \
                -c %min_aln_cov.content \
                --remove-tmp-files \
                --threads ${THREADS_TO_USE}

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Generating output." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

#Generate FASTA-style output
mmseqs createseqfiledb ../results/${JOBID}_seqDB \
                       ../results/${JOBID}_clu \
                       ../results/${JOBID}_clu_seq \
                       --threads ${THREADS_TO_USE}

mmseqs result2flat     ../results/${JOBID}_seqDB \
                       ../results/${JOBID}_seqDB \
                       ../results/${JOBID}_clu_seq \
                        ../results/${JOBID}_clu_seq.fa


filtermmseqs.pl -i ../results/${JOBID}_clu_seq.fa \
                -o ../results/${JOBID}

rm -r ../results/tmp
rm ../results/${JOBID}_*

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1