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

if [ ${SEQ_COUNT} -gt "10000" ] ; then
      echo "#Input contains more than 10000 sequences." >> ../results/process.log
      false
fi

# set gapopen and gapextend costs depending on given matrix
GAPOPEN=11
GAPEXT=1
INPUT="query"

if [ "%matrix.content" = "BLOSUM80" ] || [ "%matrix.content" = "PAM70" ] ; then
    GAPOPEN=10
fi
if [ "%matrix.content" = "PAM30" ] ; then
    GAPOPEN=9
fi
if [ "%matrix.content" = "BLOSUM45" ] ; then
    GAPOPEN=15
    GAPEXT=2
fi

#remove '-' from sequences, merge sequences split into multiple lines, create an indexed copy of the FASTA file
prepareForClans.pl %alignment.path ../results/${JOBID}.0.fas ../results/${JOBID}.1.fas

echo "#Performing ${SEQ_COUNT} X ${SEQ_COUNT} pairwise BLAST+ comparisons." >> ../results/process.log

#BLAST formatted database
makeblastdb -in ../results/${JOBID}.1.fas -dbtype prot

#NXN BLAST
blastp -query ../results/${JOBID}.1.fas \
       -db ../results/${JOBID}.1.fas \
       -outfmt "6 qacc sacc evalue" \
       -matrix %matrix.content \
       -evalue %clans_eval.content  \
       -gapopen ${GAPOPEN} \
       -gapextend ${GAPEXT} \
       -max_target_seqs ${SEQ_COUNT} \
       -max_hsps 1 \
       -out ../results/${JOBID}.nxnblast \
       -seg no \
       -num_threads %THREADS
       
echo "done" >> ../results/process.log

echo "#Generating CLANS file." >> ../results/process.log

blast2clans.pl ../results/${JOBID} ../results/${JOBID}.0.fas ${SEQ_COUNT}

cd ../results/
zip -q ${JOBID}.clans.zip ${JOBID}.clans

rm ${JOBID}.clans
rm ${JOBID}.nxnblast
rm ${JOBID}*fas*

echo "done" >> ../results/process.log