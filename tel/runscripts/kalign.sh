kalign -i %alignment.path \
       -o ../results/alignment.clustalw_aln \
       -s %gap_open.content \
       -e %gap_ext_kaln.content \
       -t %gap_term.content \
       -m %bonusscore.content \
       -c input \
       -f clu

reformat_hhsuite.pl clu fas ../results/alignment.clustalw_aln ../results/alignment.fas
