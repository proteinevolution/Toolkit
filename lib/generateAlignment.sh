#!/usr/bin/env bash

if [ "$HOSTNAME" = "olt" ]
  then
    source /ebio/abt1_share/toolkit_support1/code/bioprogs/env/environment.sh

elif [ "$HOSTNAME" = "rye" ]
  then
    source /cluster/toolkit/production/bioprogs/env/environment_prod.sh
  fi

hhmakemodel.pl  -i results/${jobID}.hhr -fas results/alignment.fas -m ${numList}