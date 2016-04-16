#!/bin/bash
#export PATH=$PATH:%{CLUSTALW}

trap 'kill $(jobs -p)' EXIT

export DIR_4_TCOFFEE=./specific
export TMP_4_TCOFFEE=./specific
export CACHE_4_TCOFFEE=./specific

!{BIO}/tools/tcoffee/bin/t_coffee -in %{sequences} -cache=no -output clustalw_aln score_pdf score_html

mv *.dnd results/
mv *.score_html results/
mv *.score_html results/
mv *.score_pdf results/
mv *.clustalw_aln results/
