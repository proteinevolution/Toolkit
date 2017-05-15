JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [ $CHAR_COUNT -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

if [ $SEQ_COUNT = "0" ] ; then
      echo "#Invalid input format. Input should be in FASTA format." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

if [ $SEQ_COUNT -lt "2" ] ; then
      echo "#Input should contain at least 2 sequences." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

OUTFORMAT=$(reformatValidator.pl fas ufas \
	        $(readlink -f ../params/alignment) \
            $(readlink -f ../params/alignment) \
            -d 160 -uc -l 32000)

if [ "$OUTFORMAT" = "ufas" ] ; then
    SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
    echo "#Read ${SEQ_COUNT} sequences." >> ../results/process.log
    curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
else
    echo "#Input is not in FASTA format." >> ../results/process.log
    curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
    false
fi
echo "done"  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

if [ $SEQ_COUNT -gt "2000" ] ; then
      echo "#Input contains more than 2000 sequences." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

echo "#Aligning sequences with MSAProbs."  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

if [ %output_order.content == "input"] ; then
    msaprobs %alignment.path \
                -num_threads %THREADS \
                -o ../results/alignment.fas
else
    msaprobs %alignment.path \
               -num_threads %THREADS \
               -a \
               -o ../results/alignment.fas
fi

echo "done"  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Preparing output." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "done"  >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

reformat_hhsuite.pl fas clu ../results/alignment.fas ../results/alignment.clustalw_aln
# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json
