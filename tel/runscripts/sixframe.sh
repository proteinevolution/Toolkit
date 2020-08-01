SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [[ ${CHAR_COUNT} -gt "30000" ]] ; then
      echo "#Input may not contain more than 30000 characters." >> ../results/process.log
      false
fi

if [[ ${FORMAT} = "1" ]] || [[ ${SEQ_COUNT} -gt "1" ]] ; then
      echo "#Input is a multiple sequence alignment; expecting a single DNA sequence." >> ../results/process.log
      false
fi

if [[ ${SEQ_COUNT} = "0" ]] ; then
      sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
      perl -pe 's/\s+//g' ../params/alignment1 > ../params/alignment
      CHAR_COUNT=$(wc -m < ../params/alignment)

      if [[ ${CHAR_COUNT} -gt "30000" ]] ; then
            echo "#Input may not contain more than 30000 characters." >> ../results/process.log
            false
      else
            sed -i "1 i\>${JOBID}" ../params/alignment
      fi
fi

echo "#Query is a protein sequence with ${CHAR_COUNT} residues." >> ../results/process.log

echo "done" >> ../results/process.log

echo "#Running 6FrameTranslation on the input DNA sequence." >> ../results/process.log

java -Xmx256m -cp ${BIOPROGS}/tools/sixframe translate \
                      -i %alignment.path \
                      -o ../results/${JOBID}.out \
                      -seq %inc_nucl.content \
                      -mode %codon_table.content \
                      -annot %amino_nucl_rel.content &> ../results/status.log

echo "done" >> ../results/process.log
