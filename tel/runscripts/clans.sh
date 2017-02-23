#!/bin/bash

trap 'kill $(jobs -p)' EXIT

%BIOPROGS/tools/ncbi-blast-2.3.0+/bin/psiblast -db %STANDARD/%standarddb.content \
                                            -matrix %matrix.content \
                                            -num_iterations %num_iter.content \
                                            -evalue %evalue.content \
                                            -num_threads 4 \
                                            -in_msa %alignment.path \
                                            -out results/out.psiblastp \
                                            -outfmt 0 \
                                            -html\
                                            -out_pssm results/out.ksf





