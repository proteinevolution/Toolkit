SEQ_COUNT=$(wc -l < ../params/alignment)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [[ ${SEQ_COUNT} -gt "100000" ]] ; then
      echo "#Input contains more than 100000 sequences/headers." >> ../results/process.log
      false
fi

if [[ ${CHAR_COUNT} -gt "10000000" ]] ; then
      echo "#Input may no contain more than 10000000 characters." >> ../results/process.log
      false
fi

echo "#Extracting identifiers." >> ../results/process.log

seq2id.pl       %alignment.path \
                ../results/ids.json

echo "done" >> ../results/process.log