A3M_INPUT=$(head -1 ../params/alignment | egrep "^#A3M#" | wc -l)

SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [[ ${CHAR_COUNT} -gt "10000000" ]] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      false
fi

if [[ ${A3M_INPUT} = "1" ]] ; then

    sed -i '1d' ../params/alignment
    cp ../params/alignment ../results/${JOBID}.in.a3m

    reformatValidator.pl a3m fas \
           $(readlink -f ../results/${JOBID}.in.a3m) \
           $(readlink -f ../results/${JOBID}.in.fas)

     if [[ ! -f ../results/${JOBID}.in.fas ]]; then
            echo "#Input is not in valid A3M format." >> ../results/process.log
            false
     else
            echo "#Query is in A3M format." >> ../results/process.log
            rm ../results/${JOBID}.in.fas
            echo "done" >> ../results/process.log
     fi

else

    if [[ ${SEQ_COUNT} = "0" ]] && [[ ${FORMAT} = "0" ]] ; then
          sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
          CHAR_COUNT=$(wc -m < ../params/alignment1)

          if [[ ${CHAR_COUNT} -gt "10000" ]] ; then
                echo "#Single protein sequence inputs may not contain more than 10000 characters." >> ../results/process.log
                false
          else
                sed -i "1 i\>${JOBID}" ../params/alignment1
                mv ../params/alignment1 ../params/alignment
          fi
    fi

    if [[ ${FORMAT} = "1" ]] ; then
          reformatValidator.pl clu a3m \
                $(readlink -f %alignment.path) \
                $(readlink -f ../results/${JOBID}.in.a3m) \
                -d 160 -l 32000
    else
          reformatValidator.pl fas a3m \
                $(readlink -f %alignment.path) \
                $(readlink -f ../results/${JOBID}.in.a3m) \
                -d 160 -l 32000
    fi

    if [[ ! -f ../results/${JOBID}.in.a3m ]]; then
        echo "#Input is not in aligned FASTA/CLUSTAL format." >> ../results/process.log
        false
    fi
fi

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.in.a3m | wc -l)

if [[ ${SEQ_COUNT} -gt "10000" ]] ; then
    echo "#Input contains more than 10000 sequences." >> ../results/process.log
    false
fi

if [[ ${SEQ_COUNT} -gt "1" ]] ; then
    echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
else
    echo "#Query is a single protein sequence." >> ../results/process.log
fi

echo "done" >> ../results/process.log

reformatValidator.pl a3m fas \
       $(readlink -f ../results/${JOBID}.in.a3m) \
       $(readlink -f ../results/${JOBID}.in.fas) \
       -d 160 -uc -l 32000

head -n 2 ../results/${JOBID}.in.fas > ../results/firstSeq0.fas
sed 's/[\.\-]//g' ../results/firstSeq0.fas > ../results/firstSeq.fas

echo "#Predicting sequence features." >> ../results/process.log

TMPRED=`tmhmm ../results/firstSeq.fas -short`

run_Coils -c -min_P 0.8 < ../results/firstSeq.fas >& ../results/firstSeq.cc
COILPRED=$(egrep ' 0 in coil' ../results/firstSeq.cc | wc -l)

# Run SignalP; since the source organism is unknown, check all four cases
${BIOPROGS}/tools/signalp/bin/signalp -org 'euk' -format 'short' -fasta ../results/firstSeq.fas -prefix "../results/${JOBID}_euk" -tmp '../results/'
${BIOPROGS}/tools/signalp/bin/signalp -org 'gram+' -format 'short' -fasta ../results/firstSeq.fas -prefix "../results/${JOBID}_gramp" -tmp '../results/'
${BIOPROGS}/tools/signalp/bin/signalp -org 'gram-' -format 'short' -fasta ../results/firstSeq.fas -prefix "../results/${JOBID}_gramn" -tmp '../results/'
${BIOPROGS}/tools/signalp/bin/signalp -org 'arch' -format 'short' -fasta ../results/firstSeq.fas -prefix "../results/${JOBID}_arch" -tmp '../results/'

