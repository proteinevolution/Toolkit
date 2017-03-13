#!/bin/bash
# Set environment

source /ebio/abt1_share/toolkit_support1/code/bioprogs/env/environment_server.sh
if [ ! -e "results/$accession.template.fas" ]
then
    #successively try to fetch a3m from databases
    ffindex_get ${scopdir}scope_a3m.ffdata ${scopdir}scope_a3m.ffindex $accession >> results/$accession.a3m
    ffindex_get ${mmcifdir}mmcif70_a3m.ffdata ${mmcifdir}mmcif70_a3m.ffindex $accession >> results/$accession.a3m
    ffindex_get ${pfamdir}PfamA_a3m.ffdata ${pfamdir}PfamA_a3m.ffindex "#$accession" >> results/$accession.a3m
    hhfilter -i results/$accession.a3m -o results/$accession.template.reduced.a3m -diff 100
    reformat.pl a3m fas results/$accession.template.reduced.a3m results/$accession.template.fas
fi
