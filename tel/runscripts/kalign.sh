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

if [ %output_order.content = "input"] ; then
    OUTPUTORDER="input"
else
    OUTPUTORDER="tree"
fi

echo "#Aligning sequences with Kalign."  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

kalign -i %alignment.path \
       -o ../results/alignment.clustalw_aln \
       -s %gap_open.content \
       -e %gap_ext_kaln.content \
       -t %gap_term.content \
       -m %bonusscore.content \
       -c ${OUTPUTORDER} \
       -f clu

echo "done"  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Preparing output." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

reformat_hhsuite.pl clu fas ../results/alignment.clustalw_aln ../results/alignment.fas
# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json

echo "done"  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

