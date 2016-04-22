#!/bin/bash
#export PATH=$PATH:%{CLUSTALW}

trap 'kill $(jobs -p)' EXIT

export MAFFT_BINARIES='!{BIO}/tools/mafft/binaries'

!{BIO}/tools/mafft/scripts/mafft \
--op ${gapopen} \
--ep ${offset} \
#{alnorder} \
#{clustalw} \
%{sequences} > \
results/out
