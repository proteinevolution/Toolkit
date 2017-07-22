JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)
FORMAT=$(head -1 ../params/alignment | egrep "^CLUSTAL" | wc -l)

if [ ${CHAR_COUNT} -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      updateProcessLog
      false
fi

if [ ${SEQ_COUNT} = "0" ] && [ ${FORMAT} = "0" ] ; then
      sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
      CHAR_COUNT=$(wc -m < ../params/alignment1)

      if [ ${CHAR_COUNT} -gt "10000" ] ; then
            echo "#Single protein sequence inputs may not contain more than 10000 characters." >> ../results/process.log
            updateProcessLog
            false
      else
            sed -i "1 i\>Q_${JOBID}" ../params/alignment1
            mv ../params/alignment1 ../params/alignment
      fi
fi

if [ ${FORMAT} = "1" ] ; then
      reformatValidator.pl clu fas \
            $(readlink -f %alignment.path) \
            $(readlink -f ../results/${JOBID}.fas) \
            -d 160 -uc -l 32000
else
      reformatValidator.pl fas fas \
            $(readlink -f %alignment.path) \
            $(readlink -f ../results/${JOBID}.fas) \
            -d 160 -uc -l 32000
fi

if [ ! -f ../results/${JOBID}.fas ]; then
    echo "#Input is not in aligned FASTA/CLUSTAL format." >> ../results/process.log
    updateProcessLog
    false
fi

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.fas | wc -l)

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

head -n 2 ../results/${JOBID}.fas > ../results/tmp
sed 's/[\.\-]//g' ../results/tmp > ../results/${JOBID}.seq
rm ../results/tmp


#CHECK IF MSA generation is required or not
if [ "%quick_iters.content" = "0" ] && [ ${SEQ_COUNT} -gt "1" ] ; then
        echo "#No MSA generation required for building A3M." >> ../results/process.log
        updateProcessLog

        cp ../results/${JOBID}.fas ../results/${JOBID}.aln

        reformat_hhsuite.pl fas a3m \
            $(readlink -f ../results/${JOBID}.aln) \
            $(readlink -f ../results/${JOBID}.a3m) \
            -d 160 -uc -num -r -M first

        psiblast -subject ../results/${JOBID}.seq \
             -in_msa ../results/${JOBID}.aln \
            -out_ascii_pssm ../results/${JOBID}.pssm

        sed 's/[\.\-]//g' ../results/${JOBID}.aln > ../results/sequencedb

        formatdb -p T -i ../results/sequencedb



        echo "done" >> ../results/process.log
        updateProcessLog
else
    #MSA generation required
    #Check what method to use (PSI-BLAST? HHblits?)

        echo "#Query MSA generation required." >> ../results/process.log
        updateProcessLog
        echo "done" >> ../results/process.log
        updateProcessLog

        echo "#Running PSI-BLAST for query MSA and A3M generation." >> ../results/process.log
        updateProcessLog
        #Check if input is a single sequence or an MSA
        INPUT="query"
        if [ ${SEQ_COUNT} -gt 1 ] ; then
            INPUT="in_msa"
        fi

        psiblast -db ${STANDARD}/%target_psi_db.content \
                 -num_iterations %quick_iters.content \
                 -num_threads %THREADS \
                 -${INPUT} ../results/${JOBID}.fas \
                 -out ../results/output_psiblastp.html \
                 -out_ascii_pssm ../results/${JOBID}.pssm

        #keep results only of the last iteration
        shorten_psiblast_output.pl ../results/output_psiblastp.html ../results/output_psiblastp.html

        #extract MSA in a3m format
        alignhits_html.pl   ../results/output_psiblastp.html ../results/${JOBID}.aln \
                    -Q ../results/${JOBID}.seq \
                    -fas \
                    -no_link \
                    -blastplus

        reformat_hhsuite.pl fas a3m \
         $(readlink -f ../results/${JOBID}.aln) \
         $(readlink -f ../results/${JOBID}.a3m) \
         -d 160 -uc -num -r -M first

        seq2id.pl ../results/${JOBID}.a3m ../results/tmp > ../results/${JOBID}.ids

        seq_retrieve.pl -i ../results/${JOBID}.ids \
                -o ../results/sequencedb \
                -d %STANDARD/%target_psi_db.content \
                -unique T

        formatdb -p T -i ../results/sequencedb


        echo "done" >> ../results/process.log
        updateProcessLog
fi

echo "#Executing PSIPRED." >> ../results/process.log
updateProcessLog

addss.pl ../results/${JOBID}.a3m

echo "done" >> ../results/process.log
updateProcessLog

echo "#Executing SPIDER2 and SPOT-Disorder." >> ../results/process.log
updateProcessLog

#RUN SPOT-D and SPIDER2

cd ../results/
${SPOTD}/run_local.sh ${JOBID}.pssm
cd ../0/

echo "done" >> ../results/process.log
updateProcessLog

echo "#Executing IUpred." >> ../results/process.log
updateProcessLog

#RUN IUPred
iupred ../results/${JOBID}.seq long > ../results/${JOBID}.iupred_dat

echo "done" >> ../results/process.log
updateProcessLog

