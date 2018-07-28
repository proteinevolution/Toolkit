#!/bin/bash
# Set environment
if [ "${HOSTNAME}" = "olt" ]
  then
    source /ebio/abt1_share/toolkit_support1/code/bioprogs/env/environment.sh
  elif [ "${HOSTNAME}" = "rye" ]
  then
    source /cluster/toolkit/production/bioprogs/env/environment_rye.sh
fi

HHSUITE=${DATABASES}/hh-suite/
PFAMREGEX="(pfam[0-9]+)|(^PF[0-9]+ ?(.[0-9]+))"
FILESTRING=$(tr "\n" " " < params/dbs)
DBS=(${FILESTRING})

if [ ! -e "results/${accession}.a3m" ]
then
    for i in "${DBS[@]}"
    do
        if [[ "${accession}" =~ ${PFAMREGEX} ]]
        then
            ffindex_get ${HHSUITE}${i}_a3m.ffdata ${HHSUITE}${i}_a3m.ffindex "${accession}.a3m" >> results/${accession}.a3m
        else
            ffindex_get ${HHSUITE}${i}_a3m.ffdata ${HHSUITE}${i}_a3m.ffindex ${accession} >> results/${accession}.a3m
        fi
    done

    # Align two sequences or MSAs
    if [[ ${FILESTRING} == "" ]]
    then
        ffindex_get results/db_a3m.ffdata results/db_a3m.ffindex "db.a3m" >> results/$accession.a3m
    fi

    hhfilter -i results/${accession}.a3m -o results/${accession}.reduced.a3m -diff 100
    reformat.pl a3m a3m results/${accession}.reduced.a3m results/${accession}_tmp.a3m -noss

    mv results/${accession}_tmp.a3m results/${accession}.a3m

    sed -i "1 i\#A3M#" results/${accession}.a3m

fi
