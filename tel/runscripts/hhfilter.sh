hhfilter        -i %alignment.path \
                -o ../results/output.a3m \
                -id %max_seqid.content \
                -qid %min_seqid_query.content \
                -cov %min_query_cov.content \
                -diff %num_seqs_extract \
                -M 30

reformat.pl     a3m fas \
                ../results/output.a3m \
                ../results/output.fas