JOBID=%jobid.content
# set gapopen and gapextend costs depending on given matrix
GAPOPEN=11
GAPEXT=1
INPUT="query"

SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)
FORMAT=$(head -1 ../params/alignment | egrep "^CLUSTAL" | wc -l)

if [ $CHAR_COUNT -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

if [ $SEQ_COUNT = "0" ] && [ $FORMAT = "0" ] ; then
      echo "#Invalid input format. Input should be in aligned FASTA/CLUSTAL format." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

if [ $FORMAT = "1" ] ; then
      reformatValidator.pl clu fas \
            $(readlink -f %alignment.path) \
            $(readlink -f ../results/${JOBID}.fas) \
            -d 160 -uc -l 32000
else
      reformatValidator.pl fas fas \
            $(readlink -f %alignment.path) \
            $(readlink -f ../results/${JOBID}.fas) \
            -d 160 -uc -l 32000
fi

if [ ! -f ../results/${JOBID}.fas ]; then
    echo "#Input is not in aligned FASTA/CLUSTAL format." >> ../results/process.log
    curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
    false
fi

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.fas | wc -l)

if [ $SEQ_COUNT -gt "2000" ] ; then
      echo "#Input contains more than 2000 sequences." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

if [ $SEQ_COUNT -gt "1" ] ; then
       echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
       curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
else
       echo "#Query is a single protein sequence." >> ../results/process.log
       curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
fi
echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1



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
         -num_iterations %maxrounds.content \
         -evalue %evalue.content \
         -inclusion_ethresh %hhpred_incl_eval.content \
         -gapopen $GAPOPEN \
         -gapextend $GAPEXT \
         -num_threads %THREADS \
         -max_target_seqs %desc.content \
         -${INPUT} ../results/${JOBID}.fas \
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
                    -Q ../results/${JOBID}.fas \
                    -e %evalue.content \
                    -fas \
                    -no_link \
                    -blastplus

# create HTML and PNG for blastviz visualisation
blastJson2tab.py ../results/output_psiblastp.json ../results/output_psiblastp.tab
blastviz_json.pl ../results/output_psiblastp.tab %jobid.content ../results/ ../results/ >> ../logs/blastviz.log

# Generate Query in JSON
fasta2json.py ../results/${JOBID}.fas ../results/query.json


# Generate Query in JSON
fasta2json.py ../results/output_psiblastp.aln ../results/alignment.json

# Produce Evalues list
awk {'print $(NF-6)'} ../results/output_psiblastp.tab >> ../results/evalues

# add DB to json
manipulate_json.py -k 'db' -v '%standarddb.content' ../results/output_psiblastp.json

# add evalue to json
manipulate_json.py -k 'evalue' -v '%hhpred_incl_eval.content' ../results/output_psiblastp.json
