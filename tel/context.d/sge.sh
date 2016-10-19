#!/bin/bash

qsub -sync n \
      -cwd  \
       %r | grep -oE "[0-9]+" >> jobIDCluster
