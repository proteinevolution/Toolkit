qsub -sync n \
      -l h_vmem=128G \
      -cwd  \
       %r | grep -oE "[0-9]+" >> jobIDCluster
