SEQ_COUNT=$(wc -l < ../params/alignment)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [ ${SEQ_COUNT} -gt "20000" ] ; then
      echo "#Input contains more than 20000 identifiers." >> ../results/process.log
      false
fi

if [ ${CHAR_COUNT} -gt "1000000" ] ; then
      echo "#Input may no contain more than 1000000 characters." >> ../results/process.log
      false
fi

echo "#Retrieving sequences from the %standarddb.content database." >> ../results/process.log

seq_retrieve.pl -i %alignment.path \
                -o ../results/sequences.fa \
                -d %STANDARD/%standarddb.content \
                -unique T > ../results/unretrievable

reformat_hhsuite.pl fas ufas \
            $(readlink -f ../results/sequences.fa) \
            $(readlink -f ../results/sequences.fa) \
            -d 100 -uc -l 32000

echo "done" >> ../results/process.log