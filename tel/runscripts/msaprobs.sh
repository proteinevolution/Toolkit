JOBID=%jobid.content
SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

if [ $SEQ_COUNT -gt "2000" ] ; then

      echo "#Input contains more than 2000 sequences." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      echo "error" >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      $?=1
fi

echo "#Read ${SEQ_COUNT} sequences." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "done"  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Aligning sequences with MSAProbs."  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

if [ %output_order.content == "input"] ; then
    msaprobs %alignment.path \
                -num_threads %THREADS \
                -o ../results/alignment.fas
else
    msaprobs %alignment.path \
               -num_threads %THREADS \
               -a \
               -o ../results/alignment.fas
fi

echo "done"  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Preparing output." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "done"  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

reformat_hhsuite.pl fas clu ../results/alignment.fas ../results/alignment.clustalw_aln
# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json
