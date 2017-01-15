

if [ %msageneration.content == "hhblits" ] ; then 

    hhblits -cpu 8 \
            -v 2 \
            -i %alignment.path \
            -d %UNIPROT  \
            -o ../results/msagen.hhblits \
            -oa3m ../results/query.a3m \
            -n %msa_gen_max_iter.content \
            -mact 0.35

else
    if [ %msageneration.content == "psiblast" ] ; then

        buildali.pl -nodssp \
                    -cpu 4 \
                    -v 1 \
                    -n %msa_gen_max_iter.content  \
                    -diff 1000 %inclusion_ethresh #{@cov_min} \
                    -a2m #{a2mFile}

    fi
fi



# Here assume that the query alignment exists

# Perform HHsearch # TODO Include more parameters
hhsearch -cpu 4 \
         -i ../results/query.a3m \
         -d '%hhsuitedb.content'  \
         -o ../results/hhsearch.hhr \
          -z 1 \
          -b 1 \
          -dbstrlen 10000 \
          -cs ${HHLIB}/data/context_data.lib 

JOBID=%jobid.content


# Prepare FASTA files for 'Show Query Alignemt', HHviz bar graph, and HMM histograms
# Reformat query into fasta format ('full' alignment, i.e. 100 maximally diverse sequences, to limit amount of data to transfer)
#hhfilter -i ../results/query.a3m -o ../results/query.reduced.a3m -diff 100
#reformat.pl a3m fas ../results/query.reduced.a3m  ../results/query.fas -d 160 -uc   # max. 160 chars in description

#hhfilter -i ../results/query.a3m -o ../results/query.reduced.a3m -diff 50
#reformat.pl a3m fas ../results/query.reduced.a3m  ../results/query.reduced.fas -d 160 -uc   # max. 160 chars in description


# Generate input files for hhviz
cp ../results/hhsearch.hhr ../results/${JOBID}.hhr

# Generate graphical display of hits
hhviz.pl ${JOBID} ../results/ ../results/  &> /dev/null

cp ../results/${JOBID}.png ../results/hitlist.png
cp ../results/${JOBID}.html ../results/hitlist.html


