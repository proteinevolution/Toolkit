SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [ ${CHAR_COUNT} -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      updateProcessLog
      false
fi

if [ ${SEQ_COUNT} = "0" ] ; then
      echo "#Invalid input format. Input should be in FASTA format." >> ../results/process.log
      updateProcessLog
      false
fi

if [ ${SEQ_COUNT} -lt "2" ] ; then
      echo "#Input should contain at least 2 sequences." >> ../results/process.log
      updateProcessLog
      false
fi

OUTFORMAT=$(reformatValidator.pl fas ufas \
	        $(readlink -f ../params/alignment) \
            $(readlink -f ../params/alignment) \
            -d 160 -uc -l 32000)

if [ "${OUTFORMAT}" = "ufas" ] ; then
    SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
    echo "#Read ${SEQ_COUNT} sequences." >> ../results/process.log
    updateProcessLog
else
    echo "#Input is not in FASTA format." >> ../results/process.log
    updateProcessLog
    false
fi
echo "done"  >> ../results/process.log
updateProcessLog

if [ ${SEQ_COUNT} -gt "2000" ] ; then
      echo "#Input contains more than 2000 sequences." >> ../results/process.log
      updateProcessLog
      false
fi

if [ %output_order.content = "input" ] ; then
    OUTPUTORDER="--output-order=input-order"
else
    OUTPUTORDER="--output-order=tree-order"
fi

echo "#Aligning sequences with Clustal Omega."  >> ../results/process.log
updateProcessLog

clustalo   -i %alignment.path \
           -o ../results/alignment.clustalw_aln \
           --outfmt=clustal \
           -v \
           --force \
           ${OUTPUTORDER} \
           --threads=%THREADS

echo "done"  >> ../results/process.log
updateProcessLog

echo "#Preparing output." >> ../results/process.log
updateProcessLog

echo "done"  >> ../results/process.log
updateProcessLog

reformat_hhsuite.pl clu fas ../results/alignment.clustalw_aln ../results/alignment.fas
# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json
