JOBID=%jobid.content

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