# fetch parameters from the parent here
cp ../../%parentid.content/results/%parentid.content.hhr ../params

mkdir -p ../results/cif

echo "#Converting selected template alignments into PIR format." >> ../results/process.log

checkTemplates.pl -i   ../params/%parentid.content.hhr \
                  -pir ../results/tomodel.pir \
                  -cif %CIFALL \
                  -o   $(readlink -f ../results/cif) \
                  -m   %templates.content  >   ../results/results.out

echo "done" >> ../results/process.log

# Remove line which reveals a path from the result
sed -i '/create/d' $(readlink -f ../results/results.out)

# Remove whitespace lines and whitespace at line start
sed -i 's/^\s+//' $(readlink -f ../results/tomodel.pir)
sed -i '/^\s+$/d' $(readlink -f ../results/tomodel.pir)


