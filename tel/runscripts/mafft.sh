#!/bin/bash
#export PATH=$PATH:%{CLUSTALW}

trap 'kill $(jobs -p)' EXIT

export MAFFT_BINARIES='%BIOPROGS/tools/mafft2/binaries'

%BIOPROGS/tools/mafft2/scripts/mafft \
                           --op %gap_open.content \
                           --ep %offset.content \
                            %alignment.path  > results/out
