SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

echo "#Read ${SEQ_COUNT} sequences." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "done"  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Aligning sequences with MAFFT."  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

if [ %output_order.content == "input"] ; then

    mafft --op %gap_open.content \
          --ep %offset.content \
          --quiet \
          %alignment.path > ../results/alignment.fas
else
    mafft --op %gap_open.content \
          --ep %offset.content \
          --quiet \
          --reorder \
          %alignment.path > ../results/alignment.fas
fi

echo "done"  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Preparing output." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "done"  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

# Convert FASTA to CLUSTAL
reformat_hhsuite.pl fas clu ../results/alignment.fas ../results/alignment.clustalw_aln

# Convert FASTA to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json
