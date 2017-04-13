#!/bin/bash
# Set environment
source /ebio/abt1_share/toolkit_support1/code/bioprogs/env/environment.sh
HHBLITS=${DATABASES}/hhblits/
DB=${STANDARDNEW}/uniprot

#separate accessions in string by whitespace
ACCESSIONS=$(echo $accessionsStr | tr " " "\n")

# write accessions to be retrieved in file
printf "${ACCESSIONS[@]}" > results/accessionsToRetrieve


MAPPINGFILE="uniclust_uniprot_mapping.tsv"
MAPPEDID=`grep ${accession} ${HHBLITS}${MAPPINGFILE} | awk '{print $1}'`

#retrieve full length sequences
seq_retrieve.pl -i results/accessionsToRetrieve \
                -o results/sequences.fa \
                -d ${DB} \
                -unique 1 > results/unretrievabl
