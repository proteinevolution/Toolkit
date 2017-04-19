JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

reformat_phylip.pl -i=fas \
                   -o=phy \
                   -f=$(readlink -f %alignment.path) \
                   -a=$(readlink -f ../results/${JOBID}.phy)

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Running PhyML." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

PhyML-3.1_linux64 -i ../results/${JOBID}.phy \
                  -d aa \
                  - %matrix_phyml.content \
                  -b %no_replicates.content \
                  -a %gamma_rate.content \
                  -v 0.0
echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
