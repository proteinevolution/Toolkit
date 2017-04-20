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
                  -a e \
                  -v e \
                  -s SPR

if [ "%no_replicates.content" -gt "0" ] ; then
    cat ../results/*phyml_stats.txt ../results/*phy_phyml_boot_stats.txt > ../results/${JOBID}.stats
    rm ../results/*phyml_boot*
    rm ../results/*phyml_stats.txt
else
    mv ../results/*phyml_stats.txt ../results/${JOBID}.stats
fi

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
