JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)


if [ $SEQ_COUNT -gt "1" ] ; then
       echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
       curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
else
       echo "#Query is a single protein sequence." >> ../results/process.log
       curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
fi

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

#CHECK IF MSA generation is required or not
if [ %msa_gen_max_iter.content == "0" ] && [ ${SEQ_COUNT} -gt "1" ] ; then
        echo "#No MSA generation required." >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

        reformat_hhsuite.pl fas a3m %alignment.path query.a3m -M first
        mv query.a3m ../results/query.a3m

else
    #MSA generation required
    #MSA generation by HHblits
    echo "#MSA generation required. Running %msa_gen_max_iter.content iteration(s) of HHblits." >> ../results/process.log
    curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
    hhblits -cpu 8 \
            -v 2 \
            -i %alignment.path \
            -d %UNIPROT  \
            -o ../results/msagen.hhblits \
            -oa3m ../results/query.a3m \
            -n %msa_gen_max_iter.content \
            -mact 0.35

fi

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

#100 maximally diverse sequences
hhfilter -i ../results/query.a3m \
         -o ../results/query.reduced.a3m \
         -diff 100

#max. 160 characters in description
reformat_hhsuite.pl a3m fas \
         ../results/query.reduced.a3m \
         ../results/query.fas \
         -d 160

echo "#Running HHblits." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

hhrepid -qsc 0.$i \
        -i ../results/query.a3m \
        -o ../results/query.hhrepid \
        -d $HHREPIDPATH/cal_small.hhm \
        -pdir ../results \
        -tp $HHREPIDPATH/tp.dat \
        -fp $HHREPIDPATH/fp.dat \
        -P %self_aln_pval_threshold.content \
        -T %rep_pval_threshold.content \
        -mrgr %merge_iters.content \
        -ssm %score_ss.content \
        -mapt1 %mac_cutoff.content \
        -mapt2 %mac_cutoff.content \
        -mapt3 %mac_cutoff.content \
        -domm %domain_bound_detection.content

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1


#ruby $HHREPIDPATH/script/graphrepeats -i ../results/query.hhrepid \
#                                -q ../results/query.fas \
#                                -o ../results/query.png \
#                                -m ../results/query.map
