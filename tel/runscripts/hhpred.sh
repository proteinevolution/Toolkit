

if [ %msageneration.content == "hhblits" ] ; then 

    hhblits -cpu 8 \
            -v 2 \
            -i %alignment.path \
            -d %UNIPROT  \
            -o ../results/msagen.hhblits \
            -oa3m query.a3m \
            -n %msa_gen_max_iter.content \
            -mact 0.35
fi


