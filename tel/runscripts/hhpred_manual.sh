# fetch parameters from the parent here
cp ../../%parentid.content/results/%parentid.content.hhr ../params

mkdir -p ../results/cif

echo "#Converting selected template alignments into PIR format." >> ../results/process.log

checkTemplates.pl -i   ../params/%parentid.content.hhr \
                  -pir ../results/tomodel.pir \
                  -cif %CIFALL \
                  -o   $(readlink -f ../results/cif) \
                  -m   %templates.content \
                  -l ../results/results.out

echo "done" >> ../results/process.log
