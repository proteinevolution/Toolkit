JOBID=%jobid.content

aln2plot.pl %alignment.path

mv -- -1.png ../results/${JOBID}_hyd.png
mv -- -2.png ../results/${JOBID}_scvol.png


