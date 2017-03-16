if [ %output_order.content == "input"] ; then
    muscle -in %alignment.path \
            -out ../results/alignment.fas \
            -maxiters %maxrounds.content \
            -quiet \
            -stable
else
    muscle -in %alignment.path \
            -out ../results/alignment.fas \
            -maxiters %maxrounds.content \
            -quiet
fi

reformat_hhsuite.pl fas clu ../results/alignment.fas  ../results/alignment.clustalw_aln

# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json