

# fetch parameters from the parent here
cp ../../%parentid.content/results/hhsearch.hhr ../params
cp ../../%parentid.content/results/query.a3m ../params


checkTemplates.pl -i   ../params/hhsearch.hhr \
                  -q   ../params/query.a3m \
                  -pir ../results/tomodel.pir \
                  -m   %templates.content  >   ../results/results.out
