#!/bin/bash
# Set environment
if [ "$HOSTNAME" = "olt" ]
  then
    source /ebio/abt1_share/toolkit_support1/code/bioprogs/env/environment.sh

elif [ "$HOSTNAME" = "rye" ]
  then
    source /cluster/toolkit/production/bioprogs/env/environment_rye.sh
  fi


if [ ! -e "results/$accession.fas" ]
then

    DB=${HHOMPDBPATH}/HHOMP

    ffindex_get ${DB}_a3m.ffdata ${DB}_a3m.ffindex $accession >> results/$accession.fas
fi