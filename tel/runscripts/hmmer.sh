
#CHECK IF MSA generation is required or not

if [ %max_hhblits_iter.content == "0" ] ; then
    #Use user MSA to build HMM

    hmmbuild --cpu %THREADS \
             $(readlink -f ../params/infile_hmm) \
             %alignment.path
else
    #MSA generation required; generation by HHblits
    hhblits -cpu 8 \
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

    hmmbuild --cpu %THREADS \
             $(readlink -f ../params/infile_hmm) \
             $(readlink -f ../params/infile_sto)
fi

hmmsearch --cpu %THREADS \
          -E %eval_cutoff.content \
          --tblout    $(readlink -f ../results/tbl) \
          --domtblout $(readlink -f ../results/domtbl) \
          -o $(readlink -f ../results/outfile) \
          -A $(readlink -f ../results/outfile_multi_sto) \
           $(readlink -f ../params/infile_hmm)  %STANDARD/%standarddb.content