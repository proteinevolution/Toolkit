JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

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

removeInvalid.pl %alignment.path ../results/${JOBID}.fas

ffindex_from_fasta ../results/${JOBID}.ffdata ../results/${JOBID}.ffindex ../results/${JOBID}.fas
makeblastdb -in ../results/${JOBID}.fas -dbtype prot


blastp -query ../results/${JOBID}.fas \
       -db ../results/${JOBID}.fas \
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

blast2clans.pl ../results/${JOBID} ../results/${JOBID}.fas