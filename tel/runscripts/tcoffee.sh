JOBID=%jobid.content
SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

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

if [ ${SEQ_COUNT} -gt "500" ] ; then
      echo "#Input contains more than 500 sequences." >> ../results/process.log
      updateProcessLog
      false
fi

shortenFastaHeader.pl -i %alignment.path -o ../results/alignment -d 160

export DIR_4_TCOFFEE=./tmp
export TMP_4_TCOFFEE=./tmp
export CACHE_4_TCOFFEE=./tmp

if [ %output_order.content = "input"] ; then
    OUTPUTORDER="-outorder=input"
else
    OUTPUTORDER="-outorder=aligned "
fi

echo "#Aligning sequences with T-Coffee."  >> ../results/process.log
updateProcessLog

if [ %output_order.content = "input"] ; then
        t_coffee -in ../results/alignment \
                -cache=no \
                ${OUTPUTORDER} \
                -output clustalw_aln \
                -case=upper \
                -mode=regular \
                -n_core=%THREADS
else
        t_coffee -in ../results/alignment \
                -cache=no \
                ${OUTPUTORDER} \
                -output clustalw_aln \
                -case=upper \
                -mode=regular \
                -n_core=%THREADS
fi

echo "done"  >> ../results/process.log
updateProcessLog

echo "#Preparing output." >> ../results/process.log
updateProcessLog

echo "done"  >> ../results/process.log
updateProcessLog

mv alignment.* ../results/

reformat_hhsuite.pl clu fas ../results/alignment.clustalw_aln ../results/alignment.fas

# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json
