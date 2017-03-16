

if [ %output_order.content == "input"] ; then

    mafft --op %gap_open.content \
          --ep %offset.content \
          --quiet \
          %alignment.path > ../results/alignment.fas
else
    mafft --op %gap_open.content \
          --ep %offset.content \
          --quiet \
          --reorder \
          %alignment.path > ../results/alignment.fas
fi

# Convert FASTA to CLUSTAL
reformat_hhsuite.pl fas clu ../results/alignment.fas ../results/alignment.clustalw_aln

# Convert FASTA to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json