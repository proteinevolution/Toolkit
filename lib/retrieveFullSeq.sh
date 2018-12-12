#!/bin/bash
# Set environment
source ${ENVIRONMENT}

#separate accessions in string by whitespace
ACCESSIONS=$(echo $accessionsStr | tr " " "\n")

# write accessions to be retrieved in file
printf "${ACCESSIONS[@]}" > results/${filename}_accessionsToRetrieve

if [ ${db} = "pdb_nr" ] ; then
        #makeblastdb cannot parse PDB IDs
        ffindex_get ${STANDARD}/${db}.ffdata ${STANDARD}/${db}.ffindex $accessionsStr > results/${filename}.fa
else
        #retrieve full length sequences
        seq_retrieve.pl -i results/${filename}_accessionsToRetrieve \
                    -o results/${filename}.fa \
                    -d ${STANDARD}/${db} \
                    -unique 1 > results/${filename}_unretrievable
fi