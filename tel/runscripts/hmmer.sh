
#CHECK IF MSA generation is required or not

SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

if [ %max_hhblits_iter.content == "0" -a $SEQ_COUNT -gt "1"] ; then
    #Use user MSA to build HMM

    $HMMERPATH/hmmbuild --cpu %THREADS \
             ../params/infile_hmm \
             %alignment.path
else
    #MSA generation required; generation by HHblits
    hhblits -cpu %THREADS \
            -v 2 \
            -i %alignment.path \
            -d %UNIPROT  \
            -o ../results/msagen.hhblits \
            -oa3m ../results/query.a3m \
            -n %max_hhblits_iter.content \
            -mact 0.35

    #Filter down to a maximum 90% pairwise sequence identity
    hhfilter -i ../results/query.a3m \
             -o ../results/query.reduced.a3m \
             -id 90

    #Convert to fasta format
    reformat.pl a3m fas ../results/query.a3m $(readlink -f ../params/infile_sto)

    $HMMERPATH/hmmbuild --cpu %THREADS \
             ../params/infile_hmm \
             ../params/infile_sto
fi

    $HMMERPATH/hmmsearch --cpu %THREADS \
          -E %eval_cutoff.content \
          --tblout ../results/tbl \
          --domtblout ../results/domtbl \
          -o ../results/outfile \
          -A ../results/outfile_multi_sto \
          ../params/infile_hmm  %STANDARD/%standarddb.content