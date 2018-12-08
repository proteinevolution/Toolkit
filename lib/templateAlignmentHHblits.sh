#!/bin/bash
# Set environment
source ${ENVIRONMENT}

if [ ! -e "results/$accession.fas" ]
then
    HHBLITS=${DATABASES}/hhblits/
    DB=$( grep "^[^#]" ${DATABASES}/hhblits/DB | awk 'NR==1{print $1}')


    MAPPINGFILE="uniclust_uniprot_mapping.tsv"
    echo $HHBLITS$MAPPINGFILE
    MAPPEDID=`grep ${accession} ${HHBLITS}${MAPPINGFILE} | awk '{print $1}'`

    ffindex_get ${HHBLITS}${DB}_a3m.ffdata ${HHBLITS}${DB}_a3m.ffindex $MAPPEDID >> results/$accession.a3m
    sed -i '1d' results/$accession.a3m
    hhfilter -i results/$accession.a3m -o results/$accession.ra3m -diff 100
    sed -i "1 i\#A3M#" results/$accession.ra3m

fi