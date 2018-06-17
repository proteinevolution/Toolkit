SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [ ${CHAR_COUNT} -gt "10000000" ] ; then
      echo "#Input may no contain more than 10000000 characters." >> ../results/process.log
      false
fi

if [ ${SEQ_COUNT} = "0" ] && [ ${FORMAT} = "0" ] ; then
      echo "#Invalid input format. Input should be in aligned FASTA/CLUSTAL format." >> ../results/process.log
      false
fi

if [ ${FORMAT} = "1" ] ; then

      OUTFORMAT=$(reformatValidator.pl clu fas \
	        $(readlink -f ../params/alignment) \
            $(readlink -f ../params/alignment) \
            -d 160 -uc -l 32000)
else
      OUTFORMAT=$(reformatValidator.pl fas fas \
	        $(readlink -f ../params/alignment) \
            $(readlink -f ../params/alignment) \
            -d 160 -uc -l 32000)
fi

if [ "${OUTFORMAT}" = "fas" ] ; then

    SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
    echo "#Read MSA with ${SEQ_COUNT} sequences." >> ../results/process.log

else
    echo "#Input is not in aligned FASTA/CLUSTAL format." >> ../results/process.log
    false
fi
echo "done" >> ../results/process.log

if [ ${SEQ_COUNT} -gt "2000" ] ; then
      echo "#Input contains more than 2000 sequences." >> ../results/process.log
      false
fi

prepareForAncescon.pl %alignment.path ../results/${JOBID}.in ../results/${JOBID}.names

reformat_hhsuite.pl fas clu "$(readlink -f ../results/${JOBID}.in)" "$(readlink -f ../results/${JOBID}.clu)"

# Remove CLUSTAL text in alignment.clu
sed -i '/CLUSTAL/Id' ../results/${JOBID}.clu

echo "Running ANCESCON on query MSA." >> ../results/process.log

ancestral -i ../results/${JOBID}.clu \
          -o ../results/${JOBID}.anc_out

echo "done" >> ../results/process.log

echo "#Preparing output." >> ../results/process.log

echo "done" >> ../results/process.log

cp ../results/${JOBID}.clu.tre  ../results/${JOBID}.clu.orig.tre
ancescontreemerger.pl -n ../results/${JOBID}.names -t ../results/${JOBID}.clu.tre

rm ../results/${JOBID}.clu.dis
rm ../results/${JOBID}.names
rm ../results/${JOBID}.clu
rm ../results/${JOBID}.clu.orig.tre
rm ../results/${JOBID}.in
