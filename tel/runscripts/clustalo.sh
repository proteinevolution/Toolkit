if [ %output_order.content == "input"] ; then

    clustalo   -i %alignment.path \
           -o ../results/alignment.clustalw_aln \
           --outfmt=clustal \
           -v \
           --force \
           --output-order=input-order \
           --threads=%THREADS
else
    clustalo   -i %alignment.path \
           -o ../results/alignment.clustalw_aln \
           --outfmt=clustal \
           -v \
           --force \
           --output-order=tree-order \
           --threads=%THREADS
fi

reformat_hhsuite.pl clu fas ../results/alignment.clustalw_aln ../results/alignment.fas
# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json
