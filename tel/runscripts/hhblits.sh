JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)


reformat_hhsuite.pl fas fas \
	$(readlink -f %alignment.path) \
    $(readlink -f ../results/${JOBID}.in.fas) \
    -d 160 -uc -l 32000

if [ $SEQ_COUNT -gt "1" ] ; then
       echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
       curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
else
       echo "#Query is a single protein sequence." >> ../results/process.log
       curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
fi

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Searching %hhblitsdb.content." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

hhblits -cpu %THREADS \
        -i ../results/${JOBID}.in.fas \
        -d %HHBLITS/%hhblitsdb.content     \
        -o $(readlink -f ../results/${JOBID}.hhr) \
        -oa3m $(readlink -f ../results/${JOBID}.a3m)  \
        -e %hhblits_incl_eval.content \
        -n %maxrounds.content  \
        -p %pmin.content \
        -Z %max_lines.content \
        -z 1 \
        -b 1 \
        -B %max_lines.content  \
        -seq %max_seqs.content \
        -%alignmode.content

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1


echo "#Generating output" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1


hhr2json.py "$(readlink -f ../results/${JOBID}.hhr)" > $(readlink -f ../results/${JOBID}.json)
json2fasta.py ../results/${JOBID}.json ../results/${JOBID}.fasta
#Visualization
hhviz.pl ${JOBID} ../results/ ../results/  &> /dev/null


# Reformat query into fasta format; 100 most diverse sequences
hhfilter -i $(readlink -f ../results/${JOBID}.a3m) \
         -o $(readlink -f ../results/${JOBID}.rep100.a3m) \
         -diff 100

# Reformat query into fasta format; full alignment
reformat_hhsuite.pl a3m fas \
            $(readlink -f ../results/${JOBID}.rep100.a3m) \
            $(readlink -f ../results/${JOBID}.rep100.fas) \
            -d 160

#create full alignment json; use for forwarding
fasta2json.py ../results/${JOBID}.rep100.fas ../results/rep100.json


# Generate Query in JSON
fasta2json.py ../results/${JOBID}.in.fas ../results/query.json

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
