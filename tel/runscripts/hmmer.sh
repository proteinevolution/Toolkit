reformat.pl fas sto %alignment.path $(readlink -f ../params/infile_sto)


hmmbuild --cpu 4 \
         $(readlink -f ../params/infile_hmm) \
         $(readlink -f ../params/infile_sto)

hmmsearch --cpu 4 \
          -E 1e-1 \
          --tblout    $(readlink -f ../results/tbl) \
          --domtblout $(readlink -f ../results/domtbl) \
          -o $(readlink -f ../results/outfile) \
          -A $(readlink -f ../results/outfile_multi_sto) \
           $(readlink -f ../params/infile_hmm)  %STANDARD/%standarddb.content

