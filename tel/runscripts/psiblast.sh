#!/bin/bash

trap 'kill $(jobs -p)' EXIT

<BIOPROGS>/tools/ncbi-blast-2.3.0+/bin/psiblast -db <DATABASES>/standard/nr70 \
                                            -matrix ${matrix} \
                                            -num_iterations ${num_iter} \
                                            -evalue ${evalue}\
                                            -gapopen ${gap_open} \
                                            -gapextend ${gap_ext} \
                                            -num_threads 4 \
                                            -num_descriptions ${desc}\
                                            -num_alignments ${desc}\
                                            -in_msa %{alignment} \
                                            -out results/out.psiblastp \
                                            -outfmt 0 \
                                            -html\
                                            -out_pssm results/out.ksf 
# Produce some extra files:
< results/out.psiblastp grep Expect | awk '{ print $8; }' | sed 's/,$//' > results/evalues.dat



