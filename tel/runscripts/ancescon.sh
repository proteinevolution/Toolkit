JOBID=%jobid.content

reformat_hhsuite.pl fas fas $(readlink -f %alignment.path) $(readlink -f %alignment.path) -uc -l 32000
SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

prepareForAncescon.pl %alignment.path ../results/${JOBID}.in ../results/${JOBID}.names

reformat_hhsuite.pl fas clu "$(readlink -f ../results/${JOBID}.in)" "$(readlink -f ../results/${JOBID}.clu)"

# Remove CLUSTAL text in alignment.clu
sed -i '/CLUSTAL/Id' ../results/${JOBID}.clu

echo "Running ANCESCON on query MSA." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

ancestral -i ../results/${JOBID}.clu \
          -o ../results/${JOBID}.anc_out

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Preparing output." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

cp ../results/${JOBID}.clu.tre  ../results/${JOBID}.clu.orig.tre
ancescontreemerger.pl -n ../results/${JOBID}.names -t ../results/${JOBID}.clu.tre