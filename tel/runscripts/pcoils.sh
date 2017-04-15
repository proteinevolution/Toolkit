JOBID=%jobid.content
SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

WEIGHTING_MODE=""
declare -a COMMAND_TO_RUN_SINGLE=(run_Coils_iterated run_Coils_pdb run_Coils run_Coils_old)
declare -a COMMAND_TO_RUN_MSA=(run_PCoils_iterated run_PCoils_pdb run_PCoils run_PCoils_old)

reformat_hhsuite.pl fas fas %alignment.path %alignment.path -uc -r -M first


if [ "%pcoils_weighting.content" = "0" ]; then
    WEIGHTING_MODE="-nw"
fi

if [ "%pcoils_input_mode.content" = "2" ]; then

        INPUT="query"
        if [ $SEQ_COUNT -gt 1 ] ; then
            INPUT="in_msa"
        fi

        psiblast -db ${STANDARDNEW}/nr_euk \
                 -num_iterations 1 \
                 -evalue 0.0001 \
                 -inclusion_ethresh 0.0001 \
                 -num_threads %THREADS \
                 -num_descriptions 10000 \
                 -num_alignments 10000 \
                 -html \
                 -${INPUT} %alignment.path \
                 -out ../results/output_psiblastp.html

        #extract MSA in a3m format
        alignhits_html.pl   ../results/output_psiblastp.html ../params/${JOBID}.in \
                    -Q %alignment.path \
                    -e 0.0001\
                    -b 1.0 \
                    -cov 40 \
                    -fas \
                    -no_link \
                    -blastplus
else
        cp ../params/alignment ../params/${JOBID}.in
fi

deal_with_sequence.pl ../params/${JOBID} ../params/${JOBID}.in  ../results/${JOBID}.buffer
cp ../params/${JOBID}.deal_with_sequence ../results

if [ "%pcoils_input_mode.content" = "0" ]; then
        ${COMMAND_TO_RUN_SINGLE[%pcoils_matrix.content]} ${WEIGHTING_MODE} -win 14 < ../results/${JOBID}.buffer > ../results/${JOBID}.coils_n14
        ${COMMAND_TO_RUN_SINGLE[%pcoils_matrix.content]} ${WEIGHTING_MODE} -win 21 < ../results/${JOBID}.buffer > ../results/${JOBID}.coils_n21
        ${COMMAND_TO_RUN_SINGLE[%pcoils_matrix.content]} ${WEIGHTING_MODE} -win 28 < ../results/${JOBID}.buffer > ../results/${JOBID}.coils_n28
        prepare_coils_gnuplot.pl ../results/${JOBID} ../results/${JOBID}.coils_n14 ../results/${JOBID}.coils_n21 ../results/${JOBID}.coils_n28
        create_numerical.rb -i ../results/${JOBID} -m %pcoils_matrix.content -s ../params/${JOBID}.in -w %pcoils_weighting.content
fi


if [ "%pcoils_input_mode.content" = "1" ] || [ "%pcoils_input_mode.content" = "2" ]; then
         reformat_hhsuite.pl fas a3m \
         $(readlink -f ../params/${JOBID}.in) \
         $(readlink -f ../results/${JOBID}.alignment.a3m) \
         -uc -num -r -M first
         ${COILSDIR}/hhmake -i ../results/${JOBID}.alignment.a3m \
                -o ../results/${JOBID}.hhmake.out \
                -pcm 2 -pca 0.5 -pcb 2.5 -cov 20
         deal_with_profile.pl ../results/${JOBID}.hhmake.out ../results/${JOBID}.myhmmmake.out

         ${COMMAND_TO_RUN_MSA[%pcoils_matrix.content]} ${WEIGHTING_MODE} -win 14 -prof ../results/${JOBID}.myhmmmake.out < ../results/${JOBID}.buffer > ../results/${JOBID}.coils_n14
         ${COMMAND_TO_RUN_MSA[%pcoils_matrix.content]} ${WEIGHTING_MODE} -win 21 -prof ../results/${JOBID}.myhmmmake.out < ../results/${JOBID}.buffer > ../results/${JOBID}.coils_n21
         ${COMMAND_TO_RUN_MSA[%pcoils_matrix.content]} ${WEIGHTING_MODE} -win 28 -prof ../results/${JOBID}.myhmmmake.out < ../results/${JOBID}.buffer > ../results/${JOBID}.coils_n28
         prepare_for_gnuplot.pl ../results/${JOBID} F 2 ../results/${JOBID}.coils_n14 ../results/${JOBID}.coils_n21 ../results/${JOBID}.coils_n28 ../results/${JOBID}.horiz
         create_numerical.rb -i ../results/${JOBID} -m %pcoils_matrix.content -a ../params/${JOBID}.in -w %pcoils_weighting.content
fi