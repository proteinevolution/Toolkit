A3M_INPUT=$(head -1 ../params/alignment | egrep "^#A3M#" | wc -l)
SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

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
            cp ../params/alignment ../results/${JOBID}.in.a3m
            rm ../params/alignment.tmp
            echo "done" >> ../results/process.log
     fi
else

    if [ ${SEQ_COUNT} = "0" ] && [ ${FORMAT} = "0" ] ; then
          sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
          CHAR_COUNT=$(wc -m < ../params/alignment1)

          if [ ${CHAR_COUNT} -gt "10000" ] ; then
                echo "#Single protein sequence inputs may not contain more than 10000 characters." >> ../results/process.log
                false
          else
                sed -i "1 i\>Q_${JOBID}" ../params/alignment1
                mv ../params/alignment1 ../params/alignment
          fi
    fi

    if [ ${FORMAT} = "1" ] ; then
          reformatValidator.pl clu a3m \
                $(readlink -f %alignment.path) \
                $(readlink -f ../results/${JOBID}.in.a3m) \
                -d 160 -l 32000
    else
          reformatValidator.pl fas a3m \
                $(readlink -f %alignment.path) \
                $(readlink -f ../results/${JOBID}.in.a3m) \
                -d 160 -l 32000
    fi

    if [ ! -f ../results/${JOBID}.in.a3m ]; then
        echo "#Input is not in aligned FASTA/CLUSTAL format." >> ../results/process.log
        false
    fi
fi

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.in.a3m | wc -l)

if [ ${SEQ_COUNT} -gt "10000" ] ; then
      echo "#Input contains more than 10000 sequences." >> ../results/process.log
      false
fi

if [ ${SEQ_COUNT} -gt "1" ] ; then
       echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
else
       echo "#Query is a single protein sequence." >> ../results/process.log
fi

echo "done" >> ../results/process.log

reformatValidator.pl a3m fas \
       $(readlink -f ../results/${JOBID}.in.a3m) \
       $(readlink -f ../results/${JOBID}.fas) \
       -d 160 -uc -l 32000

head -n 2 ../results/${JOBID}.fas > ../results/firstSeq0.fas
sed 's/[\.\-]//g' ../results/firstSeq0.fas > ../results/firstSeq.fas
rm ../results/firstSeq0.fas ../results/${JOBID}.fas



#CHECK IF MSA generation is required or not
if [ "%msa_gen_max_iter.content" = "0" ] && [ ${SEQ_COUNT} -gt "1" ] ; then
        echo "#No MSA generation required for building A3M." >> ../results/process.log
        hhfilter -i ../results/${JOBID}.in.a3m \
                 -o ../results/${JOBID}.a3m \
                 -cov %min_cov.content\
                 -qid %min_seqid_query.content

        echo "done" >> ../results/process.log
else
    #MSA generation required
    echo "#Query MSA generation required." >> ../results/process.log
    echo "done" >> ../results/process.log

    if [ %msa_gen_max_iter.content -lt "2" ] ; then
        ITERS=1
    else
        ITERS=%msa_gen_max_iter.content
    fi

    #MSA generation by HHblits
        echo "#Running ${ITERS} iteration(s) of HHblits for query MSA and A3M generation." >> ../results/process.log

        hhblits -cpu %THREADS \
                -v 2 \
                -e %hhpred_incl_eval.content \
                -i ../results/${JOBID}.in.a3m \
                -M first \
                -d %UNICLUST  \
                -oa3m ../results/${JOBID}.a3m \
                -n -${ITERS} \
                -qid %min_seqid_query.content \
                -cov %min_cov.content \
                -mact 0.35

        echo "done" >> ../results/process.log

fi

#Generate representative MSA for forwarding

hhfilter -i ../results/${JOBID}.a3m \
         -o ../results/reduced.a3m \
         -diff 100

sed -i "1 i\#A3M#" ../results/reduced.a3m

addss.pl ../results/${JOBID}.a3m


echo "#Searching profile HMM database(s)." >> ../results/process.log

${HHOMPPATH}/hhmake -v 1 -cov 20 -qid 0 -diff 100 \
                    -i ../results/${JOBID}.a3m \
                    -o ../results/${JOBID}.hhm

${HHOMPPATH}/hhomp -cpu %THREADS \
                   -i ../results/${JOBID}.hhm \
                   -d ${HHOMPPATH}/cal.hhm \
                   -cal -local

${HHOMPPATH}/hhomp -cpu %THREADS \
                   -i ../results/${JOBID}.hhm \
                   -d ${HHOMPDBPATH}/%hhompdb.content \
                   -o ../results/${JOBID}.hhr \
                   -p %pmin.content \
                   -P %pmin.content \
                   -Z %desc.content \
                   -%alignmode.content \
                   -B %desc.content \
                   -seq 1 \
                   -b 1

echo "done" >> ../results/process.log

echo "#Preparing output." >> ../results/process.log

hhviz.pl ${JOBID} ../results/ ../results/  &> /dev/null


# Generate Hitlist in JSON for hhrfile
${HHOMPPATH}/hhomp_hhr2json.py "$(readlink -f ../results/${JOBID}.hhr)" > ../results/${JOBID}.json

# add DB to json
manipulate_json.py -k 'db' -v '%hhompdb.content' ../results/${JOBID}.json

# Generate Query in JSON
fasta2json.py ../results/firstSeq.fas ../results/query.json

echo "done" >> ../results/process.log