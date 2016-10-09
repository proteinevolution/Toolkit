#!/bin/bash

trap 'kill $(jobs -p)' EXIT

%BIOPROGS/tools/kalign/kalign -i %alignment.path \
                                   -o results/kalign_aln \
                                   -s %gap_open.content \
                                   -e %gap_ext.content \
                                   -t %gap_term.content \
                                   -m %bonusscore.content \
                                   -c %outorder.content \
                                   -f fasta \
                                      > logs/status.log

