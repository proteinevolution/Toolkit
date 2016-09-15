#!/bin/bash

trap 'kill $(jobs -p)' EXIT

N_THREADS =1

%BIOPROGS/tools/GLProbs/glprobs \
                                %sequences.path \
                                -o results/glprobs_aln \
                                -num_threads 1