JOBID=%jobid.content

###Fix after slider works
##   -e %inclusion_ethresh.content  \

hhblits -cpu %THREADS \
        -i %alignment.path \
        -d %HHBLITS/%hhblitsdb.content     \
        -o $(readlink -f ../results/${JOBID}.hhr) \
        -oa3m $(readlink -f ../results/${JOBID}.a3m)  \
        -e 0.001 \
        -n %maxrounds.content  \
        -p %pmin.content \
        -Z %max_lines.content \
        -z 1 \
        -b 1 \
        -B %max_lines.content  \
        -seq %max_seqs.content \
        -%alignmode.content

hhr2json.py "$(readlink -f ../results/${JOBID}.hhr)" > $(readlink -f ../results/${JOBID}.json)
json2fasta.py ../results/${JOBID}.json ../results/${JOBID}.fasta
#Visualization
hhviz.pl ${JOBID} ../results/ ../results/  &> /dev/null

# Reformat query into fasta format; full alignment
reformat_hhsuite.pl a3m fas \
            $(readlink -f ../results/${JOBID}.a3m) \
            $(readlink -f ../results/${JOBID}.full.fas) \
            -d 160

#create full alignment json; use for forwarding
fasta2json.py ../results/${JOBID}.full.fas ../results/${JOBID}.full.json


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
fasta2json.py ../results/${JOBID}.rep100.fas ../results/${JOBID}.rep100.json


# Generate Query in JSON
fasta2json.py %alignment.path ../results/query.json

