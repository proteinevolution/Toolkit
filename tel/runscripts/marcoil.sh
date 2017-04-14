CHAR_COUNT=$(egrep '^>' ../params/alignment  -c)

echo "#Query is a protein sequence with ${CHAR_COUNT} residues." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1


echo "#Executing MARCOIL." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

# Switch on correct Matrix
if [ "mtk" = "%matrix_marcoil.content" ] ; then

    matrix_copy.sh "${MARCOILMTK}" ../params//R5.MTK
    matrix_copy.sh "${MARCOILMTIDK}" ../params//R5.MTIDK
    PARAMMATRIX="-C"

elif  [ "mtidk" = "%matrix_marcoil.content" ] ; then

    matrix_copy.sh "${MARCOILMTIDK}" ../params//R5.MTIDK
    matrix_copy.sh "${MARCOILMTK}" ../params//R5.MTK
    PARAMMATRIX="-C -i"

elif  [ "9fam" = "%matrix_marcoil.content" ] ; then

    PARAMMATRIX=""
fi

TRANSPROB=${MARCOILINPUT}/R3.transProbHigh
if [ "%transition_probability.content" = "1" ] ; then

    TRANSPROB="${MARCOILINPUT}/R3.transProbLow"
fi

cp %alignment.path ../params/alignment.in
marcoil  ${PARAMMATRIX} \
                      +dssSl \
                      -T ${TRANSPROB} \
                      -E ${MARCOILINPUT}/R2.emissProb \
                      -P "$(readlink -f ../params/alignment)" \
                      "$(readlink -f ../params/alignment.in)"

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Preparting OUTPUT." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

# Prepare MARCOIL GNUPLOT
if [ "${PARAMMATRIX}" = "-C -i" ] || [ "${PARAMMATRIX}" = "-C" ] ;then

    prepare_marcoil_gnuplot.pl "$(readlink -f ../params/alignment)" "$(readlink -f ../params/alignment.ProbListPSSM)" 
    cp ../params/alignment.ProbListPSSM ../params/alignment.ProbList
else
    prepare_marcoil_gnuplot.pl "$(readlink -f ../params/alignment)" "$(readlink -f ../params/alignment.ProbList)"
fi

# Numerical output
create_numerical_marcoil.rb "$(readlink -f ../params/)/"

cp ../params/* ../results/

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
