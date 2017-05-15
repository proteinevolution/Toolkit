JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)
CHAR_COUNT=$(wc -m < ../params/alignment)


if [ $CHAR_COUNT -gt "30000" ] ; then

      echo "#Input contains more than 30000 characters." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      echo "error" >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      $?=1
fi

if [ $SEQ_COUNT -eq "0" ] ; then

      echo "#Invalid input format. Input should be in FASTA format." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      echo "error" >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      $?=1
fi

if [ $SEQ_COUNT -gt "1" ] ; then

      echo "#Input may not contain more than 1 DNA sequence." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      echo "error" >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      $?=1
fi

java -Xmx4G -cp $BIOPROGS/tools/sixframe translate \
                      -i %alignment.path \
                      -o ../results/${JOBID}.out \
                      -seq %inc_nucl.content \
                      -mode %codon_table.content \
                      -annot %amino_nucl_rel.content &> ../results/status.log
