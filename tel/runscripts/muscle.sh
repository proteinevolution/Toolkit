muscle -in %alignment.path \
       -clwstrict \
       -out ../results/alignment.clustalw_aln \
       -maxiters %maxrounds.content

reformat_hhsuite.pl clu fas ../results/alignment.clustalw_aln ../results/alignment.fas