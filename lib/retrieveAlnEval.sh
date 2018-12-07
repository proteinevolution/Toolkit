#!/bin/bash
# Set environment
source ${ENVIRONMENT}

#separate accessions in string by whitespace
ACCESSIONS=$(echo $accessionsStr | tr " " "\n")

# write accessions to be retrieved in file
printf "${ACCESSIONS[@]}" >> results/${filename}_accessionsToRetrieve

retrieveAlignment.pl  results/output.aln_fas \
                      results/${filename}_accessionsToRetrieve \
                      results/${filename}.fa \
                      ${mode}

