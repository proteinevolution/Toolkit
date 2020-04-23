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

SEQ_COUNT=$(egrep '^>' ../results/tomodel.pir | wc -l)

if [[ ${SEQ_COUNT} -lt "2" ]] ; then
      echo "#Selected template(s) failed our sanity checks. Only PDB entries maybe selected as templates for modelling.\
      Please re-run your job with new templates." >> ../results/process.log
      false
fi
