#!/bin/bash

trap 'kill $(jobs -p)' EXIT

#% alignment : STO


perl %BIOPROGS/helpers/reformat.pl -i=%alignment_format.content \
                                   -o=sto \
                                   -f=%alignment.path \
                                   -a=temp/infile_sto

%BIOPROGS/tools/hmmer3/binaries/hmmbuild --cpu 4 \
                                         temp/infile_hmm \
                                         temp/infile_sto 

%BIOPROGS/tools/hmmer3/binaries/hmmsearch --cpu 4 \
                                          -E 1e-1 \
                                         --tblout results/tbl \
                                         --domtblout results/domtbl \
                                          -o results/outfile \
                                          -A results/outfile_multi_sto \
                                           temp/infile_hmm \
                                           %standarddb.content



#perl !{BIO}/helpers/reformat.pl -i=${alignment_format} -o=clu -f=#{alignment} -a=@{result}
