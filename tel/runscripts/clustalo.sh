SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

echo "#Read ${SEQ_COUNT} sequences" >> ../results/process.log
echo "done"  >> ../results/process.log

echo "#Aligning sequences with Clustal Omega " >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1




if [ %output_order.content == "input"] ; then
    OUTPUTORDER="--output-order=input-order"
else
    OUTPUTORDER="--output-order=tree-order"
fi

clustalo   -i %alignment.path \
           -o ../results/alignment.clustalw_aln \
           --outfmt=clustal \
           -v \
           --force \
           ${OUTPUTORDER} \
           --threads=%THREADS

echo "done"  >> ../results/process.log
echo "#Preparing output " >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
echo "done"  >> ../results/process.log

reformat_hhsuite.pl clu fas ../results/alignment.clustalw_aln ../results/alignment.fas
# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json
