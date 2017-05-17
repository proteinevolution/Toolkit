SEQ_COUNT=$(wc -l < ../params/alignment)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [ $CHAR_COUNT -gt "100" ] ; then
      echo "#Input may not contain more than 100 characters." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

if [ $SEQ_COUNT -gt "0" ] ; then
      echo "#Invalid input. Please enter a PROSITE grammar or a regular expression." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

PATTERN=$(less ../params/alignment)

echo "#Searching %standarddb.content DB for sequences '${PATTERN}' pattern." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

patsearch.pl        -i  %alignment.path \
                    -d %STANDARD/%standarddb.content \
                    -o ../results/output.json \
                    -sc %seqcount.content \
                    -%grammar.content > report_patsearch
echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Generating output." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
