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

      echo "#Input may not contain more than 1 protein sequence." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      echo "error" >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      $?=1
fi

if [ "%codon_table_organism.content" == "" ] ; then
        backtranslate.pl  -o=../results/${JOBID}.out \
                          -i=%alignment.path \
                          -oformat=%inc_amino.content \
                          -g=%genetic_code.content

else
        findOrganism.pl -i "%codon_table_organism.content" \
                        -d $BACKTRANSLATORPATH/CUT_database \
                        -o ../results/${JOBID}.org

       if grep -q "not found in database" ../results/${JOBID}.org ; then

            mv ../results/${JOBID}.org ../results/${JOBID}.out
        else
            makeCUT.pl -i ../results/${JOBID}.org \
                     -o ../results/${JOBID}.cut \
                     -d $BACKTRANSLATORPATH/CUT_database

            backtranslate.pl  -o=../results/${JOBID}.out \
                          -i=%alignment.path \
                          -oformat=%inc_amino.content \
                          -g=%genetic_code.content \
                          -c=../results/${JOBID}.cut \
                          -cformat=1
        fi
fi