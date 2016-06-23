#!/bin/bash

trap 'kill $(jobs -p)' EXIT

perl %BIOPROGS/helpers/reformat.pl -i=%alignment_format.content \
                                   -o=clu \
                                   -f=%alignment.path \
                                   -a=results/result
