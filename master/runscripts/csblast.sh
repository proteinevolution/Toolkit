#!/bin/bash

trap 'kill $(jobs -p)' EXIT


!{BIO}/tools/csblast/bin -i %{alignment} \
                         -j ${num_iter} \
    #                     -h ${e_thresh}  \ Not implemented \
                         -D !{BIO}/tools/csblast/data/K4000.crf    \
                         -B  %{alignment}      \
                          --blast-path #{BLAST}/bin \
                         -e #{@expect} \ 
                         -F #{@filter}
                         -G #{@gapopen}
                         -E #{@gapext}
                         -v #{@descriptions} 
                         -b #{@alignments} 
                         -T T
                         -o #{@outfile}
                         -d \"#{@db_path}\"
                         -I T
                         -a 1 




