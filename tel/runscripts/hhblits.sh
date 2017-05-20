JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)
FORMAT=$(head -1 ../params/alignment | egrep "^CLUSTAL" | wc -l)

if [ $CHAR_COUNT -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

if [ $SEQ_COUNT = "0" ] && [ $FORMAT = "0" ] ; then
      echo "#Invalid input format. Input should be in aligned FASTA/CLUSTAL format." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

if [ $FORMAT = "1" ] ; then
      reformatValidator.pl clu fas \
            $(readlink -f %alignment.path) \
            $(readlink -f ../results/${JOBID}.in.fas) \
            -d 160 -uc -l 32000
else
      reformatValidator.pl fas fas \
            $(readlink -f %alignment.path) \
            $(readlink -f ../results/${JOBID}.in.fas) \
            -d 160 -uc -l 32000
fi

if [ ! -f ../results/${JOBID}.in.fas ]; then
    echo "#Input is not in aligned FASTA/CLUSTAL format." >> ../results/process.log
    curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
    false
fi

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.in.fas | wc -l)

if [ $SEQ_COUNT -gt "2000" ] ; then
      echo "#Input contains more than 2000 sequences." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

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
        -Z %desc.content \
        -z 1 \
        -b 1 \
        -B %desc.content

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1


echo "#Generating output" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

#Generate query template alignment
hhmakemodel.pl -i ../results/${JOBID}.hhr -fas ../results/querytemplateMSA.fas -p %pmin.content
# Generate Query in JSON
fasta2json.py ../results/querytemplateMSA.fas ../results/querytemplate.json


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
            $(readlink -f ../results/alignment.fas) \
            -d 160

#create full alignment json; use for forwarding
fasta2json.py ../results/alignment.fas ../results/rep100.json

# add DB to json
manipulate_json.py -k 'db' -v '%hhblitsdb.content' ../results/${JOBID}.json



# Generate Query in JSON
fasta2json.py ../results/${JOBID}.in.fas ../results/query.json

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
