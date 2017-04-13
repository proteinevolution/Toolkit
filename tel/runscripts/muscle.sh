JOBID=%jobid.content
SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

echo "#Read ${SEQ_COUNT} sequences." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "done"  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Aligning sequences with MUSCLE."  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

if [ %output_order.content == "input"] ; then
    muscle -in %alignment.path \
            -out ../results/alignment.fas \
            -maxiters %maxrounds.content \
            -quiet \
            -stable
else
    muscle -in %alignment.path \
            -out ../results/alignment.fas \
            -maxiters %maxrounds.content \
            -quiet
fi

echo "done"  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Preparing output." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "done"  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

reformat_hhsuite.pl fas clu ../results/alignment.fas  ../results/alignment.clustalw_aln

# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json
