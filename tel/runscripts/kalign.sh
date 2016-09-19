#!/bin/bash

trap 'kill $(jobs -p)' EXIT

%BIOPROGS/tools/kalign/kalign -i %sequences.path \
                                   -o results/kalign_aln \
                                   -s %gapopen.content \
                                   -e %gapextension.content \
                                   -t %termgap.content \
                                   -m %bonusscore.content \
                                   -c %outorder.content \
                                      > logs/status.log