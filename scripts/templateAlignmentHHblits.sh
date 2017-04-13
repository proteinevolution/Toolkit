#!/bin/bash
# Set environment
source /ebio/abt1_share/toolkit_support1/code/bioprogs/env/environment.sh


if [ ! -e "results/$accession.fas" ]
then
    HHBLITS=${DATABASES}/hhblits/
    DB=$( grep "^[^#]" ${DATABASES}/hhblits/DB | awk 'NR==1{print $1}')


    MAPPINGFILE="uniclust_uniprot_mapping.tsv"
    echo $HHBLITS$MAPPINGFILE
    MAPPEDID=`grep ${accession} ${HHBLITS}${MAPPINGFILE} | awk '{print $1}'`

    ffindex_get ${HHBLITS}${DB}_a3m.ffdata ${HHBLITS}${DB}_a3m.ffindex $MAPPEDID >> results/$accession.a3m


    hhfilter -i results/$accession.a3m -o results/$accession.reduced.a3m -diff 100
    reformat.pl a3m fas results/$accession.reduced.a3m results/$accession.fas
fi