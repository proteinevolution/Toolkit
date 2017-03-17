hhfilter        -i %alignment.path \
                -o ../results/alignment.a3m \
                -id %max_seqid.content \
                -qid %min_seqid_query.content \
                -cov %min_query_cov.content \
                -diff %num_seqs_extract \
                -M 30

reformat_hhsuite.pl a3m fas ../results/alignment.a3m ../results/alignment.fas

reformat_hhsuite.pl fas clu ../results/alignment.fas ../results/alignment.clustalw_aln

# Convert fasta to JSON
fasta2json.py ../results/alignment.fas ../results/alignment.json