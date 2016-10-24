%BIOPROGS/tools/ncbi-blast-2.3.0+/bin/psiblast -db %standarddb.content \
                                            -matrix %matrix.content \
                                            -num_iterations %num_iter.content \
                                            -evalue %evalue.content \
                                            -inclusion_ethresh %inclusion_ethresh.content \
                                            -gapopen %gap_open.content \
                                            -gapextend %gap_ext.content \
                                            -num_threads 4 \
                                            -num_descriptions %desc.content \
                                            -num_alignments %desc.content \
                                            -in_msa %alignment.path \
                                            -out results/out.psiblastp \
                                            -outfmt 0 \
                                            -html\
                                            -out_pssm results/out.ksf


# create HTML and PNG for blastviz visualisation

ID=$(<JOB.json jq '._id'| awk '{ print $2; }'| sed 's/"//g'| tr -d '\n')

perl %BIOPROGS/helpers/blastviz.pl results/out.psiblastp blastviz results files/$ID >> logs/blastviz.log

# extract alignment from
perl %BIOPROGS/helpers/alignhits_html.pl results/out.psiblastp results/out.align -e %evalue.content -fas -no_link -blastplus

# reformat alignment to clustal
perl %BIOPROGS/helpers/reformat.pl -i=fas \
                                   -o=clu \
                                   -f=results/out.align \
                                   -a=results/out.align_clu

#scala %BIOPROGS/helpers/psiblastpPostProcess.scala results/out.psiblastp

# Produce some extra files:
< results/out.psiblastp grep Expect | awk '{ print $8; }' | sed 's/,$//' > results/evalues.dat



