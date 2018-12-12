SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [ ${CHAR_COUNT} -gt "10000" ] ; then
      echo "#Input may not contain more than 10000 characters." >> ../results/process.log
      false
fi

if [ ${FORMAT} = "1" ] || [ ${SEQ_COUNT} -gt "1" ] ; then
      echo "#Input is a multiple sequence alignment; expecting a single protein sequence." >> ../results/process.log
      false
fi

if [ ${SEQ_COUNT} = "0" ] ; then
      sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
      perl -pe 's/\s+//g' ../params/alignment1 > ../params/alignment
      CHAR_COUNT=$(wc -m < ../params/alignment)

      if [ ${CHAR_COUNT} -gt "10000" ] ; then
            echo "#Input may not contain more than 10000 characters." >> ../results/process.log
            false
      else
            sed -i "1 i\>${JOBID}" ../params/alignment
      fi
fi

echo "#Query is a protein sequence with ${CHAR_COUNT} residues." >> ../results/process.log

echo "done" >> ../results/process.log


if [ %codon_table_organism.content = "" ] || [ %codon_table_organism.content = "false" ] ; then
        backtranslate.pl  -o=../results/${JOBID}.out \
                          -i=%alignment.path \
                          -oformat=%inc_amino.content \
                          -g=%genetic_code.content
else
        findOrganism.pl -i "%codon_table_organism.content" \
                        -d ${BACKTRANSLATORPATH}/CUT_database \
                        -o ../results/${JOBID}.org

       if grep -q "not found in database" ../results/${JOBID}.org ; then

            mv ../results/${JOBID}.org ../results/${JOBID}.out
        else
            makeCUT.pl -i ../results/${JOBID}.org \
                     -o ../results/${JOBID}.cut \
                     -d ${BACKTRANSLATORPATH}/CUT_database

            backtranslate.pl  -o=../results/${JOBID}.out \
                          -i=%alignment.path \
                          -oformat=%inc_amino.content \
                          -g=%genetic_code.content \
                          -c=../results/${JOBID}.cut \
                          -cformat=1
        fi
fi