qsub -sync n \
     -l h_vmem=128G \
     -cwd  \
     %r | grep -oE "[0-9]+" > jobIDCluster

# Write the file to delete the execution. This is necessary for the Toolkit being able to delete the job from the gridengine
echo "#!/bin/bash" > delete.sh
echo "qdel $(cat jobIDCluster)" >> delete.sh
chmod u+x delete.sh
