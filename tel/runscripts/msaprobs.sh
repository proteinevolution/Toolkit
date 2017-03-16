if [ %output_order.content == "input"] ; then
    msaprobs %alignment.path \
                -num_threads %THREADS \
                -o ../results/alignment.fas
else
    msaprobs %alignment.path \
               -num_threads %THREADS \
               -a \
               -o ../results/alignment.fas
fi

reformat_hhsuite.pl fas clu ../results/alignment.fas ../results/alignment.clustalw_aln
# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json