echo "#Executing DISOPRED3." >> ../results/process.log
updateProcessLog

run_disopred.pl ../results/${JOBID}.seq ../results/sequencedb

echo "done" >> ../results/process.log
updateProcessLog

echo "#Executing MARCOIL." >> ../results/process.log
updateProcessLog

#Running MARCOIL for coiled coil prediction
# Switch on correct Matrix
matrix_copy.sh "${MARCOILMTIDK}" ../0/R5.MTIDK
PARAMMATRIX="-C -i"
TRANSPROB="${MARCOILINPUT}/R3.transProbHigh"

marcoil  ${PARAMMATRIX} \
                      +dssSl \
                      -T ${TRANSPROB} \
                      -E ${MARCOILINPUT}/R2.emissProb \
                      -P "$(readlink -f ../results/${JOBID}.seq)" \
                      "$(readlink -f ../results/${JOBID}.seq)"

echo "done" >> ../results/process.log
updateProcessLog

echo "#Executing COILS." >> ../results/process.log
updateProcessLog

#Run COILS
deal_with_sequence.pl ../results/${JOBID} ../results/${JOBID}.seq  ../results/${JOBID}.buffer
run_Coils -win 28 < ../results/${JOBID}.buffer > ../results/${JOBID}.coils_n21

echo "done" >> ../results/process.log
updateProcessLog

echo "#Executing PCOILS." >> ../results/process.log
updateProcessLog

#Run PCOILS
${COILSDIR}/hhmake -i ../results/${JOBID}.a3m \
                -o ../results/${JOBID}.hhmake.out \
                -pcm 2 -pca 0.5 -pcb 2.5 -cov 20
deal_with_profile.pl ../results/${JOBID}.hhmake.out ../results/${JOBID}.myhmmmake.out
run_PCoils -win 28 -prof ../results/${JOBID}.myhmmmake.out < ../results/${JOBID}.buffer > ../results/${JOBID}.pcoils_n21

echo "done" >> ../results/process.log
updateProcessLog


echo "#Executing TMHMM." >> ../results/process.log
updateProcessLog

#Run TMHMM
tmhmm ../results/${JOBID}.seq > ../results/${JOBID}.tmhmm_dat

echo "done" >> ../results/process.log
updateProcessLog

echo "#Executing Phobius." >> ../results/process.log
updateProcessLog

#Run PHOBIUS
phobius.pl ../results/${JOBID}.seq > ../results/${JOBID}.phobius_dat

echo "done" >> ../results/process.log
updateProcessLog

echo "#Executing PolyPhobius." >> ../results/process.log
updateProcessLog

#Run POLYPHOBIUS
perl ${POLYPHOBIUS}/jphobius.pl -poly ../results/${JOBID}.aln > ../results/${JOBID}.jphobius_dat

echo "done" >> ../results/process.log
updateProcessLog

echo "#Generating output" >> ../results/process.log
updateProcessLog

parseQ2D.pl ${JOBID}

#Write query sequence without gaps into JSON
fasta2json.py ../results/${JOBID}.seq ../results/query.json

#Initialize JSON
echo "{}" > ../results/${JOBID}.json

#Write PSIPRED results into JSON
PSIPRED="$(sed -n '2{p;q;}' ../results/${JOBID}.a3m)"

ALPHA=0
BETA=0
ALPHA=$(echo ${PSIPRED} | tr -cd "H" | wc -c)
BETA=$(echo ${PSIPRED} | tr -cd "E" | wc -c)

if [ ${ALPHA} -gt "0" ] || [ ${BETA} -gt "0" ] ; then
    manipulate_json.py -k 'psipred' -v "$PSIPRED" ../results/${JOBID}.json
    manipulate_json.py -k 'psipred_conf' -v "" ../results/${JOBID}.json
else
        manipulate_json.py -k 'psipred' -v "" ../results/${JOBID}.json
        manipulate_json.py -k 'psipred_conf' -v "" ../results/${JOBID}.json
fi


#Write SPIDER2 and SPOTD results into JSON
ALPHA=0
BETA=0
ALPHA=$(tr -cd "H" <  ../results/${JOBID}.spider2 | wc -c)
BETA=$(tr -cd "E" <  ../results/${JOBID}.spider2 | wc -c)

if [ ${ALPHA} -gt "0" ] || [ ${BETA} -gt "0" ] ; then
    SS="$(sed -n '1{p;q;}' ../results/${JOBID}.spider2)"
    manipulate_json.py -k 'spider2' -v "$SS" ../results/${JOBID}.json
else
    manipulate_json.py -k 'spider2' -v "" ../results/${JOBID}.json
fi

IDR=$(tr -cd "D" <  ../results/${JOBID}.spot | wc -c)

if [ ${IDR} -gt "0" ] ; then
    SS="$(sed -n '1{p;q;}' ../results/${JOBID}.spot)"
    manipulate_json.py -k 'spot-d' -v "$SS" ../results/${JOBID}.json
else
    manipulate_json.py -k 'spot-d' -v "" ../results/${JOBID}.json
fi

echo "done" >> ../results/process.log
updateProcessLog


