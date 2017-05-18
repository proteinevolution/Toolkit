JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)
FORMAT=$(head -1 ../params/alignment | egrep "^CLUSTAL" | wc -l)

if [ $CHAR_COUNT -gt "1000000" ] ; then
      echo "#Input may not contain more than 1000000 characters." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

if [ $SEQ_COUNT = "0" ] && [ $FORMAT = "0" ] ; then
      echo "#Invalid input format. Input should be in aligned FASTA/CLUSTAL format." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

if [ $FORMAT = "1" ] ; then
      reformatValidator.pl clu fas \
            $(readlink -f %alignment.path) \
            $(readlink -f ../results/${JOBID}.aln) \
            -d 160 -uc -l 32000
else
      reformatValidator.pl fas fas \
            $(readlink -f %alignment.path) \
            $(readlink -f ../results/${JOBID}.aln) \
            -d 160 -uc -l 32000
fi

if [ ! -f ../results/${JOBID}.aln ]; then
    echo "#Input is not in aligned FASTA/CLUSTAL format." >> ../results/process.log
    curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
    false
fi

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.aln | wc -l)

if [ $SEQ_COUNT -gt "2000" ] ; then
      echo "#Input contains more than 2000 sequences." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

if [ $SEQ_COUNT -gt "1" ] ; then
       echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
       curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
else
       echo "#Query is a single protein sequence. Please input an alignment." >> ../results/process.log
       curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
       false
fi
echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1


cd ../results/

echo "#Running Ali2D on the input alignment." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

java -Xmx8g -jar $ALI2DPATH/prepareAli2d.jar ../results/${JOBID}.aln ${JOBID} %invoke_psipred.content ${JOBID}.mainlog

cd ../0/

runMemsat2.pl ../results/${JOBID}.mainlog

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Generating output pages." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1


java -Xmx8000m -jar $ALI2DPATH/buildParams.jar ../results/${JOBID}.aln ../results/${JOBID}.mainlog > ../results/${JOBID}.results

/usr/bin/python $ALI2DPATH/viewer.py ../results/${JOBID}.results ../results/${JOBID}.results_colorC color true
/usr/bin/python $ALI2DPATH/viewer.py ../results/${JOBID}.results ../results/${JOBID}.results_color color false

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
