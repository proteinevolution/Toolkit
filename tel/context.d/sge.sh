#!/bin/bash

qsub -sync n \
      -cwd  \
       %r
