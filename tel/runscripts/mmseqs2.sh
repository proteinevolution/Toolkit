
#convert input sequences into the MMseqs database format
mmseqs createdb %alignment.path \
                ../results/seqDB \
                --max-seq-len 30000

#create temp directory
mkdir ../results/tmp

#Let the results of clustering be written in the results directory
#cd ../results

#clustering step
mmseqs cluster  ../results/seqDB \
                ../results/clu \
                ../results/tmp \
                --min-seq-id %min_seqid.content \
                -c %min_aln_cov.content \
                --remove-tmp-files \
                --threads %THREADS


#Generate FASTA-style output
mmseqs createseqfiledb ../results/seqDB \
                       ../results/clu \
                       ../results/clu_seq \
                       --threads %THREADS

mmseqs result2flat     ../results/seqDB \
                       ../results/seqDB \
                       ../results/clu_seq \
                        ../results/clu_seq.fa


filtermmseqs.pl -i ../results/clu_seq.fa \
                -o ../results/output

