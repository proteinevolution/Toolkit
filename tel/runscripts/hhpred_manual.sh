

# fetch parameters from the parent here
cp ../../%parentid.content/results/hhsearch.hhr ../params
cp ../../%parentid.content/results/query.a3m ../params

mkdir -p ../results/cif

checkTemplates2.pl -i   ../params/hhsearch.hhr \
                   -pir ../results/tomodel.pir \
                   -cif %CIF \
                   -q   $(readlink -f ../params/query.a3m) \
                   -o   $(readlink -f ../results/cif) \
                   -m   %templates.content  >   ../results/results.out

#checkTemplates.pl -i   ../params/hhsearch.hhr \
#                  -q   ../params/query.a3m \
#                  -pir ../results/tomodel.pir \
#                  -m   %templates.content  >   ../results/results.out

# Remove line which reveals a path from the result
#sed -i '/create/d' $(readlink -f ../results/results.out)


