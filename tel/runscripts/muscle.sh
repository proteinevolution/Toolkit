#!/bin/bash

trap 'kill $(jobs -p)' EXIT
%BIOPROGS/tools/muscle/muscle -in %sequences.path \
                             -out results/muscle_aln \
                             -maxiters %maxrounds.content \
                              %otheradvanced.content \
                               > logs/status.log