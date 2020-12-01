SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [[ ${CHAR_COUNT} -gt "10000000" ]] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      false
fi

if [[ ${FORMAT} = "1" ]] || [[ ${SEQ_COUNT} -gt "1" ]] ; then
      echo "#Input is a multiple sequence alignment; expecting a single protein sequence." >> ../results/process.log
      false
fi

if [[ ${SEQ_COUNT} = "0" ]] && [[ ${FORMAT} = "0" ]] ; then
      CHAR_COUNT=$(wc -m < ../params/alignment)

      if [[ ${CHAR_COUNT} -gt "10000" ]] ; then
            echo "#Single protein sequence inputs may not contain more than 10000 characters." >> ../results/process.log
            false
      else
            # Adding a FASTA header line
            sed -i "1 i\>Q_${JOBID}" ../params/alignment
      fi
fi

reformatValidator.pl fas fas \
    $(readlink -f %alignment.path) \
    $(readlink -f ../results/${JOBID}.fas) \
    -d 160 -uc -l 32000

# remove invalid characters from sequence
sed -i "2 s/[^a-z^A-Z]//g" ../results/${JOBID}.fas

if [[ ! -f ../results/${JOBID}.fas ]]; then
    echo "#Input is not in FASTA format." >> ../results/process.log
    false
fi

echo "#Query is a single protein sequence." >> ../results/process.log
echo "done" >> ../results/process.log

sed "1 s/^.*$/>${JOBID}/" ../results/${JOBID}.fas > ../results/${JOBID}.fseq
CHAR_COUNT=$(sed -n '2p' ../results/${JOBID}.fseq | wc -c)

# MSA generation required
# Run PSI-BLAST and HHblits

echo "#Query MSA generation required." >> ../results/process.log
echo "done" >> ../results/process.log
echo "#Running PSI-BLAST for query MSA and A3M generation." >> ../results/process.log

psiblast -db ${STANDARD}/%target_psi_db.content \
         -evalue %evalue.content \
         -num_iterations %quick_iters.content \
         -num_threads %THREADS \
         -query ../results/${JOBID}.fas \
         -out ../results/output_psiblastp.html \
         -num_descriptions 10000 \
         -num_alignments 10000 \
         -max_hsps 1 \
         -out_ascii_pssm ../results/${JOBID}.pssm

# keep results only of the last iteration parse out an alignment
shorten_psiblast_output.pl ../results/output_psiblastp.html ../results/output_psiblastp.html

# extract MSA in a3m format
alignhits_html.pl   ../results/output_psiblastp.html ../results/${JOBID}.aln \
                    -Q ../results/${JOBID}.fas \
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

echo "#Running HHblits for HMM generation." >> ../results/process.log

hhblits -i ../results/${JOBID}.fas \
        -ohhm ../results/${JOBID}.hhm \
        -d %HHBLITS/UniRef30 \
        -n 2 \
        -v 0 \
        -maxres 40000 \
        -cpu %THREADS -Z 0 \
        -o /dev/null
echo "done" >> ../results/process.log


# Run SignalP; since the source organism is unknown, check all four cases
echo "#Executing SignalP." >> ../results/process.log
${BIOPROGS}/tools/signalp/bin/signalp -org 'euk' -format 'short' -fasta ../results/${JOBID}.fas -prefix "../results/${JOBID}_euk" -tmp '../results/'
${BIOPROGS}/tools/signalp/bin/signalp -org 'gram+' -format 'short' -fasta ../results/${JOBID}.fas -prefix "../results/${JOBID}_gramp" -tmp '../results/'
${BIOPROGS}/tools/signalp/bin/signalp -org 'gram-' -format 'short' -fasta ../results/${JOBID}.fas -prefix "../results/${JOBID}_gramn" -tmp '../results/'
${BIOPROGS}/tools/signalp/bin/signalp -org 'arch' -format 'short' -fasta ../results/${JOBID}.fas -prefix "../results/${JOBID}_arch" -tmp '../results/'
echo "done" >> ../results/process.log

echo "#Executing PSIPRED." >> ../results/process.log

addss.pl ../results/${JOBID}.a3m

echo "done" >> ../results/process.log

echo "#Executing PSSpred4." >> ../results/process.log

cd ../results/
${BIOPROGS}/tools/PSSpred/PSSpred.pl ../results/${JOBID}.fas ../results/sequencedb
cd ../0/

echo "done" >> ../results/process.log


echo "#Executing DeepCNF-SS." >> ../results/process.log

cd ../results/
hhmake -i ${JOBID}.a3m -o ${JOBID}.feat

${DEEPCNF}/DeepCNF_SS.sh -i ${JOBID}.fas \
                         -d sequencedb \
                         -t "${PWD}" \
                         -c %THREADS
