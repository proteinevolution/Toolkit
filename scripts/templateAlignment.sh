#!/bin/bash
# Set environment

source /ebio/abt1_share/toolkit_support1/code/bioprogs/env/environment.sh
HHSUITE=$DATABASES/hh-suite
scopdir=${HHSUITE}/scope/scope
pfamdir=${HHSUITE}/pfama/pfama
mmcifdir=${HHSUITE}/mmcif70/mmcif70


if [ ! -e "results/$accession.template.fas" ]
then
    #successively try to fetch a3m from databases
    ffindex_get ${scopdir}_a3m.ffdata ${scopdir}_a3m.ffindex $accession >> results/$accession.a3m
    ffindex_get ${mmcifdir}_a3m.ffdata ${mmcifdir}_a3m.ffindex $accession >> results/$accession.a3m
    ffindex_get ${pfamdir}_a3m.ffdata ${pfamdir}_a3m.ffindex "#$accession" >> results/$accession.a3m
    hhfilter -i results/$accession.a3m -o results/$accession.template.reduced.a3m -diff 100
    reformat.pl a3m fas results/$accession.template.reduced.a3m results/$accession.template.fas
fi
