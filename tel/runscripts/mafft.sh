#!/bin/bash
#export PATH=$PATH:%{CLUSTALW}

trap 'kill $(jobs -p)' EXIT

export MAFFT_BINARIES='%BIOPROGS/tools/mafft2/binaries'

%BIOPROGS/tools/mafft2/scripts/mafft \
                           --op %gapopen.content \
                           --ep %offset.content \
                            %sequences.path  > results/out
