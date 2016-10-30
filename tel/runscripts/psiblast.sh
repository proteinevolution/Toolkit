ID=$(<JOB.json jq '._id'| awk '{ print $2; }'| sed 's/"//g'| tr -d '\n')
curl -X POST  "localhost:6111/jobs/message/${ID}?message=Starting+PSI-BLAST+run"
%BIOPROGS/tools/ncbi-blast-2.5.0+/bin/psiblast -db %standarddb.content \
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

curl -X POST  "localhost:6111/jobs/message/${ID}?message=PSI-BLAST+run+has+been+finished+successfully"


mv results/out.psiblastp results/out.psiblastp_without_links

curl -X POST  "localhost:6111/jobs/message/${ID}?message=Supplementing+Links+to+the+PSI-BLAST+result+page"
python %BIOPROGS/helpers/Psiblast_add_links.py -i results/out.psiblastp_without_links >> results/out.psiblastp
curl -X POST  "localhost:6111/jobs/message/${ID}?message=Links+supplemented"


# create HTML and PNG for blastviz visualisation

ID=$(<JOB.json jq '._id'| awk '{ print $2; }'| sed 's/"//g'| tr -d '\n')

curl -X POST  "localhost:6111/jobs/message/${ID}?message=Start+Computing+PSI-BLAST+visualization"
perl %BIOPROGS/helpers/blastviz.pl results/out.psiblastp blastviz results files/$ID >> logs/blastviz.log
curl -X POST  "localhost:6111/jobs/message/${ID}?message=Visualization+computed"



# extract alignment from

curl -X POST  "localhost:6111/jobs/message/${ID}?message=Extracting+alignment+from+BLAST+result+page"
perl %BIOPROGS/helpers/alignhits_html.pl results/out.psiblastp results/out.align -e %evalue.content -fas -no_link -blastplus

# reformat alignment to clustal
curl -X POST  "localhost:6111/jobs/message/${ID}?message=Reformating+to+CLUSTALW"
perl %BIOPROGS/helpers/reformat.pl -i=fas \
                                   -o=clu \
                                   -f=results/out.align \
                                   -a=results/out.align_clu

curl -X POST  "localhost:6111/jobs/message/${ID}?message=Make+some+fancy+Scala+stuff"
scala %BIOPROGS/helpers/psiblastpPostProcess.scala results/out.psiblastp

# Produce some extra files:
< results/out.psiblastp grep Expect | awk '{ print $8; }' | sed 's/,$//' > results/evalues.dat



