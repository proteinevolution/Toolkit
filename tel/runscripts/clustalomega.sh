#!/bin/bash

#number of threads needs to be changed and it just set abitrary

trap 'kill $(jobs -p)' EXIT

%BIOPROGS/clustal-omega/clustalo   -i %alingment.path \
                                   -o clustalo_aln \
                                   --outfmt=clustal \
                                   -v \
                                   --force \
                                   --threads=1\
                                   %otheradvanced.content \
                                   > results/report






