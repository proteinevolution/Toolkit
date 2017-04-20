JOBID=%jobid.content
# set gapopen and gapextend costs depending on given matrix
GAPOPEN=11
GAPEXT=1
INPUT="query"

reformat_hhsuite.pl fas fas $(readlink -f %alignment.path) $(readlink -f %alignment.path) -uc -r -M first -l 32000

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
         -inclusion_ethresh %hhpred_incl_eval.content \
         -gapopen $GAPOPEN \
         -gapextend $GAPEXT \
         -num_threads %THREADS \
         -max_target_seqs %desc.content \
         -${INPUT} %alignment.path \
         -out ../results/output_psiblastp.asn \
         -outfmt 11 \
         -max_hsps 1

#converst ASN.1 output to JSON
blast_formatter -archive ../results/output_psiblastp.asn \
                -outfmt 15 \
                -out ../results/output_psiblastp.json \
                -max_target_seqs %desc.content

#converst ASN.1 output to HTML
blast_formatter -archive ../results/output_psiblastp.asn \
                -out ../results/output_psiblastp.html \
                -num_descriptions %desc.content \
                -num_alignments %desc.content

#keep results only of the last iteration
shorten_psiblast_output.pl ../results/output_psiblastp.html ../results/output_psiblastp.html


#extract MSA
alignhits_html.pl   ../results/output_psiblastp.html ../results/output_psiblastp.aln \
                    -Q %alignment.path \
                    -e %evalue.content \
                    -fas \
                    -no_link \
                    -blastplus

# create HTML and PNG for blastviz visualisation
blastJson2tab.py ../results/output_psiblastp.json ../results/output_psiblastp.tab
blastviz_json.pl ../results/output_psiblastp.tab %jobid.content ../results/ ../results/ >> ../logs/blastviz.log

# Generate Query in JSON
fasta2json.py %alignment.path ../results/query.json


# Generate Query in JSON
fasta2json.py ../results/output_psiblastp.aln ../results/${JOBID}.alignment.json

# Produce Evalues list
awk {'print $(NF-6)'} ../results/output_psiblastp.tab >> ../results/evalues

# add DB to json
manipulate_json.py -k 'db' -v '%standarddb.content' ../results/output_psiblastp.json

# add DB to json
manipulate_json.py -k 'evalue' -v '%evalue.content' ../results/output_psiblastp.json
