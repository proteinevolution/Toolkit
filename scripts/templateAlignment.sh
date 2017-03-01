#!/bin/bash
# Set environment

source /ebio/abt1_share/toolkit_support1/code/bioprogs/env/environment_server.sh
DB=""

if [ -e $scopdir/$accession.a3m ]
then
    DB=$scopdir
fi
if [ -e $mmcifdir/$accession.a3m ]
then
    DB=$mmcifdir
fi
if [ -e $pfamdir/$accession.a3m ]
then
    DB=$pfamdir
fi
if [ -e $pdbdir/$accession.a3m ]
then
    DB=$pdbdir
fi

if [ ! -e "results/$jobID.template.reduced.a3m" ]
then
    hhfilter -i $DB/$accession.a3m -o results/$jobID.template.reduced.a3m -diff 100
    reformat.pl a3m fas results/$jobID.template.reduced.a3m results/$jobID.template.fas
fi


#hhfilter -i $scopdir/$accession.a3m -o results/$jobID.template.reduced.a3m -diff 50
#reformat.pl -r a3m fas results/$jobID.template.reduced.a3m results/$jobID.template.reduced.fas
#rm results/$jobID.template.reduced.a3m