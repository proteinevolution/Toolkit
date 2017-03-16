if [ %output_order.content == "input"] ; then
    kalign -i %alignment.path \
            -o ../results/alignment.clustalw_aln \
            -s %gap_open.content \
            -e %gap_ext_kaln.content \
            -t %gap_term.content \
            -m %bonusscore.content \
            -c input \
            -f clu
else
    kalign -i %alignment.path \
            -o ../results/alignment.clustalw_aln \
            -s %gap_open.content \
            -e %gap_ext_kaln.content \
            -t %gap_term.content \
            -m %bonusscore.content \
            -c tree \
            -f clu
fi

reformat_hhsuite.pl clu fas ../results/alignment.clustalw_aln ../results/alignment.fas
# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json