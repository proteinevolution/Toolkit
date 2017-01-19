java -Xmx3G -jar ${BLAMMERJAR} \
            -conf ${BLAMMERCONF} \
            -infile %alignment.path \
            -coverage %min_query_cov.content \
            -blastmax %max_eval.content \
            -cluwidth %min_anchor_width.content \
            -s/c %min_colscore.content \
            -seqs %max_seqs.content \
            -maxsim %max_seqid.content \
            -html T \
            -oformat clustal \
            -dohmmb f \
            -dohmms f \
            -dohmma f \
            -doext f \
            -dotax f \
            -verbose 2 \
            -hmmer ${HMMERBINARIES} \
            -blastdb ${STANDARDNEW} \
            -taxdir ${TAXONOMY}

# Move the result file to the output directory
mv ../params/alignment.cln ../results/alignment.clustalw_aln

