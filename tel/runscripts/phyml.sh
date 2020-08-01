SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [[ ${CHAR_COUNT} -gt "10000000" ]] ; then
      echo "#Input may no contain more than 10000000 characters." >> ../results/process.log
      false
fi

if [[ ${SEQ_COUNT} = "0" ]] && [[ ${FORMAT} = "0" ]] ; then
      echo "#Invalid input format. Input should be in aligned FASTA/CLUSTAL format." >> ../results/process.log
      false
fi

if [[ ${FORMAT} = "1" ]] ; then

      OUTFORMAT=$(reformatValidator.pl clu fas \
	        $(readlink -f ../params/alignment) \
            $(readlink -f ../params/alignment) \
            -d 160 -uc -l 32000)
else
      OUTFORMAT=$(reformatValidator.pl fas fas \
	        $(readlink -f ../params/alignment) \
            $(readlink -f ../params/alignment) \
            -d 160 -uc -l 32000)
fi

if [[ "${OUTFORMAT}" = "fas" ]] ; then

    SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
    echo "#Read MSA with ${SEQ_COUNT} sequences." >> ../results/process.log

else
    echo "#Input is not in aligned FASTA/CLUSTAL format." >> ../results/process.log
    false
fi
echo "done" >> ../results/process.log

if [[ ${SEQ_COUNT} -gt "100" ]] ; then
      echo "#Input contains more than 100 sequences." >> ../results/process.log
      false
fi

reformat_phylip.pl -i=fas \
                   -o=phy \
                   -f=$(readlink -f %alignment.path) \
                   -a=$(readlink -f ../results/${JOBID}.phy)

echo "#Running PhyML." >> ../results/process.log

if [[ "%no_replicates.content" -gt 0 ]] ; then
    echo "done" >> ../results/process.log
    echo "#Performing %no_replicates.content bootstrap iterations." >> ../results/process.log
fi

PhyML-3.1_linux64 -i ../results/${JOBID}.phy \
                  -d aa \
                  -m %matrix_phyml.content \
                  -b %no_replicates.content \
                  -a e \
                  -v e \
                  -s SPR

if [[ "%no_replicates.content" -gt "0" ]] ; then
    cat ../results/*phyml_stats.txt ../results/*phy_phyml_boot_stats.txt > ../results/${JOBID}.stats
    rm ../results/*phyml_boot*
    rm ../results/*phyml_stats.txt
else
    mv ../results/*phyml_stats.txt ../results/${JOBID}.stats
fi

mv ../results/${JOBID}.phy_phyml_tree.txt  ../results/${JOBID}.tree

echo "done" >> ../results/process.log
