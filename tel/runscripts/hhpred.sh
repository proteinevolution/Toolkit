#!/bin/bash

trap 'kill $(jobs -p)' EXIT

perl ../../scripts/reformat.pl -i=%alignment_format.content \
                                   -o=a2m \
                                   -f=%alignment.path \
                                   -a=temp/infile_a2m


