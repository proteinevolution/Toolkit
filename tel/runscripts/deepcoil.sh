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
            -d 160 -uc -l 32000
else
      reformatValidator.pl fas fas \
            $(readlink -f %alignment.path) \
            $(readlink -f ../results/${JOBID}.fas) \
            -d 160 -uc -l 32000
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


sed -n '2p' ../results/${JOBID}.fas > ../results/tmp
sed 's/[\.\-]//g' ../results/tmp > ../results/${JOBID}.fseq
CHAR_COUNT=$(wc -m < ../results/${JOBID}.fseq)
sed -i "1 i\>${JOBID}" ../results/${JOBID}.fseq
rm ../results/tmp


if [ ${CHAR_COUNT} -lt "30" ] || [ ${CHAR_COUNT} -gt "500" ] ; then
    echo "#Input sequence should be between 30 and 500." >> ../results/process.log
    false
fi

if [ ${SEQ_COUNT} = 1 ] ; then

    if [ "%pcoils_input_mode.content" = "0"  ] || [ "%pcoils_input_mode.content" = "1"  ]; then

        echo "#Running DeepCoil." >> ../results/process.log

        ${DEEPCOIL}/deepcoil -i ../results/${JOBID}.fseq \
                     -out_path ../results/

        echo "done" >> ../results/process.log

    else

        echo "#Running 3 iterations of PSI-BLAST against nr90 for PSSM generation." >> ../results/process.log

        psiblast -db ${STANDARD}/nr90 \
                 -evalue 0.001 \
                 -num_iterations 3 \
                 -num_threads %THREADS \
                 -query ../results/${JOBID}.fseq \
                 -out_ascii_pssm ../results/${JOBID}.pssm
        echo "done" >> ../results/process.log

        echo "#Running DeepCoil." >> ../results/process.log

        ${DEEPCOIL}/deepcoil -i ../results/${JOBID}.fseq \
                  -out_path ../results/ \
                  -pssm \
                  -pssm_path ../results/
        echo "done" >> ../results/process.log

    fi

else
        echo "#Running DeepCoil." >> ../results/process.log
        psiblast -subject ../results/${JOBID}.fseq \
             -in_msa ../results/${JOBID}.fas \
             -out_ascii_pssm ../results/${JOBID}.pssm

        ${DEEPCOIL}/deepcoil -i ../results/${JOBID}.fseq \
                     -out_path ../results/ \
                     -pssm \
                     -pssm_path ../results/
        echo "done" >> ../results/process.log
fi


${DEEPCOIL}/prepare_deepcoil_gnuplot.pl ../results/${JOBID} ../results/${JOBID}.out