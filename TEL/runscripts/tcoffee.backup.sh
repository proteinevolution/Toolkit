#!/bin/bash
#export PATH=$PATH:#{CLUSTALW}
export DIR_4_TCOFFEE=./specific
export TMP_4_TCOFFEE=./specific
export CACHE_4_TCOFFEE=./specific
../../bioprogs/tools/tcoffee/bin/t_coffee -in #{sequences} -cache=no \
	-output clustalw_aln score_pdf score_html \
#	?{mlalign_id_pair|-in Mlalign_id_pair} \
#	?{mfast_pair|-in Mfast_pair} \
#	?{mslow_pair|-in Mslow_pair}
#	#{@mclustalw_pair} \
#	
#	-quiet=stdout
mv *.dnd results/
mv *.score_html results/
mv *.score_html results/
mv *.score_pdf results/
mv *.clustalw_aln results/