#Write IUPred results into JSON
IDR=$(tr -cd "D" <  ../results/${JOBID}.iupred | wc -c)

if [ ${IDR} -gt "0" ] ; then
    SS="$(sed -n '1{p;q;}' ../results/${JOBID}.iupred)"
    manipulate_json.py -k 'iupred' -v "$SS" ../results/${JOBID}.json
else
    manipulate_json.py -k 'iupred' -v "" ../results/${JOBID}.json
fi

#Write DISORDER3 results into JSON
IDR=$(tr -cd "D" <  ../results/${JOBID}.disopred | wc -c)

if [ ${IDR} -gt "0" ] ; then
    SS="$(sed -n '1{p;q;}' ../results/${JOBID}.disopred)"
    manipulate_json.py -k 'disopred3' -v "$SS" ../results/${JOBID}.json
else
    manipulate_json.py -k 'disopred3' -v "" ../results/${JOBID}.json
fi

#Write MARCOIL results
CC_COUNT=$(tr -cd "C" <  ../results/${JOBID}.marcoil | wc -c)

if [ ${CC_COUNT} -gt "7" ] ; then
    MARCOIL="$(sed -n '1{p;q;}' ../results/${JOBID}.marcoil)"
    manipulate_json.py -k 'marcoil' -v "$MARCOIL" ../results/${JOBID}.json
else
    manipulate_json.py -k 'marcoil' -v "" ../results/${JOBID}.json
fi

#Write COILS results
CC_COUNT=0
CC_COUNT=$(tr -cd "C" <  ../results/${JOBID}.coils | wc -c)

if [ ${CC_COUNT} -gt "7" ] ; then
    COILS="$(sed -n '1{p;q;}' ../results/${JOBID}.coils)"
    manipulate_json.py -k 'coils_w28' -v "$COILS" ../results/${JOBID}.json
else
    manipulate_json.py -k 'coils_w28' -v "" ../results/${JOBID}.json
fi

#Write PCOILS results
CC_COUNT=0
CC_COUNT=$(tr -cd "C" <  ../results/${JOBID}.pcoils | wc -c)

if [ ${CC_COUNT} -gt "7" ] ; then
    PCOILS="$(sed -n '1{p;q;}' ../results/${JOBID}.pcoils)"
    manipulate_json.py -k 'pcoils_w28' -v "$PCOILS" ../results/${JOBID}.json
else
    manipulate_json.py -k 'pcoils_w28' -v "" ../results/${JOBID}.json
fi

#Write TMHMM results
TM_COUNT=0
TM_COUNT=$(tr -cd "M" <  ../results/${JOBID}.tmhmm | wc -c)

if [ ${TM_COUNT} -gt "5" ] ; then
    TMH="$(sed -n '1{p;q;}' ../results/${JOBID}.tmhmm)"
    manipulate_json.py -k 'tmhmm' -v "$TMH" ../results/${JOBID}.json
else
    manipulate_json.py -k 'tmhmm' -v "" ../results/${JOBID}.json
fi

#Write PHOBIUS results
TM_COUNT=0
TM_COUNT=$(tr -cd "M" <  ../results/${JOBID}.phobius | wc -c)

if [ ${TM_COUNT} -gt "5" ] ; then
    TMH="$(sed -n '1{p;q;}' ../results/${JOBID}.phobius)"
    manipulate_json.py -k 'phobius' -v "$TMH" ../results/${JOBID}.json
else
    manipulate_json.py -k 'phobius' -v "" ../results/${JOBID}.json
fi

TM_COUNT=0
TM_COUNT=$(tr -cd "M" <  ../results/${JOBID}.polyphobius | wc -c)

if [ ${TM_COUNT} -gt "5" ] ; then
    TMH="$(sed -n '1{p;q;}' ../results/${JOBID}.polyphobius)"
    manipulate_json.py -k 'polyphobius' -v "$TMH" ../results/${JOBID}.json
else
    manipulate_json.py -k 'polyphobius' -v "" ../results/${JOBID}.json
fi


EUK=$(signalp -t euk -f short ../results/${JOBID}.seq | grep " Y " | wc -l)
GRAMP=$(signalp -t gram+ -f short ../results/${JOBID}.seq | grep " Y " | wc -l)
GRAMN=$(signalp -t gram+ -f short ../results/${JOBID}.seq | grep " Y " | wc -l)
PHOBIUSSIGNAL=$(grep "FT   SIGNAL" ../results/${JOBID}.phobius_dat | wc -l)

if [ ${EUK} -gt "0" ] || [ ${GRAMP} -gt "0" ] || [ ${GRAMN} -gt "0" ] || [ ${PHOBIUSSIGNAL} -gt "0" ] ; then
    TMH="$(sed -n '1{p;q;}' ../results/${JOBID}.tmhmm)"
    manipulate_json.py -k 'signal' -v "yes" ../results/${JOBID}.json
else
    manipulate_json.py -k 'signal' -v "no" ../results/${JOBID}.json
fi

cd ../results/
find . -type f -not -name '*.json' -a -not -name '*.log' -print0 | xargs -0 rm --
echo "done" >> ../results/process.log
updateProcessLog
