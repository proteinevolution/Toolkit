#!/bin/bash
# Set environment


if [ "$HOSTNAME" = "olt" ]
  then
    source /ebio/abt1_share/toolkit_support1/code/bioprogs/env/environment.sh

elif [ "$HOSTNAME" = "rye" ]
  then
    source /cluster/toolkit/production/bioprogs/env/environment_rye.sh
  fi

    #separate accessions in string by whitespace
    ACCESSIONS=$(echo $accessionsStr | tr " " "\n")

    # write accessions to be retrieved in file
    printf "${ACCESSIONS[@]}" >> results/${filename}_accessionsToRetrieve

    retrieveAlignment.pl  results/output_psiblastp.aln \
                          results/${filename}_accessionsToRetrieve \
                          results/${filename}.fa \
                          ${mode}

