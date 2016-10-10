#!/usr/bin/env bash


trap 'kill $(jobs -p)' EXIT


%BIOPROGS/tools/probcons/probcons -v \
                            -c %consistency.content \
                            -ir %itrefine.content \
                            -pre %pretrain.content \
                            %alignment.path \
                            > results/probcons_aln
