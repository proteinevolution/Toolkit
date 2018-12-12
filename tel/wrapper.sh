#!/bin/bash
# get the jobID
JOBID=$(basename $(dirname $(pwd)))
# get key for job status update to job folder
KEY=`date | md5sum | awk '{print $1}'`
# return the current key
echo $KEY > ../key

until $(curl -X PUT --output /dev/null --silent --head --fail http://%HOSTNAME:%PORT/api/jobs/status/queued/$JOBID/$KEY); do
    printf 'host unreachable\n...waiting to set job status to queued\n'
    sleep 5
done

#Submit job to SGE
if [[ %SUBMITMODE = "sge" ]]; then
            qsub -sync n \
                -l s_rt=%SOFTRUNTIME \
                -l h_rt=%HARDRUNTIME \
                -l s_vmem=%SOFTMEMORY \
                -l h_vmem=%MEMORY,h=%SGENODES \
                -pe parallel %THREADS \
                -cwd  \
                -terse \
                -v BIOPROGSROOT=%BIOPROGSROOT,DATABASES=%DATABASES \
                %r > jobIDCluster
else
                export BIOPROGSROOT="%BIOPROGSROOT"
                export DATABASES="%DATABASES"
                %r > jobIDCluster
fi

# Grab the sge ID from the generated file
if [[ -e jobIDCluster ]]; then
    SGEID=$(<jobIDCluster)
fi

# Set sge id in the Toolkit
until $(curl -X PUT --output /dev/null --silent --head --fail http://%HOSTNAME:%PORT/api/jobs/sge/$JOBID/$SGEID/$KEY); do
    printf 'host unreachable\n...waiting to retrieve sge id\n'
    sleep 5
done

# Write the file to delete the execution. This is necessary for the Toolkit being able to delete the job from the gridengine
echo "#!/bin/bash" > delete.sh
echo "qdel $SGEID" >> delete.sh
chmod u+x delete.sh