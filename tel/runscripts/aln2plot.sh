SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [ ${CHAR_COUNT} -gt "1000000" ] ; then
      echo "#Input may not contain more than 1000000 characters." >> ../results/process.log
      false
fi

if [ ${SEQ_COUNT} = "0" ] && [ ${FORMAT} = "0" ] ; then
      echo "#Invalid input format. Input should be in aligned FASTA/CLUSTAL format." >> ../results/process.log
      false
fi

if [ ${FORMAT} = "1" ] ; then
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
    false
fi

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.aln | wc -l)

if [ ${SEQ_COUNT} -gt "2000" ] ; then
      echo "#Input contains more than 2000 sequences." >> ../results/process.log
      false
fi

if [ ${SEQ_COUNT} -gt "1" ] ; then
       echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
else
       echo "#Query is a single protein sequence. Please input an alignment." >> ../results/process.log
       false
fi
echo "done" >> ../results/process.log

aln2plot.pl ../results/${JOBID}.aln
mv -- ../results/${JOBID}-1.png ../results/${JOBID}_hyd.png
mv -- ../results/${JOBID}-2.png ../results/${JOBID}_scvol.png


