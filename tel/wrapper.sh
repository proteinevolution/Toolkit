#!/bin/bash


HOSTNAME=$(hostname)



if [ "$HOSTNAME" = "olt" ] || [ "$HOSTNAME" = "rye" ]; then


JOBID=$(basename $(dirname $(pwd))) 


curl -X POST http://%HOSTNAME:%PORT/jobs/queued/$JOBID

if [ "$HOSTNAME" = "olt" ]
  then
    qsub -sync n \
         -l h_vmem=%MEMORY,h="node502|node503|node504|node505|node506|node507|node508|node509|node510|node511|node512|node513" \
         -cwd  \
         %r | grep -oE "[0-9]+" > jobIDCluster

elif [ "$HOSTNAME" = "rye" ]
  then
      HOSTNAME="rye"
      qsub -sync n \
               -l h_vmem=%MEMORY,h="node33|node34|node35|node36" \
               -cwd  \
               %r | grep -oE "[0-9]+" > jobIDCluster
  fi


# Write the file to delete the execution. This is necessary for the Toolkit being able to delete the job from the gridengine
echo "#!/bin/bash" > delete.sh
echo "qdel $(cat jobIDCluster)" >> delete.sh
chmod u+x delete.sh

else
    touch "delete.sh"
    
    chmod +x "delete.sh" 
    exec %r
fi

