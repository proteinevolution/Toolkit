JOBID=%jobid.content

#convert input sequences into the MMseqs database format
mmseqs createdb %alignment.path \
                ../results/${JOBID}_seqDB \
                --max-seq-len 30000

#create temp directory
mkdir ../results/tmp

#Let the results of clustering be written in the results directory
#cd ../results

#use linear clustering mode
mmseqs linclust  ../results/${JOBID}_seqDB \
                ../results/${JOBID}_clu \
                ../results/tmp \
                --min-seq-id %min_seqid.content \
                -c %min_aln_cov.content \
                --remove-tmp-files \
                --threads %THREADS


#Generate FASTA-style output
mmseqs createseqfiledb ../results/${JOBID}_seqDB \
                       ../results/${JOBID}_clu \
                       ../results/${JOBID}_clu_seq \
                       --threads %THREADS

mmseqs result2flat     ../results/${JOBID}_seqDB \
                       ../results/${JOBID}_seqDB \
                       ../results/${JOBID}_clu_seq \
                        ../results/${JOBID}_clu_seq.fa


filtermmseqs.pl -i ../results/${JOBID}_clu_seq.fa \
                -o ../results/${JOBID}

rm -r ../results/tmp
rm ../results/${JOBID}_*