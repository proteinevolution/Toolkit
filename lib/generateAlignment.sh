#!/usr/bin/env bash
# Set environment
source ${ENVIRONMENT}

hhmakemodel.pl  -i results/${jobID}.hhr -fas results/${filename}.fa -m ${numList}
sed -i '/^$/d' results/${filename}.fa