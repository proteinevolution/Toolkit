JOBID=%jobid.content

###Fix after slider works
##   -e %inclusion_ethresh.content  \

hhblits -cpu %THREADS \
        -i %alignment.path \
        -d %HHBLITS/%hhblitsdb.content     \
        -o $(readlink -f ../results/${JOBID}.hhr) \
        -oa3m $(readlink -f ../results/out.a3m)  \
        -e 0.001 \
        -n %maxrounds.content  \
        -p %pmin.content \
        -Z %max_lines.content \
        -z 1 \
        -b 1 \
        -B %max_lines.content  \
        -seq %max_seqs.content \
        -aliw %aliwidth.content \
        -%alignmode.content

hhr2json.py "$(readlink -f ../results/${JOBID}.hhr)" > $(readlink -f ../results/${JOBID}.json)

json2fasta.py ../results/${JOBID}.json ../results/${JOBID}.fasta


# Reformat query into fasta format ('full' alignment, i.e. 100 maximally diverse sequences, to limit amount of data to transfer)
hhfilter -i $(readlink -f ../results/out.a3m) \
         -o $(readlink -f ../results/out.full.a3m) \
         -diff 100
reformat.pl a3m fas \
            $(readlink -f ../results/out.full.a3m) \
            $(readlink -f ../results/out.full.fas) \
            -d 160

#create full alignment json
fasta2json.py ../results/out.full.fas ../results/full.json
#delete temp files
rm ../results/out.full.fas ../results/out.full.a3m



# Reformat query into fasta format (reduced alignment)  
hhfilter -i $(readlink -f ../results/out.a3m) \
         -o $(readlink -f ../results/out.reduced.a3m)  \
         -diff 50


reformat.pl -r a3m fas \
    $(readlink -f ../results/out.reduced.a3m) \
    $(readlink -f ../results/out.reduced.fas)

#create reduced alignment json
fasta2json.py ../results/out.reduced.fas ../results/reduced.json
#delete temp files
rm ../results/out.reduced.fas ../results/out.reduced.a3m

# Generate Query in JSON
fasta2json.py %alignment.path ../results/query.json

hhviz.pl ${JOBID} ../results/ ../results/  &> /dev/null