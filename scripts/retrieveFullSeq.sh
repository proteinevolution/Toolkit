#!/bin/bash
# Set environment
source /ebio/abt1_share/toolkit_support1/code/bioprogs/env/environment.sh

    #separate accessions in string by whitespace
    ACCESSIONS=$(echo $accessionsStr | tr " " "\n")

    # write accessions to be retrieved in file
    printf "${ACCESSIONS[@]}" > results/accessionsToRetrieve

    #retrieve full length sequences
    seq_retrieve.pl -i results/accessionsToRetrieve \
                    -o results/sequences.fa \
                    -d ${STANDARDNEW}/${db} \
                    -unique 1 > results/unretrievable
