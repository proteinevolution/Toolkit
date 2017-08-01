JOBID=%jobid.content
A3M_INPUT=$(head -1 ../params/alignment | egrep "^#A3M#" | wc -l)

SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)
FORMAT=$(head -1 ../params/alignment | egrep "^CLUSTAL" | wc -l)

if [ ${CHAR_COUNT} -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      updateProcessLog
      false
fi


if [ ${A3M_INPUT} = "1" ] ; then

    sed -i '1d' ../params/alignment
    cp ../params/alignment ../results/${JOBID}.in.a3m

    reformatValidator.pl a3m fas \
           $(readlink -f ../results/${JOBID}.in.a3m) \
           $(readlink -f ../results/${JOBID}.in.fas) \
           -d 160 -uc -l 32000

     if [ ! -f ../results/${JOBID}.in.fas ]; then
            echo "#Input is not in valid A3M format." >> ../results/process.log
            updateProcessLog
            false
     else
            echo "#Query is in A3M format." >> ../results/process.log
            updateProcessLog
            echo "done" >> ../results/process.log
            updateProcessLog
     fi

else

    if [ ${SEQ_COUNT} = "0" ] && [ ${FORMAT} = "0" ] ; then
          sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
          CHAR_COUNT=$(wc -m < ../params/alignment1)

          if [ ${CHAR_COUNT} -gt "10000" ] ; then
                echo "#Single protein sequence inputs may not contain more than 10000 characters." >> ../results/process.log
                updateProcessLog
                false
          else
                sed -i "1 i\>${JOBID}" ../params/alignment1
                mv ../params/alignment1 ../params/alignment
          fi
    fi

    if [ ${FORMAT} = "1" ] ; then
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
        updateProcessLog
        false
    fi

    SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.in.fas | wc -l)

    if [ ${SEQ_COUNT} -gt "10000" ] ; then
          echo "#Input contains more than 10000 sequences." >> ../results/process.log
          updateProcessLog
          false
    fi

    if [ ${SEQ_COUNT} -gt "1" ] ; then
           echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
           updateProcessLog
    else
           echo "#Query is a single protein sequence." >> ../results/process.log
           updateProcessLog
    fi


    echo "done" >> ../results/process.log
    updateProcessLog

fi
echo "#Searching %hhblitsdb.content." >> ../results/process.log
updateProcessLog



head -n 2 ../results/${JOBID}.in.fas > ../results/firstSeq0.fas
sed 's/[\.\-]//g' ../results/firstSeq0.fas > ../results/firstSeq.fas

TMPRED=`tmhmm ../results/firstSeq.fas -short`

run_Coils -c -min_P 0.8 < ../results/firstSeq.fas >& ../results/firstSeq.cc
COILPRED=$(egrep ' 0 in coil' ../results/firstSeq.cc | wc -l)

rm ../results/firstSeq0.fas ../results/firstSeq.cc



if [ ${A3M_INPUT} = "1" ] ; then
    INPUT="../results/${JOBID}.in.a3m"
else
    INPUT="../results/${JOBID}.in.fas  -M first"
fi

hhblits -cpu %THREADS \
        -i ${INPUT} \
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
updateProcessLog

rm ../results/${JOBID}.in.*

echo "#Generating output" >> ../results/process.log
updateProcessLog

#Generate query template alignment
hhmakemodel.pl -i ../results/${JOBID}.hhr -fas ../results/querytemplateMSA.fas -p %pmin.content
# Generate Query in JSON
fasta2json.py ../results/querytemplateMSA.fas ../results/querytemplate.json


hhr2json.py "$(readlink -f ../results/${JOBID}.hhr)" > $(readlink -f ../results/${JOBID}.json)
#json2fasta.py ../results/${JOBID}.json ../results/${JOBID}.fasta
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

head -n 400 ../results/tmp0 > ../results/tmp1

reformat_hhsuite.pl a3m fas \
         $(readlink -f ../results/tmp1) \
         $(readlink -f ../results/alignment.fas) \
         -d 160 -l 32000 -uc

rm ../results/tmp0 ../results/tmp1

#create full alignment json; use for forwarding
fasta2json.py ../results/alignment.fas ../results/reduced.json

# add DB to json
manipulate_json.py -k 'db' -v '%hhblitsdb.content' ../results/${JOBID}.json

# add transmembrane prediction info to json
manipulate_json.py -k 'TMPRED' -v "${TMPRED}" ../results/${JOBID}.json

# add coiled coil prediction info to json
manipulate_json.py -k 'COILPRED' -v "${COILPRED}" ../results/${JOBID}.json

# Generate Query in JSON
fasta2json.py ../results/firstSeq.fas ../results/query.json

echo "done" >> ../results/process.log
updateProcessLog
