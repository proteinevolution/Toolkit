SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [ ${CHAR_COUNT} -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      false
fi

if [ ${SEQ_COUNT} = "0" ] && [ ${FORMAT} = "0" ] ; then
      sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
      CHAR_COUNT=$(wc -m < ../params/alignment1)

      if [ ${CHAR_COUNT} -gt "10000" ] ; then
            echo "#Single protein sequence inputs may not contain more than 10000 characters." >> ../results/process.log
            false
      else
            sed -i "1 i\>${JOBID}" ../params/alignment1
            mv ../params/alignment1 ../params/alignment
      fi
fi

if [ ${FORMAT} = "1" ] ; then
      reformatValidator.pl clu fas \
            $(readlink -f %alignment.path) \
            $(readlink -f ../results/${JOBID}.fas) \
            -d 160 -uc -r -M first -l 32000
else
      reformatValidator.pl fas fas \
            $(readlink -f %alignment.path) \
            $(readlink -f ../results/${JOBID}.fas) \
            -d 160 -uc -r -M first -l 32000
fi

if [ ! -f ../results/${JOBID}.fas ]; then
    echo "#Input is not in aligned FASTA/CLUSTAL format." >> ../results/process.log
    false
fi

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.fas | wc -l)

if [ ${SEQ_COUNT} -gt "2000" ] ; then
      echo "#Input contains more than 2000 sequences." >> ../results/process.log
      false
fi

if [ ${SEQ_COUNT} -gt "1" ] ; then
       echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
else
       echo "#Query is a single protein sequence." >> ../results/process.log
fi
echo "done" >> ../results/process.log

mv ../results/${JOBID}.fas ../params/alignment

if [ "%repper_input_mode.content" = "1" ]; then

        echo "#MSA generation required. Running 1 iteration of PSI-BLAST against nr70." >> ../results/process.log

        INPUT="query"
        if [ ${SEQ_COUNT} -gt 1 ] ; then
            INPUT="in_msa"
        fi

        psiblast -db ${STANDARD}/nr70 \
                 -num_iterations 1 \
                 -evalue 0.0001 \
                 -inclusion_ethresh 0.0001 \
                 -num_threads %THREADS \
                 -${INPUT} %alignment.path \
                 -out ../results/output_psiblastp.html

        #extract MSA in a3m format
        alignhits_html.pl   ../results/output_psiblastp.html ../results/${JOBID}.in \
                    -Q %alignment.path \
                    -e 0.0001\
                    -b 1.0 \
                    -a3m \
                    -no_link \
                    -blastplus

        hhfilter -i ../results/${JOBID}.in \
                 -o ../results/${JOBID}.in \
                 -cov 20 \
                 -qid 40

        reformat_hhsuite.pl a3m fas \
                 $(readlink -f ../results/${JOBID}.in) \
                 $(readlink -f ../results/${JOBID}.in) \
                 -uc -num -r

        rm ../results/*.html

        echo "done" >> ../results/process.log

else
        cp ../params/alignment ../results/${JOBID}.in
fi




${REPPERDIR}/deal_with_sequence.pl ../results/${JOBID} %window_size.content %periodicity_min.content \
                                   %periodicity_max.content 0 %ftwin_threshold.content \
                                   ../results/${JOBID}.ftwin_par ../results/${JOBID}.in ../results/${JOBID}.buffer

echo "#Excecuting FTwin." >> ../results/process.log

${REPPERDIR}/complete_profile ../results/${JOBID}.in ../results/${JOBID}.ftwin_par \
                              ../results/${JOBID}.ftwin_plot ${REPPERDIR}/hydro.dat
echo "done" >> ../results/process.log


${REPPERDIR}/sort_by_intensity.pl %window_size.content ../results/${JOBID}


reformat.pl fas a3m \
            $(readlink -f ../results/${JOBID}.in) \
            $(readlink -f ../results/${JOBID}.alignment.a3m) \
            -uc -num -r -M first


echo "#Excecuting PSIPRED." >> ../results/process.log

${REPPERDIR}/addss.pl -i ../results/${JOBID}.alignment.a3m -o ../results/${JOBID}.alignment.ss -t ../results/${JOBID}.horiz
INPUT_MODE=1
FLAG=0

echo "done" >> ../results/process.log

hhmake -i ../results/${JOBID}.alignment.a3m \
       -o ../results/${JOBID}.hhmake.out \
       -pcm 2 -pca 0.5 -pcb 2.5 -cov 20

${REPPERDIR}/deal_with_profile.pl ../results/${JOBID}.hhmake.out ../results/${JOBID}.myhmmmake.out

echo "#Excecuting PCOILS." >> ../results/process.log
cd ../results

run_PCoils -win 14 -prof ${JOBID}.myhmmmake.out < ${JOBID}.buffer > ${JOBID}.ftwin_n14
run_PCoils -win 21 -prof ${JOBID}.myhmmmake.out < ${JOBID}.buffer > ${JOBID}.ftwin_n21
run_PCoils -win 28 -prof ${JOBID}.myhmmmake.out < ${JOBID}.buffer > ${JOBID}.ftwin_n28

cd ../0
echo "done" >> ../results/process.log

echo "#Excecuting REPwin." >> ../results/process.log

${REPPERDIR}/repper64 -i ../results/${JOBID}.buffer \
                      -w %window_size.content \
                      -thr %repwin_threshold.content \
                      -dat ../results/${JOBID}_repper.dat -v 0

echo "done" >> ../results/process.log


echo "#Generating output." >> ../results/process.log
${REPPERDIR}/prepare_for_gnuplot.pl ../results/${JOBID} ../results/${JOBID}.ftwin_ov \
                                    ../results/${JOBID}_repper.dat ${INPUT_MODE} ${FLAG} %periodicity_min.content %periodicity_max.content \
                                    ../results/${JOBID}.ftwin_n14 \
                                    ../results/${JOBID}.ftwin_n21 \
                                    ../results/${JOBID}.ftwin_n28 \
                                    ../results/${JOBID}.horiz

rm ../results/*.a3m ../results/*.out ../results/*.ss ../results/*.ftwin* ../results/*.dat
echo "done" >> ../results/process.log