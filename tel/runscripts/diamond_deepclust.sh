SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [[ ${CHAR_COUNT} -gt "100000000" ]] ; then
      echo "#Input may not contain more than 100000000 characters." >> ../results/process.log
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

if [[ ${SEQ_COUNT} -gt "50000" ]] ; then
      echo "#Input contains more than 50000 sequences." >> ../results/process.log
      false
fi

echo "#Clustering down input set." >> ../results/process.log

# Remove duplicate entries from the input file
${DIAMONDPATH}/seqkit rmdup -j 4 %alignment.path > ../results/${JOBID}_input.fas

# Cluster using Diamond-DeepClust
${DIAMONDPATH}/diamond cluster -d ../results/${JOBID}_input.fas \
                                -o ../results/${JOBID}.clu \
                                --approx-id %diamond_min_seqid.content \
                                -M 128G \
                                -p %THREADS \
                                --member-cover %min_aln_cov.content

# Extract identifiers of representatives
cut -f1 ../results/${JOBID}.clu  | sort | uniq > ../results/${JOBID}.rep

# Extract sequences of representatives
${DIAMONDPATH}/seqkit grep -j 4 -f ../results/${JOBID}.rep ../results/${JOBID}_input.fas  > ../results/${JOBID}.fas

echo "done" >> ../results/process.log

echo "#Generating output." >> ../results/process.log

sed -i "1 i\Number of sequences in the input set: ${SEQ_COUNT}" ../results/${JOBID}.clu
SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.fas | wc -l)
sed -i "2 i\Number of sequences in the reduced set: ${SEQ_COUNT}" ../results/${JOBID}.clu

rm ../results/${JOBID}_input.fas ../results/${JOBID}.rep

echo "done" >> ../results/process.log