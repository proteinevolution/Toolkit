#!/bin/bash

trap 'kill $(jobs -p)' EXIT

perl !{BIO}/helpers/reformat.pl -i=${alignment_format} -o=sto -f=#{alignment} -a=+{infile_sto}
!{BIO}/tools/hmmer3/binaries/hmmbuild --cpu 4 +{infile_hmm} +{infile_sto}
!{BIO}/tools/hmmer3/binaries/hmmsearch --cpu 4 -E 1e-1 --tblout @{tbl} --domtblout @{domtbl}  -o @{outfile} -A @{outfile_multi_sto} +{infile_hmm} !{DATA}/nr



#perl !{BIO}/helpers/reformat.pl -i=${alignment_format} -o=clu -f=#{alignment} -a=@{result}
