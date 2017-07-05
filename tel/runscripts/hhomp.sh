#Create alignment

echo "#Running PSI-BLAST for query MSA and A3M generation." >> ../results/process.log
updateProcessLog
#Check if input is a single sequence or an MSA
INPUT="query"
if [ ${SEQ_COUNT} -gt 1 ] ; then
    INPUT="in_msa"
fi

psiblast -db ${STANDARD}/nre70 \
         -num_iterations %msa_gen_max_iter.content \
         -evalue %hhomp_incl_eval.content \
         -inclusion_ethresh 0.001 \
         -num_threads %THREADS \
         -num_descriptions 20000 \
         -num_alignments 20000 \
         -${INPUT} %alignment.path \
         -out ../results/output_psiblastp.html

#keep results only of the last iteration
shorten_psiblast_output.pl ../results/output_psiblastp.html ../results/output_psiblastp.html

#extract MSA in a3m format
alignhits_html.pl   ../results/output_psiblastp.html ../results/${JOBID}.a3m \
            -Q ../results/${JOBID}.fas \
            -e %hhomp_incl_eval.content \
            -cov %min_cov.content \
            -a3m \
            -no_link \
            -blastplus
echo "done" >> ../results/process.log
updateProcessLog



#Make HMM file
echo "#Making profile HMM from alignment." >> ../results/process.log
updateProcessLog
# dont know if min_cov=0 makes sense
hhmake -cov %min_cov -diff 100 -i ../results/${JOBID}.a3m -o ../results/${JOBID}.hhm

# Calibrate HMM file
echo "#Calibrating query HMM." >> ../results/process.log
updateProcessLog

#HHomp with query HMM against HMM database
hhomp -cpu 2 -v 2 -i ../results/${JOBID}.hhm -d ${CAL_HMM} -cal -%alignment_mode.content %bb_scoring.content

echo "#Searching %hhompdb.content." >> ../results/process.log
updateProcessLog

hhomp -cpu 2 -v 2 -i ../results/${JOBID}.hhm -d '%hhompdb.content' -o ../results/${JOBID}.hhr -p %pmin.content -Z 20000 -B %desc.content -seq 20000 -%alignment_mode.content %bb_scoring.content




# Reformat query into fasta format; 100 most diverse sequences
hhfilter -i $(readlink -f ../results/${JOBID}.a3m) \
         -o $(readlink -f ../results/${JOBID}.rep100.a3m) \
         -diff 100

# Reformat query into fasta format; full alignment
reformat_hhsuite.pl a3m fas \
            $(readlink -f ../results/${JOBID}.rep100.a3m) \
            $(readlink -f ../results/alignment.fas) \
            -d 160

#Visualization
hhviz.pl ${JOBID} ../results/ ../results/  &> /dev/null