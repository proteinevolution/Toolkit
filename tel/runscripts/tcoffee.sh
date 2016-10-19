#% sequences : FAS      

export DIR_4_TCOFFEE=./tmp  
export TMP_4_TCOFFEE=./tmp
export CACHE_4_TCOFFEE=./tmp


%BIOPROGS/tools/tcoffee/bin/t_coffee \
                   -in %alignment.path \
                   -cache=no \
                   -output clustalw_aln score_pdf score_html

mv alignment.* results/

