#!/bin/bash


HOSTNAME=$(hostname)
# get key for job status update to job folder
key=`date | md5sum | awk '{print $1}'`

echo $key > ../key
if [ "$HOSTNAME" = "olt" ] || [ "$HOSTNAME" = "rye" ]; then

QUEUE=%QUEUE
JOBID=$(basename $(dirname $(pwd)))

until $(curl -X POST --output /dev/null --silent --head --fail http://%HOSTNAME:%PORT/jobs/queued/$JOBID/$key); do
    printf 'host unreachable\n...waiting to set job status to queued\n'
    sleep 5
done

if [ "$HOSTNAME" = "olt" ]
  then
    if [ $QUEUE = "short" ]; then QUEUE="short.q"; fi
    if [ $QUEUE = "normal" ]; then QUEUE="long.q"; fi

    qsub -sync n \
         -l h_vmem=%MEMORY,h="node502|node503|node504|node505|node506|node507|node508|node509|node510|node511|node512|node513" \
         -cwd  \
         %r | grep -oE "[0-9]+" > jobIDCluster

elif [ "$HOSTNAME" = "rye" ]
  then
    if [ $QUEUE = "short" ]; then QUEUE="toolkit_immediate"; fi
    if [ $QUEUE = "normal" ]; then QUEUE="toolkit_normal"; fi

      HOSTNAME="rye"
      qsub -sync n \
               -q $QUEUE \
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