cd ../0/

echo "done" >> ../results/process.log

echo "#Executing NetSurfP2" >> ../results/process.log
python3 ${BIOPROGS}/tools/netsurfp2/from_hhm.py ../results/${JOBID}
echo "done" >> ../results/process.log

# PiPred, SPOT-D and SPIDER3 require the PSSM file to exist
if [[ -f ../results/${JOBID}.pssm ]]; then
    #RUN PiPred
    if [[ ${CHAR_COUNT} -gt "30" ]] && [[ ${CHAR_COUNT} -lt "700" ]] ; then
        echo "#Executing PiPred." >> ../results/process.log
        source $PIPRED/pipred_env/bin/activate
        ${PIPRED}/pipred.py -i ../results/${JOBID}.fseq \
            -pssm_path ../results/ \
            -out_path ../results/
        deactivate
        echo "done" >> ../results/process.log
    fi
    
    #RUN SPOT-D and SPIDER3
    echo "#Executing SPIDER3 and SPOT-Disorder." >> ../results/process.log
    cd ../results/
    ${SPOTD}/run_local.sh ${JOBID}.pssm
    python2 ${BIOPROGS}/tools/SPIDER3-numpy-server/script/spider3_pred.py ${JOBID} --odir .
    cd ../0/
    echo "done" >> ../results/process.log   
fi

echo "#Executing IUpred." >> ../results/process.log

#RUN IUPred
iupred ../results/${JOBID}.fas long > ../results/${JOBID}.iupred_dat

echo "done" >> ../results/process.log

echo "#Executing DISOPRED3." >> ../results/process.log

run_disopred.pl ../results/${JOBID}.fas ../results/sequencedb

echo "done" >> ../results/process.log

echo "#Executing MARCOIL." >> ../results/process.log

#Running MARCOIL for coiled coil prediction
# Switch on correct Matrix
matrix_copy.sh "${MARCOILMTK}" ../0/R5.MTK
matrix_copy.sh "${MARCOILMTIDK}" ../0/R5.MTIDK

                marcoil  -C \
                      +dssSl \
                      -T ${MARCOILINPUT}/R3.transProbHigh \
                      -E ${MARCOILINPUT}/R2.emissProb \
                      -P ${JOBID}.fas \
                      ../results/${JOBID}.fas
mv *.fas.* ../results/
echo "done" >> ../results/process.log

echo "#Executing COILS." >> ../results/process.log

#Run COILS
deal_with_sequence.pl ../results/${JOBID} ../results/${JOBID}.fas  ../results/${JOBID}.buffer
run_Coils -win 28 < ../results/${JOBID}.buffer > ../results/${JOBID}.coils_n21

echo "done" >> ../results/process.log

echo "#Executing PCOILS." >> ../results/process.log

#Run PCOILS
${COILSDIR}/hhmake -i ../results/${JOBID}.a3m \
                -o ../results/${JOBID}.hhmake.out \
                -pcm 2 -pca 0.5 -pcb 2.5 -cov 20
deal_with_profile.pl ../results/${JOBID}.hhmake.out ../results/${JOBID}.myhmmmake.out
run_PCoils -win 28 -prof ../results/${JOBID}.myhmmmake.out < ../results/${JOBID}.buffer > ../results/${JOBID}.pcoils_n21

echo "done" >> ../results/process.log

echo "#Executing TMHMM." >> ../results/process.log

#Run TMHMM
tmhmm ../results/${JOBID}.fas > ../results/${JOBID}.tmhmm_dat

echo "done" >> ../results/process.log

echo "#Executing Phobius." >> ../results/process.log

#Run PHOBIUS
phobius.pl ../results/${JOBID}.fas > ../results/${JOBID}.phobius_dat

echo "done" >> ../results/process.log

echo "#Executing PolyPhobius." >> ../results/process.log

#Run POLYPHOBIUS
sed -i 's/[JOU]/X/g' ../results/${JOBID}.aln
perl ${POLYPHOBIUS}/jphobius.pl -poly ../results/${JOBID}.aln > ../results/${JOBID}.jphobius_dat

echo "done" >> ../results/process.log

echo "#Generating output" >> ../results/process.log

parseQ2D.pl ${JOBID}

#Write query sequence without gaps into JSON
echo "{}" > ../results/query.json
manipulate_json.py -k 'header' -v "$(sed -n '1{p;q;}' ../results/${JOBID}.fas)" ../results/query.json
manipulate_json.py -k 'sequence' -v "$(sed -n '2{p;q;}' ../results/${JOBID}.fas)" ../results/query.json

#fasta2json.py ../results/${JOBID}.fas ../results/query.json

#Initialize JSON
echo "{}" > ../results/results.json

