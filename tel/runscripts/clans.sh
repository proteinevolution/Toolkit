JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

echo "#Input contains ${SEQ_COUNT} sequences." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

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

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

#remove '-' from sequences, merge sequences split into multiple lines, create an indexed copy of the FASTA file
prepareForClans.pl %alignment.path ../results/${JOBID}.0.fas ../results/${JOBID}.1.fas

echo "#Performing ${SEQ_COUNT} X ${SEQ_COUNT} pairwise BLAST+ comparisons." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

#BLAST formatted database
makeblastdb -in ../results/${JOBID}.1.fas -dbtype prot

#NXN BLAST
blastp -query ../results/${JOBID}.1.fas \
       -db ../results/${JOBID}.1.fas \
       -outfmt "6 qacc sacc evalue" \
       -matrix %matrix.content \
       -evalue 1  \
       -gapopen ${GAPOPEN} \
       -gapextend ${GAPEXT} \
       -max_target_seqs ${SEQ_COUNT} \
       -max_hsps 1 \
       -out ../results/${JOBID}.nxnblast \
       -seg no \
       -num_threads %THREADS
       
echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Generating CLANS file." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1       

blast2clans.pl ../results/${JOBID} ../results/${JOBID}.0.fas ${SEQ_COUNT}

rm ../results/${JOBID}.nxnblast
rm ../results/${JOBID}*fas*

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1