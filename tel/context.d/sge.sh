#!/bin/bash

qsub -sync y \
      -cwd  \
       %r