#Write PSIPRED results into JSON
PSIPRED="$(sed -n '2{p;q;}' ../results/${JOBID}.a3m)"

ALPHA=0
BETA=0
ALPHA=$(echo ${PSIPRED} | tr -cd "H" | wc -c)
BETA=$(echo ${PSIPRED} | tr -cd "E" | wc -c)

if [[ ${ALPHA} -gt "0" ]] || [[ ${BETA} -gt "0" ]] ; then
    manipulate_json.py -k 'psipred' -v "$PSIPRED" ../results/results.json
    manipulate_json.py -k 'psipred_conf' -v "" ../results/results.json
else
        manipulate_json.py -k 'psipred' -v "" ../results/results.json
        manipulate_json.py -k 'psipred_conf' -v "" ../results/results.json
fi

#Write PSSPRED results into JSON
ALPHA=0
BETA=0
ALPHA=$(tr -cd "H" <  ../results/${JOBID}.psspred | wc -c)
BETA=$(tr -cd "E" <  ../results/${JOBID}.psspred | wc -c)

if [[ ${ALPHA} -gt "0" ]] || [[ ${BETA} -gt "0" ]] ; then
    SS="$(sed -n '1{p;q;}' ../results/${JOBID}.psspred)"
    manipulate_json.py -k 'psspred' -v "$SS" ../results/results.json
else
    manipulate_json.py -k 'psspred' -v "" ../results/results.json
fi

#Write PiPred results into JSON
PI=$(tr -cd "I" <  ../results/${JOBID}.pipred | wc -c)

if [[ ${PI} -gt "0" ]] ; then
    SS="$(sed -n '1{p;q;}' ../results/${JOBID}.pipred)"
    manipulate_json.py -k 'pipred' -v "$SS" ../results/results.json
else
    manipulate_json.py -k 'pipred' -v "" ../results/results.json
fi

#Write DeepCNF results into JSON
ALPHA=0
BETA=0
ALPHA=$(tr -cd "H" <  ../results/${JOBID}.deepcnf | wc -c)
BETA=$(tr -cd "E" <  ../results/${JOBID}.deepcnf | wc -c)

if [[ ${ALPHA} -gt "0" ]] || [[ ${BETA} -gt "0" ]] ; then
    SS="$(sed -n '1{p;q;}' ../results/${JOBID}.deepcnf)"
    manipulate_json.py -k 'deepcnf' -v "$SS" ../results/results.json
else
    manipulate_json.py -k 'deepcnf' -v "" ../results/results.json
fi

#Write NetSurfP2 results into JSON
ALPHA=0
BETA=0
ALPHA=$(tr -cd "H" <  ../results/${JOBID}.netsurfpss | wc -c)
BETA=$(tr -cd "E" <  ../results/${JOBID}.netsurfpss | wc -c)

if [[ ${ALPHA} -gt "0" ]] || [[ ${BETA} -gt "0" ]] ; then
    SS="$(sed -n '1{p;q;}' ../results/${JOBID}.netsurfpss)"
    manipulate_json.py -k 'netsurfpss' -v "$SS" ../results/results.json
else
    manipulate_json.py -k 'netsurfpss' -v "" ../results/results.json
fi

IDR=$(tr -cd "D" <  ../results/${JOBID}.netsurfpd | wc -c)

if [[ ${IDR} -gt "0" ]] ; then
    SS="$(sed -n '1{p;q;}' ../results/${JOBID}.netsurfpd)"
    manipulate_json.py -k 'netsurfpd' -v "$SS" ../results/results.json
else
    manipulate_json.py -k 'netsurfpd' -v "" ../results/results.json
fi

#Write SPIDER3 and SPOTD results into JSON
ALPHA=0
BETA=0
ALPHA=$(tr -cd "H" <  ../results/${JOBID}.spider3 | wc -c)
BETA=$(tr -cd "E" <  ../results/${JOBID}.spider3 | wc -c)

if [[ ${ALPHA} -gt "0" ]] || [[ ${BETA} -gt "0" ]] ; then
    SS="$(sed -n '1{p;q;}' ../results/${JOBID}.spider3)"
    manipulate_json.py -k 'spider' -v "$SS" ../results/results.json
else
    manipulate_json.py -k 'spider' -v "" ../results/results.json
fi

IDR=$(tr -cd "D" <  ../results/${JOBID}.spot | wc -c)

if [[ ${IDR} -gt "0" ]] ; then
    SS="$(sed -n '1{p;q;}' ../results/${JOBID}.spot)"
    manipulate_json.py -k 'spot-d' -v "$SS" ../results/results.json
else
    manipulate_json.py -k 'spot-d' -v "" ../results/results.json
fi

