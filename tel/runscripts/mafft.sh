JOBID=%jobid.content
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

echo "#Aligning sequences with MAFFT."  >> ../results/process.log
updateProcessLog

if [ %output_order.content = "input" ] ; then

    mafft --op %mafft_gap_open.content \
          --preservecase \
          --ep %offset.content \
          --quiet \
          --auto \
          --thread %THREADS \
          %alignment.path > ../results/alignment.fas
else
    mafft --op %mafft_gap_open.content \
          --preservecase \
          --ep %offset.content \
          --quiet \
          --reorder \
          --auto \
          --thread %THREADS \
          %alignment.path > ../results/alignment.fas
fi

echo "done"  >> ../results/process.log
updateProcessLog

echo "#Preparing output." >> ../results/process.log
updateProcessLog

echo "done"  >> ../results/process.log
updateProcessLog

# Convert FASTA to CLUSTAL
reformat_hhsuite.pl fas clu ../results/alignment.fas ../results/alignment.clustalw_aln

# Convert FASTA to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json