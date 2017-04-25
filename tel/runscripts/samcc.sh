JOBID=%jobid.content
cp %alignment.path ../results/${JOBID}.pdb

echo "#Initializing SamCC." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

IFS=';' read -a valshelix1 <<< "%samcc_helixone.content"
IFS=';' read -a valshelix2 <<< "%samcc_helixtwo.content"
IFS=';' read -a valshelix3 <<< "%samcc_helixthree.content"
IFS=';' read -a valshelix4 <<< "%samcc_helixfour.content"


#Convert to lower case if upper
firstposHelix1="${valshelix1[0],,}"
firstposHelix2="${valshelix2[0],,}"
firstposHelix3="${valshelix3[0],,}"
firstposHelix4="${valshelix4[0],,}"

chainHelix1="${valshelix1[1]}"
chainHelix2="${valshelix2[1]}"
chainHelix3="${valshelix3[1]}"
chainHelix4="${valshelix4[1]}"

posHelix1="${valshelix1[2]}-${valshelix1[3]}"
posHelix2="${valshelix2[2]}-${valshelix2[3]}"
posHelix3="${valshelix3[2]}-${valshelix3[3]}"
posHelix4="${valshelix4[2]}-${valshelix4[3]}"

echo "!:crick:%eff_crick_angle.content" >> ../results/${JOBID}.params
echo "!:periodicity:%samcc_periodicity.content" >> ../results/${JOBID}.params
echo "!:firstpos:${firstposHelix1},${firstposHelix2},${firstposHelix3},${firstposHelix4}" >> ../results/${JOBID}.params
echo "!:pdb:../results/${JOBID}.pdb"  >> ../results/${JOBID}.params
echo "${chainHelix1}:1:${posHelix1}" >> ../results/${JOBID}.params
echo "${chainHelix2}:2:${posHelix2}" >> ../results/${JOBID}.params
echo "${chainHelix3}:3:${posHelix3}" >> ../results/${JOBID}.params
echo "${chainHelix4}:4:${posHelix4}" >> ../results/${JOBID}.params
echo "!:ref:$SAMCCPATH/beammotifcc_heptad.pdb.res" >> ../results/${JOBID}.params
echo "1:1:1-1" >> ../results/${JOBID}.params
echo "2:2:1-1" >> ../results/${JOBID}.params
echo "3:3:1-1" >> ../results/${JOBID}.params
echo "4:4:1-1" >> ../results/${JOBID}.params
echo "!:end" >> ../results/${JOBID}.params

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Running SamCC." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1


/usr/bin/python $SAMCCPATH/samcc.py ../results/${JOBID}.params ../results/${JOBID}.out

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Preparing output." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

/usr/bin/gnuplot temp0.run
/usr/bin/gnuplot temp1.run
/usr/bin/gnuplot temp2.run
/usr/bin/gnuplot temp3.run

cat out_axes.pdb out.pdb > ../results/${JOBID}.pdb
mv out* ../results/
rm temp*
echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1





