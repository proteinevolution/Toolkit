#Check is MSA generation is required

#CHECK IF MSA generation is required or not
if [ %msa_gen_max_iter.content == "0" ] ; then
        reformat_hhsuite.pl fas a3m %alignment.path query.a3m -M first
        mv query.a3m ../results/query.a3m
else
    #MSA generation required
    #MSA generation by HHblits
    hhblits -cpu 8 \
            -v 2 \
            -i %alignment.path \
            -d %UNIPROT  \
            -o ../results/msagen.hhblits \
            -oa3m ../results/query.a3m \
            -n %msa_gen_max_iter.content \
            -mact 0.35
fi

#100 maximally diverse sequences
hhfilter -i ../results/query.a3m \
         -o ../results/query.reduced.a3m \
         -diff 100

#max. 160 characters in description
reformat_hhsuite.pl a3m fas \
         ../results/query.reduced.a3m \
         ../results/query.fas \
         -d 160

hhfilter -i ../results/query.a3m \
         -o ../results/query.reduced.a3m \
         -diff 50

reformat_hhsuite.pl a3m fas \
         ../results/query.reduced.a3m \
         ../results/query.reduced.fas \
         -r


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

#ruby $HHREPIDPATH/script/graphrepeats -i ../results/query.hhrepid \
#                                -q ../results/query.fas \
#                                -o ../results/query.png \
#                                -m ../results/query.map
