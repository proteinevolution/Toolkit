#!/bin/bash
# Set environment
source /ebio/abt1_share/toolkit_support1/code/bioprogs/env/environment.sh
HHSUITE=${DATABASES}/hh-suite/
PFAMREGEX="(pfam[0-9]+)|(^PF[0-9]+ ?(.[0-9]+))"
FILESTRING=$(cat  params/dbs|tr "\n" " ")


DBS=($FILESTRING)

echo $FILESTRING

if [ ! -e "results/$number.fas" ]
then
    for i in "${DBS[@]}"
    do
    :
    echo $i $accession
    if [[ "$accession" =~ $PFAMREGEX ]]
    then
        ffindex_get ${HHSUITE}${i}_a3m.ffdata ${HHSUITE}${i}_a3m.ffindex "$accession.a3m" >> results/$number.a3m
    else
        ffindex_get ${HHSUITE}${i}_a3m.ffdata ${HHSUITE}${i}_a3m.ffindex $accession >> results/$number.a3m
    fi
    done


    # Align two sequences or MSAs
    if [[ $FILESTRING == "" ]]
    then
        ffindex_get results/db_a3m.ffdata results/db_a3m.ffindex "db.a3m" >> results/$number.a3m
        ffindex_get results/db_a3m.ffdata results/db_a3m.ffindex $accession >> results/$number.a3m
    fi
fi

hhfilter -i results/$number.a3m -o results/$number.reduced.a3m -diff 100
reformat.pl a3m fas results/$number.reduced.a3m results/$number.fas
