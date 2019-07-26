SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [[ ${CHAR_COUNT} -gt "10000" ]] ; then
      echo "#Input may not contain more than 10000 characters." >> ../results/process.log
      false
fi

if [[ ${FORMAT} = "1" ]] || [[ ${SEQ_COUNT} -gt "1" ]] ; then
      echo "#Input is a multiple sequence alignment; expecting a single protein sequence." >> ../results/process.log
      false
fi

if [[ ${SEQ_COUNT} = "0" ]] ; then
      sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
      perl -pe 's/\s+//g' ../params/alignment1 > ../params/alignment
      CHAR_COUNT=$(wc -m < ../params/alignment)

      if [[ ${CHAR_COUNT} -gt "10000" ]] ; then
            echo "#Input may not contain more than 10000 characters." >> ../results/process.log
            false
      else
            sed -i "1 i\>${JOBID}" ../params/alignment
      fi
fi

echo "#Query is a protein sequence with ${CHAR_COUNT} residues." >> ../results/process.log
echo "done" >> ../results/process.log


reformat_hhsuite.pl fas fas %alignment.path ${JOBID}.fas -l 32000 -uc

mv ${JOBID}.fas ../results

tprpred_wrapper.pl -in ../results/${JOBID}.fas \
                   -v 1 \
                   -cut %eval_tpr.content > ../results/${JOBID}.tpr

#convert TPRpred output to JSON
tprpred2json.pl ../results/${JOBID}.tpr > ../results/results.json