SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [ ${CHAR_COUNT} -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      false
fi

if [ ${SEQ_COUNT} = "0" ] ; then
      echo "#Invalid input format. Input should be in FASTA format." >> ../results/process.log
      false
fi

if [ ${SEQ_COUNT} -lt "2" ] ; then
      echo "#Input should contain at least 2 sequences." >> ../results/process.log
      false
fi

OUTFORMAT=$(reformatValidator.pl fas ufas \
	        $(readlink -f ../params/alignment) \
            $(readlink -f ../params/alignment) \
            -d 160 -uc -l 32000)

if [ "${OUTFORMAT}" = "ufas" ] ; then
    SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
    echo "#Read ${SEQ_COUNT} sequences." >> ../results/process.log
else
    echo "#Input is not in FASTA format." >> ../results/process.log
    false
fi
echo "done"  >> ../results/process.log

if [ ${SEQ_COUNT} -gt "2000" ] ; then
      echo "#Input contains more than 2000 sequences." >> ../results/process.log
      false
fi

echo "#Aligning sequences with MUSCLE."  >> ../results/process.log

muscle -in %alignment.path \
       -out ../results/alignment.fas \
       -maxiters %maxrounds.content \
       -quiet

echo "done"  >> ../results/process.log

echo "#Preparing output." >> ../results/process.log

echo "done"  >> ../results/process.log

reformat_hhsuite.pl fas clu ../results/alignment.fas  ../results/alignment.clustalw_aln

# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json
