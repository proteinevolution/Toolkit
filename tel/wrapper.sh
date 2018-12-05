#!/bin/bash
# get the jobID
JOBID=$(basename $(dirname $(pwd)))
# initialize the sgeID
SGEID=""
# get the Hostname of this server
HOSTNAME=$(hostname)
# get key for job status update to job folder
key=`date | md5sum | awk '{print $1}'`
# return the current key
echo $key > ../key

# Decide whether to submit jobs to the cluster system (SGE) or to run them locally on Olt (LOCAL).
# When the cluster system is busy, the LOCAL mode could be used to have jobs run through immediately.
# However, qstat-related functions (e.g. cluster load calculation) will not work in this mode.
# Can be overriden by setting the SUBMIT_MODE environment variable (used by docker)
MODE=${SUBMIT_MODE:-"SGE"}

if [ "$HOSTNAME" = "olt" ] || [ "$HOSTNAME" = "rye" ]; then

until $(curl -X PUT --output /dev/null --silent --head --fail http://%HOSTNAME:%PORT/api/jobs/status/queued/$JOBID/$key); do
    printf 'host unreachable\n...waiting to set job status to queued\n'
    sleep 5
done

if [ "$HOSTNAME" = "olt" ]; then
    if [ "$MODE" = "SGE" ]; then
            qsub -sync n \
                -l s_rt=%SOFTRUNTIME \
                -l h_rt=%HARDRUNTIME \
                -l s_vmem=%SOFTMEMORY \
                -l h_vmem=%MEMORY,h="node502|node503|node504|node505|node506|node507|node508|node509|node510|node511|node512|node513" \
                -pe parallel %THREADS \
                -cwd  \
                -terse \
                %r > jobIDCluster
    elif [ "$MODE" = "LOCAL" ]; then
                %r > jobIDCluster
    fi
elif [ "$HOSTNAME" = "rye" ]
  then
      HOSTNAME="rye"
      qsub -sync n \
               -l s_rt=%SOFTRUNTIME \
               -l h_rt=%HARDRUNTIME \
               -l s_vmem=%SOFTMEMORY \
               -l h_vmem=%MEMORY,h="node33|node34|node35|node36" \
               -pe parallel %THREADS \
               -cwd  \
               -terse \
               %r > jobIDCluster
  fi

# Grab the sge ID from the generated file
if [ -e jobIDCluster ]
then
    SGEID=$(<jobIDCluster)
fi

# Set sge id in the Toolkit
until $(curl -X PUT --output /dev/null --silent --head --fail http://%HOSTNAME:%PORT/api/jobs/sge/$JOBID/$SGEID/$key); do
    printf 'host unreachable\n...waiting to retrieve sge id\n'
    sleep 5
done

# Write the file to delete the execution. This is necessary for the Toolkit being able to delete the job from the gridengine
echo "#!/bin/bash" > delete.sh
echo "qdel $(cat jobIDCluster)" >> delete.sh
chmod u+x delete.sh

else
    touch "delete.sh"

    chmod +x "delete.sh"
    exec %r
fi

