#!/bin/bash

trap 'kill $(jobs -p)' EXIT

perl ../../scripts/search.pl \
                    -i  '%inputpattern.content' \
                    -d %standarddb.content \
                    -o results/patsearch_result \
                    -%type.content \
                     > report_patsearch