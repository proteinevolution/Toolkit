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
