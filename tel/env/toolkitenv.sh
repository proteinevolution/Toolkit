#!/usr/bin/env bash


if [ "$HOSTNAME" = "olt" ]
  then
    chmod u+rwx /ebio/abt1_share/toolkit_support1/code/bioprogs/env/environment.sh
    export ENVSCRIPT="/ebio/abt1_share/toolkit_support1/code/bioprogs/env/environment.sh"

elif [ "$HOSTNAME" = "rye" ]
  then
    chmod u+rwx /cluster/toolkit/production/env/environment_prod.sh
    export ENVSCRIPT="/cluster/toolkit/production/env/environment_prod.sh"
  fi


