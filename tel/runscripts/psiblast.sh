#!/bin/bash

trap 'kill $(jobs -p)' EXIT

%BIOPROGS/tools/ncbi-blast-2.3.0+/bin/psiblast -db %DATABASES/standard/nr70 \
                                            -matrix %matrix.content \
                                            -num_iterations %num_iter.content \
                                            -evalue %evalue.content \
                                            -gapopen %gap_open.content \
                                            -gapextend %gap_ext.content \
                                            -num_threads 4 \
                                            -num_descriptions %desc.content \
                                            -num_alignments %desc.content \
                                            -in_msa %alignment.path \
                                            -out results/out.psiblastp \
                                            -outfmt 0 \
                                            -html\
                                            -out_pssm results/out.ksf 
# Produce some extra files:
< results/out.psiblastp grep Expect | awk '{ print $8; }' | sed 's/,$//' > results/evalues.dat



