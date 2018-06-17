# set gapopen and gapextend costs depending on given matrix
GAPOPEN=11
GAPEXT=1
INPUT="query"

SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)

if [ ${CHAR_COUNT} -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      false
fi

if [ ${SEQ_COUNT} = "0" ] && [ ${FORMAT} = "0" ] ; then
      sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
      CHAR_COUNT=$(wc -m < ../params/alignment1)

      if [ ${CHAR_COUNT} -gt "10000" ] ; then
            echo "#Single protein sequence inputs may not contain more than 10000 characters." >> ../results/process.log
            false
      else
            sed -i "1 i\>${JOBID}" ../params/alignment1
            mv ../params/alignment1 ../params/alignment
      fi
fi

if [ ${FORMAT} = "1" ] ; then
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
    false
fi

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.fas | wc -l)

if [ ${SEQ_COUNT} -gt "5000" ] ; then
      echo "#Input contains more than 5000 sequences." >> ../results/process.log
      false
fi

if [ ${SEQ_COUNT} -gt "1" ] ; then
       echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
else
       echo "#Query is a single protein sequence." >> ../results/process.log
fi
echo "done" >> ../results/process.log

if [ ${SEQ_COUNT} -gt 1 ] ; then
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

#Shorten the header of the query sequence to 25 characters; ffindex_from_fasta cannot handle long headers without spaces
sed -n '1p' ../results/${JOBID}.fas | cut -c -25 > ../results/firstSeq0.fas
sed -n '2p' ../results/${JOBID}.fas >> ../results/firstSeq0.fas
sed 's/[\.\-]//g' ../results/firstSeq0.fas > ../results/firstSeq.fas

TMPRED=`tmhmm ../results/firstSeq.fas -short`

run_Coils -c -min_P 0.8 < ../results/firstSeq.fas >& ../results/firstSeq.cc
COILPRED=$(egrep ' 0 in coil' ../results/firstSeq.cc | wc -l)

rm ../results/firstSeq.cc

echo "#Running PSI-BLAST against the %standarddb.content DB." >> ../results/process.log

psiblast -db %STANDARD/%standarddb.content \
         -matrix %matrix.content \
         -num_iterations %maxrounds.content \
         -evalue %evalue.content \
         -inclusion_ethresh %blast_incl_eval.content \
         -gapopen ${GAPOPEN} \
         -gapextend ${GAPEXT} \
         -num_threads %THREADS \
         -max_target_seqs %desc.content \
         -${INPUT} ../results/${JOBID}.fas \
         -out ../results/output_psiblastp.asn \
         -outfmt 11 \
         -max_hsps 1

echo "done" >> ../results/process.log

echo "#Preparing output." >> ../results/process.log

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
alignhits_html.pl   ../results/output_psiblastp.html ../results/output.aln_fas \
                    -Q ../results/firstSeq0.fas \
                    -e %evalue.content \
                    -fas \
                    -no_link \
                    -blastplus

rm ../results/firstSeq0.fas

# create HTML and PNG for blastviz visualisation
blastJson2tab.py ../results/output_psiblastp.json ../results/output_psiblastp.tab
blastviz_json.pl ../results/output_psiblastp.tab %jobid.content ../results/ ../results/ >> ../logs/blastviz.log

# Generate Query in JSON
fasta2json.py ../results/firstSeq.fas ../results/query.json


# add DB to json
manipulate_json.py -k 'db' -v '%standarddb.content' ../results/output_psiblastp.json

# add evalue to json
manipulate_json.py -k 'evalue' -v '%blast_incl_eval.content' ../results/output_psiblastp.json

# add transmembrane prediction info to json
manipulate_json.py -k 'TMPRED' -v "${TMPRED}" ../results/output_psiblastp.json

# add coiled coil prediction info to json
manipulate_json.py -k 'COILPRED' -v "${COILPRED}" ../results/output_psiblastp.json

cd ../results

rm output_psiblastp.asn output_psiblastp.tab

echo "done" >> ../results/process.log
