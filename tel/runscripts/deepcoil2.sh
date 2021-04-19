SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [[ ${CHAR_COUNT} -gt "5000" ]] ; then
      echo "#Input may not contain more than 5000 characters." >> ../results/process.log
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

      if [[ ${CHAR_COUNT} -gt "5000" ]] ; then
            echo "#Input may not contain more than 5000 characters." >> ../results/process.log
            false
      else
            sed -i "1 i\>${JOBID}" ../params/alignment
      fi
fi

cp %alignment.path ../results/${JOBID}.fas
reformat_hhsuite.pl fas fas ../results/${JOBID}.fas ../results/${JOBID}.fas -l 5000 -uc

sed -n '2p' ../results/${JOBID}.fas > ../results/tmp
sed 's/[\.\-]//g' ../results/tmp > ../results/${JOBID}.fas
CHAR_COUNT=$(wc -m < ../results/${JOBID}.fas)
sed -i "1 i\>${JOBID}" ../results/${JOBID}.fas
rm ../results/tmp

echo "#Query is a protein sequence with ${CHAR_COUNT} residues." >> ../results/process.log
echo "done" >> ../results/process.log


if [[ ${CHAR_COUNT} -lt "20" ]] || [[ ${CHAR_COUNT} -gt "5000" ]] ; then
    echo "#Input sequence should be between 20 and 5000." >> ../results/process.log
    false
fi

source $DEEPCOIL2/dc2-env/bin/activate

echo "#Running DeepCoil2." >> ../results/process.log

deepcoil -i ../results/${JOBID}.fas \
         -out_path ../results/ \
         -n_cpu %THREADS \
         --plot \
         --dpi 80

mv ../results/${JOBID}.png ../results/img_deepcoil.png

echo "done" >> ../results/process.log