echo "done" >> ../results/process.log


#Write IUPred results into JSON
IDR=$(tr -cd "D" <  ../results/${JOBID}.iupred | wc -c)

if [[ ${IDR} -gt "0" ]] ; then
    SS="$(sed -n '1{p;q;}' ../results/${JOBID}.iupred)"
    manipulate_json.py -k 'iupred' -v "$SS" ../results/results.json
else
    manipulate_json.py -k 'iupred' -v "" ../results/results.json
fi

#Write DISORDER3 results into JSON
IDR=$(tr -cd "D" <  ../results/${JOBID}.disopred | wc -c)

if [[ ${IDR} -gt "0" ]] ; then
    SS="$(sed -n '1{p;q;}' ../results/${JOBID}.disopred)"
    manipulate_json.py -k 'disopred' -v "$SS" ../results/results.json
else
    manipulate_json.py -k 'disopred' -v "" ../results/results.json
fi

#Write MARCOIL results
CC_COUNT=$(tr -cd "C" <  ../results/${JOBID}.marcoil | wc -c)

if [[ ${CC_COUNT} -gt "7" ]] ; then
    MARCOIL="$(sed -n '1{p;q;}' ../results/${JOBID}.marcoil)"
    manipulate_json.py -k 'marcoil' -v "$MARCOIL" ../results/results.json
else
    manipulate_json.py -k 'marcoil' -v "" ../results/results.json
fi

#Write COILS results
CC_COUNT=0
CC_COUNT=$(tr -cd "C" <  ../results/${JOBID}.coils | wc -c)

if [[ ${CC_COUNT} -gt "7" ]] ; then
    COILS="$(sed -n '1{p;q;}' ../results/${JOBID}.coils)"
    manipulate_json.py -k 'coils_w28' -v "$COILS" ../results/results.json
else
    manipulate_json.py -k 'coils_w28' -v "" ../results/results.json
fi

#Write PCOILS results
CC_COUNT=0
CC_COUNT=$(tr -cd "C" <  ../results/${JOBID}.pcoils | wc -c)

if [[ ${CC_COUNT} -gt "7" ]] ; then
    PCOILS="$(sed -n '1{p;q;}' ../results/${JOBID}.pcoils)"
    manipulate_json.py -k 'pcoils_w28' -v "$PCOILS" ../results/results.json
else
    manipulate_json.py -k 'pcoils_w28' -v "" ../results/results.json
fi

#Write TMHMM results
TM_COUNT=0
TM_COUNT=$(tr -cd "M" <  ../results/${JOBID}.tmhmm | wc -c)

if [[ ${TM_COUNT} -gt "5" ]] ; then
    TMH="$(sed -n '1{p;q;}' ../results/${JOBID}.tmhmm)"
    manipulate_json.py -k 'tmhmm' -v "$TMH" ../results/results.json
else
    manipulate_json.py -k 'tmhmm' -v "" ../results/results.json
fi

#Write PHOBIUS results
TM_COUNT=0
TM_COUNT=$(tr -cd "M" <  ../results/${JOBID}.phobius | wc -c)

if [[ ${TM_COUNT} -gt "5" ]] ; then
    TMH="$(sed -n '1{p;q;}' ../results/${JOBID}.phobius)"
    manipulate_json.py -k 'phobius' -v "$TMH" ../results/results.json
else
    manipulate_json.py -k 'phobius' -v "" ../results/results.json
fi

TM_COUNT=0
TM_COUNT=$(tr -cd "M" <  ../results/${JOBID}.polyphobius | wc -c)

if [[ ${TM_COUNT} -gt "5" ]] ; then
    TMH="$(sed -n '1{p;q;}' ../results/${JOBID}.polyphobius)"
    manipulate_json.py -k 'polyphobius' -v "$TMH" ../results/results.json
else
    manipulate_json.py -k 'polyphobius' -v "" ../results/results.json
fi

# Write results of signal peptide prediction
PHOBIUSSIGNAL=$(grep "FT   SIGNAL" ../results/${JOBID}.phobius_dat | wc -l)
SIGNALP=$(grep 'SP(Sec/SPI)' ../results/*.signalp5 | wc -l)

if [[ ${SIGNALP} -gt "4" ]] || [[ ${PHOBIUSSIGNAL} -gt "0" ]] ; then
    TMH="$(sed -n '1{p;q;}' ../results/${JOBID}.tmhmm)"
    manipulate_json.py -k 'signal' -v "yes" ../results/results.json
else
    manipulate_json.py -k 'signal' -v "no" ../results/results.json
fi

cd ../results/
find . -type f -not -name '*.json' -a -not -name '*.log' -print0 | xargs -0 rm --
echo "done" >> ../results/process.log