rm ../results/firstSeq0.fas ../results/firstSeq.cc ../results/${JOBID}.in.fas
echo "done" >> ../results/process.log

echo "#Searching %hhblitsdb.content." >> ../results/process.log

hhblits -cpu %THREADS \
        -i ../results/${JOBID}.in.a3m \
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

rm ../results/${JOBID}.in.*

echo "#Generating output" >> ../results/process.log

#Generate query template alignment
hhmakemodel.pl -i ../results/${JOBID}.hhr -a3m ../results/querytemplateMSA.a3m -p %pmin.content -v 0

reformat_hhsuite.pl a3m a3m \
         $(readlink -f ../results/querytemplateMSA.a3m) \
         $(readlink -f ../results/fullQT.a3m) \
         -d 160 -l 32000

head -n 2000 ../results/fullQT.a3m > ../results/tmp

reformat_hhsuite.pl a3m fas \
         $(readlink -f ../results/tmp) \
         $(readlink -f ../results/reducedQT.fas) \
         -d 160 -l 32000 -uc

reformat_hhsuite.pl fas a3m \
         $(readlink -f ../results/reducedQT.fas) \
         $(readlink -f ../results/reducedQT.a3m) \
         -d 160 -l 32000

sed -i "1 i\#A3M#" ../results/fullQT.a3m
sed -i "1 i\#A3M#" ../results/reducedQT.a3m

rm ../results/tmp ../results/querytemplateMSA.a3m

# Generate Query in JSON
fasta2json.py ../results/reducedQT.fas ../results/querytemplate.json


hhr2json.py "$(readlink -f ../results/${JOBID}.hhr)" > $(readlink -f ../results/results.json)
#Visualization
hhviz.pl ${JOBID} ../results/ ../results/  &> /dev/null


# Reformat query into fasta format; 100 most diverse sequences
hhfilter -i $(readlink -f ../results/${JOBID}.a3m) \
         -o $(readlink -f ../results/reduced.a3m) \
         -diff 100

sed -i "1 i\#A3M#" ../results/reduced.a3m


reformat_hhsuite.pl a3m a3m \
         $(readlink -f ../results/${JOBID}.a3m) \
         $(readlink -f ../results/tmp0) \
         -d 160 -l 32000

mv ../results/${JOBID}.a3m ../results/full.a3m
sed -i "1 i\#A3M#" ../results/full.a3m


head -n 400 ../results/tmp0 > ../results/tmp1

reformat_hhsuite.pl a3m fas \
         $(readlink -f ../results/tmp1) \
         $(readlink -f ../results/alignment.fas) \
         -d 160 -l 32000 -uc

rm ../results/tmp0 ../results/tmp1

#create full alignment json; use for forwarding
fasta2json.py ../results/alignment.fas ../results/reduced.json

# Create a JSON with -log10(E-values) of the hits
extract_from_json.py -tool hhblits ../results/results.json ../results/plot_data.json

# add DB to json
manipulate_json.py -k 'db' -v '%hhblitsdb.content' ../results/results.json

# add transmembrane prediction info to json
manipulate_json.py -k 'tmpred' -v "${TMPRED}" ../results/results.json

# add coiled coil prediction info to json
manipulate_json.py -k 'coilpred' -v "${COILPRED}" ../results/results.json

# Write results of signal peptide prediction
SIGNALP=$(grep 'SP(Sec/SPI)' ../results/*.signalp5 | wc -l)
if [[ ${SIGNALP} -gt "4" ]]; then
    manipulate_json.py -k 'signal' -v "1" ../results/results.json
else
    manipulate_json.py -k 'signal' -v "0" ../results/results.json
fi

rm ../results/*.signalp5

# Generate Query in JSON
fasta2json.py ../results/firstSeq.fas ../results/query.json

echo "done" >> ../results/process.log
