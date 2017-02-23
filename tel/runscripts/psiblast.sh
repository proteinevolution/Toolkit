psiblast -db %STANDARD/%standarddb.content \
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
         -out ../results/output_psiblastp.json \
         -outfmt 15 \
         -out_pssm ../results/out.ksf




# create HTML and PNG for blastviz visualisation

blastJson2tab.py ../results/output_psiblastp.json ../results/output_psiblastp.tab
blastviz_json.pl ../results/output_psiblastp.tab %jobid.content ../results/ ../results/ >> ../logs/blastviz.log

# extract alignment from

#PERL5LIB=%PERLLIB
#alignhits_html.pl ../results/out.psiblastp ../results/out.align -e %evalue.content -fas -no_link -blastplus

## reformat alignment to clustal
#reformat.pl fas \
#            clu \
#            "$(readlink -f ../results/out.align)" \
#            "$(readlink -f ../results/out.align_clu)"
#
## Produces the alignment fasta output
#%SCALA %HELPER/psiblastpPostProcess.scala ../results/out.psiblastp

# Produce new PSIBLAST Overview and also the Evalues list
#parse_BLAST_HTML.py ../results/out.psiblastp > ../results/out.psiblastp_overview

