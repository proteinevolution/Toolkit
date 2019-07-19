SEQ_COUNT=$(wc -l < ../params/alignment)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [ ${CHAR_COUNT} -gt "500" ] ; then
      echo "#Input may not contain more than 500 characters." >> ../results/process.log
      false
fi

if [ ${SEQ_COUNT} -gt "0" ] ; then
      echo "#Invalid input. Please enter a PROSITE grammar or a regular expression." >> ../results/process.log
      false
fi

PATTERN=$(less ../params/alignment)

echo "#Searching %patsearchdb.content DB for sequences '${PATTERN}' pattern." >> ../results/process.log

patsearch.pl        -i  %alignment.path \
                    -d %STANDARD/%patsearchdb.content \
                    -o ../results/results.json \
                    -sc %seqcount.content \
                    -%grammar.content > report_patsearch
echo "done" >> ../results/process.log

echo "#Generating output." >> ../results/process.log

echo "done" >> ../results/process.log
