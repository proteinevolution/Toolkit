psiblast -db %STANDARD/%standarddb.content \
         -matrix %matrix.content \
         -num_iterations %num_iter.content \
         -evalue %evalue.content \
         -inclusion_ethresh %inclusion_ethresh.content \
         -gapopen %gap_open.content \
         -gapextend %gap_ext.content \
         -num_threads %THREADS \
         -num_descriptions %desc.content \
         -num_alignments %desc.content \
         -in_msa %alignment.path \
         -out ../results/output_psiblastp.json \
         -outfmt 15 \
         -out_pssm ../results/out.ksf




# create HTML and PNG for blastviz visualisation

blastJson2tab.py ../results/output_psiblastp.json ../results/output_psiblastp.tab
blastviz_json.pl ../results/output_psiblastp.tab %jobid.content ../results/ ../results/ >> ../logs/blastviz.log
# Generate Query in JSON
fasta2json.py %alignment.path ../results/query.json

# extract alignment from
#alignhits_html.pl ../results/out.psiblastp ../results/out.align -e %evalue.content -fas -no_link -blastplus

## Produces the alignment fasta output
#%SCALA %HELPER/psiblastpPostProcess.scala ../results/out.psiblastp

# Produce Evalues list
awk {'print $(NF-6)'} ../results/output_psiblastp.tab >> ../results/evalues

