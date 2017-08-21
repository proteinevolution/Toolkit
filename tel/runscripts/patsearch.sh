SEQ_COUNT=$(wc -l < ../params/alignment)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [ ${CHAR_COUNT} -gt "100" ] ; then
      echo "#Input may not contain more than 100 characters." >> ../results/process.log
      updateProcessLog
      false
fi

if [ ${SEQ_COUNT} -gt "0" ] ; then
      echo "#Invalid input. Please enter a PROSITE grammar or a regular expression." >> ../results/process.log
      updateProcessLog
      false
fi

PATTERN=$(less ../params/alignment)

echo "#Searching %standarddb.content DB for sequences '${PATTERN}' pattern." >> ../results/process.log
updateProcessLog

patsearch.pl        -i  %alignment.path \
                    -d %STANDARD/%patsearchdb.content \
                    -o ../results/${JOBID}.json \
                    -sc %seqcount.content \
                    -%grammar.content > report_patsearch
echo "done" >> ../results/process.log
updateProcessLog

echo "#Generating output." >> ../results/process.log
updateProcessLog

echo "done" >> ../results/process.log
updateProcessLog
