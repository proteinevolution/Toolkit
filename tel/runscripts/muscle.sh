#!/bin/bash

trap 'kill $(jobs -p)' EXIT
%BIOPROGS/tools/muscle/muscle -in %alignment.path \
                             -out results/muscle_aln \
                             -maxiters %maxrounds.content \
                               > logs/status.log
