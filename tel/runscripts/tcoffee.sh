JOBID=%jobid.content
export DIR_4_TCOFFEE=./tmp  
export TMP_4_TCOFFEE=./tmp
export CACHE_4_TCOFFEE=./tmp


if [ %output_order.content == "input"] ; then
        t_coffee -in %alignment.path \
                -cache=no \
                -outorder=input \
                -output clustalw_aln score_pdf score_html
else
        t_coffee -in %alignment.path \
                -cache=no \
                -outorder=aligned \
                -output clustalw_aln score_pdf score_html
fi


mv alignment.* ../results/


reformat_hhsuite.pl clu fas ../results/alignment.clustalw_aln ../results/alignment.fas

# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json