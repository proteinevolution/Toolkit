# set gapopen and gapextend costs depending on given matrix
GAPOPEN=11
GAPEXT=1
INPUT="query"

SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

if [ $SEQ_COUNT -gt 1 ] ; then
    INPUT="in_msa"
fi


if [ "%matrix.content" = "BLOSUM80" ] || [ "%matrix.content" = "PAM70" ] ; then
    GAPOPEN=10
fi
if [ "%matrix.content" = "PAM30" ] ; then
    GAPOPEN=9
fi
if [ "%matrix.content" = "BLOSUM45" ] ; then
    GAPOPEN=15
    GAPEXT=2
fi

#%inclusion_ethresh.content (switch to this after the slider is fixed)

psiblast -db %STANDARD/%standarddb.content \
         -matrix %matrix.content \
         -num_iterations %num_iter.content \
         -evalue %evalue.content \
         -inclusion_ethresh 0.005 \
         -gapopen $GAPOPEN \
         -gapextend $GAPEXT \
         -num_threads %THREADS \
         -max_target_seqs %desc.content \
         -${INPUT} %alignment.path \
         -out ../results/output_psiblastp.json \
         -outfmt 15 \
         -max_hsps 1 \
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

