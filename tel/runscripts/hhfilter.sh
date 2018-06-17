SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)
A3M_INPUT=$(head -1 ../params/alignment | egrep "^#A3M#" | wc -l)


if [ ${CHAR_COUNT} -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      false
fi

if [ ${A3M_INPUT} = "1" ] ; then

    sed -i '1d' ../params/alignment

    reformatValidator.pl a3m fas \
           $(readlink -f ../params/alignment) \
           $(readlink -f ../params/alignment.tmp) \
           -d 160 -uc -l 32000

     if [ ! -f ../params/alignment.tmp ]; then
            echo "#Input is not in valid A3M format." >> ../results/process.log
            false
     else
            echo "#Query is in A3M format." >> ../results/process.log
            mv ../params/alignment.tmp ../params/alignment
            echo "done" >> ../results/process.log
     fi
fi

if [ ${SEQ_COUNT} = "0" ] && [ ${FORMAT} = "0" ] ; then
      echo "#Invalid input format. Input should be in aligned FASTA/CLUSTAL format." >> ../results/process.log
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

else
    echo "#Input is not in aligned FASTA/CLUSTAL format." >> ../results/process.log
    false
fi
echo "done" >> ../results/process.log

if [ ${SEQ_COUNT} -gt "10000" ] ; then
      echo "#Input contains more than 10000 sequences." >> ../results/process.log
      false
fi

echo "#Starting HHfilter." >> ../results/process.log

hhfilter        -i %alignment.path \
                -o ../results/alignment.a3m \
                -id %max_seqid.content \
                -qid %min_seqid_query.content \
                -cov %min_query_cov.content \
                -diff %num_seqs_extract \
                -M 30

echo "done" >> ../results/process.log

reformat_hhsuite.pl a3m fas ../results/alignment.a3m ../results/alignment.fas

reformat_hhsuite.pl fas clu ../results/alignment.fas ../results/alignment.clustalw_aln

# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json

rm ../results/alignment.a3m