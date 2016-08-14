#!/bin/bash


#% alignment : A2M


trap 'kill $(jobs -p)' EXIT

perl %BIOPROGS/helpers/reformat.pl -i=%alignment_format.content \
                                   -o=a2m \
                                   -f=%alignment.path \
                                   -a=temp/infile_a2m


