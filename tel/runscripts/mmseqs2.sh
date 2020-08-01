SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [[ ${CHAR_COUNT} -gt "10000000" ]] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      false
fi

if [[ ${SEQ_COUNT} = "0" ]] ; then
      echo "#Invalid input format. Input should be in FASTA format." >> ../results/process.log
      false
fi

if [[ ${SEQ_COUNT} -lt "2" ]] ; then
      echo "#Input should contain at least 2 sequences." >> ../results/process.log
      false
fi

OUTFORMAT=$(reformatValidator.pl fas ufas \
	        $(readlink -f ../params/alignment) \
            $(readlink -f ../params/alignment) \
            -d 160 -uc -l 32000)

if [[ "${OUTFORMAT}" = "ufas" ]] ; then
    SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
    echo "#Read ${SEQ_COUNT} sequences." >> ../results/process.log
else
    echo "#Input is not in FASTA format." >> ../results/process.log
    false
fi
echo "done"  >> ../results/process.log

if [[ ${SEQ_COUNT} -gt "20000" ]] ; then
      echo "#Input contains more than 20000 sequences." >> ../results/process.log
      false
fi

THREADS_TO_USE=%THREADS

if [[ ${SEQ_COUNT} -lt "100" ]] ; then
    THREADS_TO_USE=1
fi

echo "#Clustering down input set." >> ../results/process.log

#convert input sequences into the MMseqs database format
mmseqs createdb %alignment.path \
                ../results/${JOBID}_seqDB \
                --max-seq-len 30000

#create temp directory
mkdir ../results/tmp

#Let the results of clustering be written in the results directory
#cd ../results

#use linear clustering mode
mmseqs %clustering_mode.content  ../results/${JOBID}_seqDB \
                ../results/${JOBID}_clu \
                ../results/tmp \
                --min-seq-id %min_seqid.content \
                -c %min_aln_cov.content \
                --remove-tmp-files \
                --threads ${THREADS_TO_USE}

echo "done" >> ../results/process.log

echo "#Generating output." >> ../results/process.log

#Generate FASTA-style output
mmseqs result2repseq ../results/${JOBID}_seqDB \
                       ../results/${JOBID}_clu \
                       ../results/${JOBID}_clu_rep \
                       --threads ${THREADS_TO_USE}

mmseqs result2flat     ../results/${JOBID}_seqDB \
                       ../results/${JOBID}_seqDB \
                       ../results/${JOBID}_clu_rep \
                        ../results/${JOBID}.fas \
                        --use-fasta-header

#remove carriage return
sed -i 's/\r//g' ../results/${JOBID}.fas

#Generate clusters
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

sed -i "1 i\Number of sequences in the input set: ${SEQ_COUNT}" ../results/${JOBID}.clu

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.fas | wc -l)

sed -i "2 i\Number of sequences in the reduced set: ${SEQ_COUNT}" ../results/${JOBID}.clu


rm -r ../results/tmp
rm ../results/${JOBID}_*

echo "done" >> ../results/process.log