SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [[ ${CHAR_COUNT} -gt "10000" ]] ; then
      echo "#Input may not contain more than 10000 characters." >> ../results/process.log
      false
fi

if [[ ${FORMAT} = "1" ]] || [[ ${SEQ_COUNT} -gt "1" ]] ; then
      echo "#Input is a multiple sequence alignment; expecting a single protein sequence." >> ../results/process.log
      false
fi

if [[ ${SEQ_COUNT} = "0" ]] ; then
      sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
      perl -pe 's/\s+//g' ../params/alignment1 > ../params/alignment
      CHAR_COUNT=$(wc -m < ../params/alignment)

      if [[ ${CHAR_COUNT} -gt "10000" ]] ; then
            echo "#Input may not contain more than 10000 characters." >> ../results/process.log
            false
      else
            sed -i "1 i\>${JOBID}" ../params/alignment
      fi
fi

echo "#Query is a protein sequence with ${CHAR_COUNT} residues." >> ../results/process.log

echo "done" >> ../results/process.log

echo "#Executing MARCOIL." >> ../results/process.log

# Switch on correct Matrix
if [[ "mtk" = "%matrix_marcoil.content" ]] ; then

    matrix_copy.sh "${MARCOILMTK}" ../0/R5.MTK
    matrix_copy.sh "${MARCOILMTIDK}" ../0/R5.MTIDK
    PARAMMATRIX="-C"

elif  [[ "mtidk" = "%matrix_marcoil.content" ]] ; then

    matrix_copy.sh "${MARCOILMTIDK}" ../0/R5.MTIDK
    matrix_copy.sh "${MARCOILMTK}" ../0/R5.MTK
    PARAMMATRIX="-C -i"

elif  [[ "9fam" = "%matrix_marcoil.content" ]] ; then

    PARAMMATRIX=""
fi

TRANSPROB=${MARCOILINPUT}/R3.transProbHigh
if [[ "%transition_probability.content" = "0" ]] ; then

    TRANSPROB="${MARCOILINPUT}/R3.transProbLow"
fi

cp %alignment.path ../params/alignment.in
marcoil  ${PARAMMATRIX} \
                      +dssSl \
                      -T ${TRANSPROB} \
                      -E ${MARCOILINPUT}/R2.emissProb \
                      -P ../params/alignment \
                      ../params/alignment.in

echo "done" >> ../results/process.log

echo "#Preparting OUTPUT." >> ../results/process.log

# Prepare MARCOIL GNUPLOT
if [[ "${PARAMMATRIX}" = "-C -i" ]] || [[ "${PARAMMATRIX}" = "-C" ]] ;then

    prepare_marcoil_gnuplot.pl "$(readlink -f ../params/alignment)" "$(readlink -f ../params/alignment.ProbListPSSM)" 
    cp ../params/alignment.ProbListPSSM ../params/alignment.ProbList
    cp ../params/alignment.DomainsPSSM ../params/alignment.Domains
    cp ../params/alignment.CompactProfilePSSM ../params/alignment.ProbPerState

else
    prepare_marcoil_gnuplot.pl "$(readlink -f ../params/alignment)" "$(readlink -f ../params/alignment.ProbList)"
fi

# Numerical output
create_numerical_marcoil.rb "$(readlink -f ../params/)/"

cp ../params/* ../results/

echo "done" >> ../results/process.log
