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

mkdir ../results/tmp

#Perform all-against-all sequence comparison using legacy BLAST
java -Xmx24G -jar ${CLANSPATH}/allblast.jar \
     -infile %alignment.path \
     -blastpath "blastall -p blastp -e %evalue.content -F F -M BLOSUM80 -G ${GAPOPEN} -E ${GAPEXT} -g T -v ${SEQ_COUNT} -b ${SEQ_COUNT} -T T -I T " \
     -formatdbpath formatdb \
     -eval 10 \
     -saveblast ../results/${JOBID}.nxnblast \
     -tmpdir ../results/tmp/


#Perform clustering
java -Xmx24G -jar ${CLANSPATH}/blast2clans.jar \
     -i ../results/${JOBID}.nxnblast \
     -o ../results/${JOBID}.clans \
     -savetype all \
     -pval %clustering_pval_threshold.content

rm -r ../results/tmp