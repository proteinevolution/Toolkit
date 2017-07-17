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


#CHECK IF MSA generation is required or not
if [ "%quick_iters.content" = "0" ] && [ ${SEQ_COUNT} -gt "1" ] ; then
        echo "#No MSA generation required for building A3M." >> ../results/process.log
        updateProcessLog

        cp ../results/${JOBID}.fas ../results/${JOBID}.aln
else
    #MSA generation required
    #Check what method to use (PSI-BLAST? HHblits?)

        echo "#Query MSA generation required." >> ../results/process.log
        updateProcessLog
        echo "done" >> ../results/process.log
        updateProcessLog
    #MSA generation by PSI-BLAST
        echo "#Running PSI-BLAST for query MSA and A3M generation." >> ../results/process.log
        updateProcessLog
        #Check if input is a single sequence or an MSA
        INPUT="query"
        if [ ${SEQ_COUNT} -gt 1 ] ; then
            INPUT="in_msa"
        fi

        psiblast -db ${STANDARD}/nr_euk70 \
                 -num_iterations %quick_iters.content \
                 -evalue %hhpred_incl_eval.content \
                 -inclusion_ethresh 0.001 \
                 -num_threads %THREADS \
                 -num_descriptions 20000 \
                 -num_alignments 20000 \
                 -${INPUT} ../results/${JOBID}.fas \
                 -out ../results/output_psiblastp.html

        #keep results only of the last iteration
        shorten_psiblast_output.pl ../results/output_psiblastp.html ../results/output_psiblastp.html

        #extract MSA in a3m format
        alignhits_html.pl   ../results/output_psiblastp.html ../results/${JOBID}.aln \
                    -Q ../results/${JOBID}.fas \
                    -e %hhpred_incl_eval.content \
                    -cov 50 \
                    -fas \
                    -no_link \
                    -blastplus
        echo "done" >> ../results/process.log
        updateProcessLog
fi

reformat_hhsuite.pl fas a3m \
         $(readlink -f ../results/${JOBID}.aln) \
         $(readlink -f ../results/${JOBID}.a3m) \
         -d 160 -uc -num -r -M first


addss.pl ../results/${JOBID}.a3m


#Write query sequence without gaps into JSON
fasta2json.py ../results/${JOBID}.fas ../results/query.json


echo "{}" > ../results/${JOBID}.json

#Write PSIPRED results into JSON
PSIPRED="$(sed -n '2{p;q;}' ../results/${JOBID}.a3m)"
manipulate_json.py -k 'psipred' -v "$PSIPRED" ../results/${JOBID}.json
PSIPREDCONF="$(sed -n '4{p;q;}' ../results/${JOBID}.a3m)"
manipulate_json.py -k 'psipred_conf' -v "$PSIPREDCONF" ../results/${JOBID}.json


#Running MARCOIL for coiled coil prediction
# Switch on correct Matrix
#matrix_copy.sh "${MARCOILMTK}" ../0/R5.MTK
matrix_copy.sh "${MARCOILMTIDK}" ../0/R5.MTIDK
PARAMMATRIX="-C -i"
TRANSPROB="${MARCOILINPUT}/R3.transProbHigh"

head -n 2 ../results/${JOBID}.fas > ../results/tmp
sed 's/[\.\-]//g' ../results/tmp > ../results/${JOBID}.seq
rm ../results/tmp

marcoil  ${PARAMMATRIX} \
                      +dssSl \
                      -T ${TRANSPROB} \
                      -E ${MARCOILINPUT}/R2.emissProb \
                      -P "$(readlink -f ../results/${JOBID}.seq)" \
                      "$(readlink -f ../results/${JOBID}.seq)"

#Run COILS
deal_with_sequence.pl ../results/${JOBID} ../results/${JOBID}.seq  ../results/${JOBID}.buffer
run_Coils -win 28 < ../results/${JOBID}.buffer > ../results/${JOBID}.coils_n21

#Run PCOILS
${COILSDIR}/hhmake -i ../results/${JOBID}.a3m \
                -o ../results/${JOBID}.hhmake.out \
                -pcm 2 -pca 0.5 -pcb 2.5 -cov 20
deal_with_profile.pl ../results/${JOBID}.hhmake.out ../results/${JOBID}.myhmmmake.out
run_PCoils -win 28 -prof ../results/${JOBID}.myhmmmake.out < ../results/${JOBID}.buffer > ../results/${JOBID}.pcoils_n21

tmhmm ../results/${JOBID}.seq > ../results/${JOBID}.tmhmm_dat

parseMARCOIL.pl ${JOBID}

CC_COUNT=$(tr -cd "C" <  ../results/${JOBID}.marcoil | wc -c)

if [ ${CC_COUNT} -gt "7" ] ; then
    MARCOIL="$(sed -n '1{p;q;}' ../results/${JOBID}.marcoil)"
    manipulate_json.py -k 'marcoil' -v "$MARCOIL" ../results/${JOBID}.json
else
    manipulate_json.py -k 'marcoil' -v "" ../results/${JOBID}.json
fi
CC_COUNT=0
CC_COUNT=$(tr -cd "C" <  ../results/${JOBID}.coils | wc -c)

if [ ${CC_COUNT} -gt "7" ] ; then
    COILS="$(sed -n '1{p;q;}' ../results/${JOBID}.coils)"
    manipulate_json.py -k 'coils_w28' -v "$COILS" ../results/${JOBID}.json
else
    manipulate_json.py -k 'coils_w28' -v "" ../results/${JOBID}.json
fi

CC_COUNT=0
CC_COUNT=$(tr -cd "C" <  ../results/${JOBID}.pcoils | wc -c)

if [ ${CC_COUNT} -gt "7" ] ; then
    PCOILS="$(sed -n '1{p;q;}' ../results/${JOBID}.pcoils)"
    manipulate_json.py -k 'pcoils_w28' -v "$PCOILS" ../results/${JOBID}.json
else
    manipulate_json.py -k 'pcoils_w28' -v "" ../results/${JOBID}.json
fi

TM_COUNT=0
TM_COUNT=$(tr -cd "M" <  ../results/${JOBID}.tmhmm | wc -c)

if [ ${TM_COUNT} -gt "5" ] ; then
    TMH="$(sed -n '1{p;q;}' ../results/${JOBID}.tmhmm)"
    manipulate_json.py -k 'tmhmm' -v "$TMH" ../results/${JOBID}.json
else
    manipulate_json.py -k 'tmhmm' -v "" ../results/${JOBID}.json
fi