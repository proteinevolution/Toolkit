JOBID=%jobid.content
mv %alignment.path ../results/${JOBID}.aln

java -Xmx500m -jar $ALI2DPATH/prepareAli2d.jar ../results/${JOBID}.aln ${JOBID} 30 1007101.mainlog