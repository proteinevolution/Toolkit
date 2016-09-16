#!/bin/bash

trap 'kill $(jobs -p)' EXIT

perl %BIOPROGS/helpers/search.pl \
                    -i  '%inputpattern.content' \
                    -d %standarddb.content \
                    -o results/patsearch_result \
                    %type \
                     > report_patsearch