#!/bin/bash
#export PATH=$PATH:%{CLUSTALW}

trap 'kill $(jobs -p)' EXIT

export MAFFT_BINARIES='!{BIO}/tools/mafft2/binaries'

!{BIO}/tools/mafft2/scripts/mafft \
--op ${gapopen} \
--ep ${offset} %{sequences} > results/out
