SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)
A3M_INPUT=$(head -1 ../params/alignment | egrep "^#A3M#" | wc -l)

if [ ${CHAR_COUNT} -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      updateProcessLog
      false
fi

if [ ${A3M_INPUT} = "1" ] ; then

    sed -i '1d' ../params/alignment
    cp ../params/alignment ../results/${JOBID}.a3m

    reformatValidator.pl a3m fas \
           $(readlink -f ../results/${JOBID}.a3m) \
           $(readlink -f ../params/alignment.tmp) \
           -d 160 -l 32000

     if [ ! -f ../params/alignment.tmp ]; then
            echo "#Input is not in valid A3M format." >> ../results/process.log
            updateProcessLog
            false
     else
            echo "#Query is in A3M format." >> ../results/process.log
            updateProcessLog
            rm ../params/alignment.tmp
            echo "done" >> ../results/process.log
            updateProcessLog
     fi
else

    if [ ${SEQ_COUNT} = "0" ] && [ ${FORMAT} = "0" ] ; then
          sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
          CHAR_COUNT=$(wc -m < ../params/alignment1)

          if [ ${CHAR_COUNT} -gt "10000" ] ; then
                echo "#Single protein sequence inputs may not contain more than 10000 characters." >> ../results/process.log
                updateProcessLog
                false
          else
                sed -i "1 i\>${JOBID}" ../params/alignment1
                mv ../params/alignment1 ../params/alignment
          fi
    fi

    if [ ${FORMAT} = "1" ] ; then
          reformatValidator.pl clu a3m \
                $(readlink -f %alignment.path) \
                $(readlink -f ../results/${JOBID}.a3m) \
                -d 160 -l 32000
    else
          reformatValidator.pl fas a3m \
                $(readlink -f %alignment.path) \
                $(readlink -f ../results/${JOBID}.a3m) \
                -d 160 -l 32000
    fi

    if [ ! -f ../results/${JOBID}.a3m ]; then
        echo "#Input is not in aligned FASTA/CLUSTAL format." >> ../results/process.log
        updateProcessLog
        false
    fi
fi

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.a3m | wc -l)

if [ ${SEQ_COUNT} -gt "10000" ] ; then
      echo "#Input contains more than 10000 sequences." >> ../results/process.log
      updateProcessLog
      false
fi

if [ ${SEQ_COUNT} -gt "1" ] ; then
       echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
       updateProcessLog
else
       echo "#Query is a single protein sequence." >> ../results/process.log
       updateProcessLog
fi
echo "done" >> ../results/process.log
updateProcessLog

#CHECK IF MSA generation is required or not
if [ %msa_gen_max_iter.content == "0" ] && [ ${SEQ_COUNT} -gt "1" ] ; then
        echo "#No MSA generation required." >> ../results/process.log
        updateProcessLog

        mv ../results/${JOBID}.a3m ../results/query.a3m
else
    #MSA generation required
    #MSA generation by HHblits
    if [ %msa_gen_max_iter.content -lt "2" ] ; then
        echo "#MSA generation required. Running 1 iteration of HHblits." >> ../results/process.log
        ITERS=1
    else
        echo "#MSA generation required. Running %msa_gen_max_iter.content iterations of HHblits." >> ../results/process.log
        ITERS=%msa_gen_max_iter.content
    fi

    updateProcessLog
    hhblits -cpu  %THREADS \
            -v 2 \
            -i  ../results/${JOBID}.a3m \
            -d %UNIPROT  \
            -o /dev/null \
            -oa3m ../results/query.a3m \
            -n ${ITERS} \
            -mact 0.35
fi

echo "done" >> ../results/process.log
updateProcessLog

echo "#Running HHrepID." >> ../results/process.log
updateProcessLog


addss.pl ../results/query.a3m

#100 maximally diverse sequences
hhfilter -i ../results/query.a3m \
         -o ../results/query.reduced.a3m \
         -diff 100
cp ../results/query.reduced.a3m ../results/query.a3m

reformat_hhsuite.pl a3m a3m \
         $(readlink -f ../results/query.reduced.a3m) \
         $(readlink -f ../results/reduced.a3m) \
         -d 160 -noss
sed -i "1 i\#A3M#" ../results/reduced.a3m


hhrepid -qsc 0.2 \
        -i ../results/query.a3m \
        -o ../results/query.hhrepid \
        -d ${HHREPIDPATH}/cal_small.hhm \
        -pdir ../results \
        -tp ${HHREPIDPATH}/tp.dat \
        -fp ${HHREPIDPATH}/fp.dat \
        -P %self_aln_pval_threshold.content \
        -T %rep_pval_threshold.content \
        -mrgr %merge_iters.content \
        -ssm %score_ss.content \
        -mapt1 %mac_cutoff.content \
        -mapt2 %mac_cutoff.content \
        -mapt3 %mac_cutoff.content \
        -domm %domain_bound_detection.content

echo "done" >> ../results/process.log
updateProcessLog


rm ../results/query.reduced.a3m ../results/query.a3m
