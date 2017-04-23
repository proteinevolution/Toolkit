JOBID=%jobid.content
mv %alignment.path ../results/${JOBID}.pdb

firstposHelix1="a"
firstposHelix2="a"
firstposHelix3="a"
firstposHelix4="a"
chainHelix1="A"
chainHelix2="B"
chainHelix3="C"
chainHelix4="D"
posHelix1="2-30"
posHelix2="2-30"
posHelix3="2-30"
posHelix4="2-30"

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

/usr/bin/python $SAMCCPATH/samcc.py ../results/${JOBID}.params ../results/${JOBID}.out
mv temp* ../results/

/usr/bin/gnuplot ../results/temp0.run
/usr/bin/gnuplot ../results/temp1.run
/usr/bin/gnuplot ../results/temp2.run
/usr/bin/gnuplot ../results/temp3.run





