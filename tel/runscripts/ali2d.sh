JOBID=%jobid.content
cp %alignment.path ../results/${JOBID}.aln

cd ../results/

java -Xmx8g -jar $ALI2DPATH/prepareAli2d.jar ../results/${JOBID}.aln ${JOBID} %invoke_psipred.content ${JOBID}.mainlog

cd ../0/

runMemsat2.pl ../results/${JOBID}.mainlog

java -Xmx8000m -jar $ALI2DPATH/buildParams.jar ../results/${JOBID}.aln ../results/${JOBID}.mainlog > ../results/${JOBID}.results

/usr/bin/python $ALI2DPATH/viewer.py ../results/${JOBID}.results ../results/${JOBID}.results_colorC color true
/usr/bin/python $ALI2DPATH/viewer.py ../results/${JOBID}.results ../results/${JOBID}.results_color color false
/usr/bin/python $ALI2DPATH/viewer.py ../results/${JOBID}.results ../results/${JOBID}.results_bw bw false
