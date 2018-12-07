#!/bin/bash
# Set environment
source ${ENVIRONMENT}

HHBLITS=${DATABASES}/hhblits/
DB=${STANDARD}/uniprot_trembl

#separate accessions in string by whitespace
ACCESSIONS=$(echo $accessionsStr | tr " " "\n")

# write accessions to be retrieved in file
printf "${ACCESSIONS[@]}" > results/${filename}_accessionsToRetrieve

#retrieve full length sequences
seq_retrieve.pl -i results/${filename}_accessionsToRetrieve \
                    -o results/${filename}.fa \
                    -d ${DB} \
                    -unique 1 > results/${filename}_unretrievabl
