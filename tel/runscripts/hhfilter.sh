JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)
FORMAT=$(head -1 ../params/alignment | egrep ^CLUSTAL | wc -l)

if [ ${CHAR_COUNT} -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

if [ ${SEQ_COUNT} = "0" ] && [ ${FORMAT} = "0" ] ; then
      echo "#Invalid input format. Input should be in aligned FASTA/CLUSTAL format." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

if [ ${FORMAT} = "1" ] ; then

      OUTFORMAT=$(reformatValidator.pl clu fas \
	        $(readlink -f ../params/alignment) \
            $(readlink -f ../params/alignment) \
            -d 160 -uc -l 32000)
else
      OUTFORMAT=$(reformatValidator.pl fas fas \
	        $(readlink -f ../params/alignment) \
            $(readlink -f ../params/alignment) \
            -d 160 -uc -l 32000)
fi

if [ "${OUTFORMAT}" = "fas" ] ; then

    SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
    echo "#Read ${SEQ_COUNT} sequences." >> ../results/process.log
    curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

else
    echo "#Input is not in aligned FASTA/CLUSTAL format." >> ../results/process.log
    curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
    false
fi
echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

if [ ${SEQ_COUNT} -gt "2000" ] ; then
      echo "#Input contains more than 2000 sequences." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

echo "#Starting HHfilter." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

hhfilter        -i %alignment.path \
                -o ../results/alignment.a3m \
                -id %max_seqid.content \
                -qid %min_seqid_query.content \
                -cov %min_query_cov.content \
                -diff %num_seqs_extract \
                -M 30

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

reformat_hhsuite.pl a3m fas ../results/alignment.a3m ../results/alignment.fas

reformat_hhsuite.pl fas clu ../results/alignment.fas ../results/alignment.clustalw_aln

# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json