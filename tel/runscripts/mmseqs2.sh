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

if [ ${SEQ_COUNT} -gt "20000" ] ; then
      echo "#Input contains more than 20000 sequences." >> ../results/process.log
      updateProcessLog
      false
fi

THREADS_TO_USE=%THREADS

if [ ${SEQ_COUNT} -lt "100" ] ; then
    THREADS_TO_USE=1
fi

echo "#Clustering down input set." >> ../results/process.log
updateProcessLog

#convert input sequences into the MMseqs database format
mmseqs createdb %alignment.path \
                ../results/${JOBID}_seqDB \
                --max-seq-len 30000

#create temp directory
mkdir ../results/tmp

#Let the results of clustering be written in the results directory
#cd ../results

#use linear clustering mode
mmseqs cluster  ../results/${JOBID}_seqDB \
                ../results/${JOBID}_clu \
                ../results/tmp \
                --min-seq-id %min_seqid.content \
                -c %min_aln_cov.content \
                --remove-tmp-files \
                --threads ${THREADS_TO_USE}

echo "done" >> ../results/process.log
updateProcessLog

echo "#Generating output." >> ../results/process.log
updateProcessLog

#Generate FASTA-style output
mmseqs createseqfiledb ../results/${JOBID}_seqDB \
                       ../results/${JOBID}_clu \
                       ../results/${JOBID}_clu_seq \
                       --threads ${THREADS_TO_USE}

mmseqs result2flat     ../results/${JOBID}_seqDB \
                       ../results/${JOBID}_seqDB \
                       ../results/${JOBID}_clu_seq \
                        ../results/${JOBID}_clu_seq.fa


filtermmseqs.pl -i ../results/${JOBID}_clu_seq.fa \
                -o ../results/${JOBID}

rm -r ../results/tmp
rm ../results/${JOBID}_*

echo "done" >> ../results/process.log
updateProcessLog