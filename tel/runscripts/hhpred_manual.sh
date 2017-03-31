# fetch parameters from the parent here
cp ../../%parentid.content/results/${JOBID}.hhr ../params
cp ../../%parentid.content/results/query.a3m ../params

mkdir -p ../results/cif

checkTemplates.pl -i   ../params/${JOBID}.hhr \
                  -pir ../results/tomodel.pir \
                  -cif %CIF \
                  -o   $(readlink -f ../results/cif) \
                  -m   %templates.content  >   ../results/results.out

# Remove line which reveals a path from the result
sed -i '/create/d' $(readlink -f ../results/results.out)

# Remove whitespace lines and whitespace at line start
sed -i 's/^\s+//' $(readlink -f ../results/tomodel.pir)
sed -i '/^\s+$/d' $(readlink -f ../results/tomodel.pir)


