#!/bin/bash
# Set environment
source ${ENVIRONMENT}

HHSUITE=${DATABASES}/hh-suite/
FILESTRING=$(tr "\n" " " < params/dbs)
DBS=(${FILESTRING})

if [[ ! -e "results/${accession}" ]]
then
    for i in "${DBS[@]}"
    do
    	ffindex_get ${HHSUITE}${i}_a3m.ffdata ${HHSUITE}${i}_a3m.ffindex ${accession} >> results/${accession}
    done

    # Align two sequences or MSAs
    if [[ ${FILESTRING} == "" ]]
    then
        ffindex_get results/db_a3m.ffdata results/db_a3m.ffindex "db.a3m" >> results/$accession
    fi

    hhfilter -i results/${accession} -o results/${accession}.reduced -diff 100
    reformat.pl a3m a3m results/${accession}.reduced results/${accession}_tmp -noss

    mv results/${accession}_tmp results/${accession}

    sed -i '/^#/d' results/${accession}
    sed -i "1 i\#A3M#" results/${accession}
fi