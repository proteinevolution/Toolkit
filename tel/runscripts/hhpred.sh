JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

if [  "%hhpred_align.content" = "true" ] ; then
        echo "#Pairwise comparison mode." >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
        echo "done" >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
fi


reformat_hhsuite.pl fas fas %alignment.path ${JOBID}.fas -l 32000 -uc
mv ${JOBID}.fas ../results

#CHECK IF MSA generation is required or not
if [ %msa_gen_max_iter.content = "0" ] && [ $SEQ_COUNT -gt "1" ] ; then
        echo "#Query is an MSA with ${SEQ_COUNT} sequences. No MSA generation required for building A3M." >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
        reformat_hhsuite.pl fas a3m ../results/${JOBID}.fas ${JOBID}.a3m -M first
        mv ${JOBID}.a3m ../results/${JOBID}.a3m
        hhfilter -i ../results/${JOBID}.a3m \
                 -o ../results/${JOBID}.a3m \
                 -cov %min_cov.content\
                 -qid %min_seqid_query.content

        addss.pl ../results/${JOBID}.a3m
        echo "done" >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
else
    #MSA generation required
    #Check what method to use (PSI-BLAST? HHblits?)

    if [ $SEQ_COUNT -gt "1" ] ; then
        echo "#Query is an MSA with ${SEQ_COUNT} sequences. MSA generation required." >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
    else
        echo "#Query is a single protein sequence. MSA generation required." >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
    fi
    echo "done" >> ../results/process.log
    curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

    #MSA generation by HHblits
    if [ %msa_gen_method.content = "hhblits" ] ; then
        echo "#Running HHblits for query MSA and A3M generation." >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
        hhblits -cpu %THREADS \
                -v 2 \
                -e %hhpred_incl_eval.content \
                -i ../results/${JOBID}.fas \
                -d %UNIPROT  \
                -oa3m ../results/${JOBID}.a3m \
                -n %msa_gen_max_iter.content \
                -qid %min_seqid_query.content \
                -cov %min_cov.content \
                -mact 0.35

        echo "done" >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

    fi
    #MSA generation by PSI-BLAST
    if [ %msa_gen_method.content = "psiblast" ] ; then

        echo "#Running PSI-BLAST for query MSA and A3M generation." >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
        #Check if input is a single sequence or an MSA
        INPUT="query"
        if [ $SEQ_COUNT -gt 1 ] ; then
            INPUT="in_msa"
        fi

        psiblast -db ${STANDARDNEW}/nre70 \
                 -num_iterations %msa_gen_max_iter.content \
                 -evalue %hhpred_incl_eval.content \
                 -inclusion_ethresh 0.001 \
                 -num_threads %THREADS \
                 -num_descriptions 20000 \
                 -num_alignments 20000 \
                 -html \
                 -${INPUT} ../results/${JOBID}.fas \
                 -out ../results/output_psiblastp.html

        #keep results only of the last iteration
        shorten_psiblast_output.pl ../results/output_psiblastp.html ../results/output_psiblastp.html

        #extract MSA in a3m format
        alignhits_html.pl   ../results/output_psiblastp.html ../results/${JOBID}.a3m \
                    -Q ../results/${JOBID}.fas \
                    -e %hhpred_incl_eval.content \
                    -cov %min_cov.content \
                    -a3m \
                    -no_link \
                    -blastplus
        echo "done" >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

    fi

    addss.pl ../results/${JOBID}.a3m

fi


# Here assume that the query alignment exists

# prepare histograms
# Reformat query into fasta format ('full' alignment, i.e. 100 maximally diverse sequences, to limit amount of data to transfer)
hhfilter -i ../results/${JOBID}.a3m \
         -o ../results/${JOBID}.reduced.a3m \
         -diff 100

DBJOINED=""
#create file in which selected dbs are written
touch ../params/dbs
# creating alignment of query and subject input
if [  "%hhpred_align.content" = "true" ]
then
    echo "#Running HHblits for template MSA and A3M generation." >> ../results/process.log
    curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

    cd ../results

    if [ %msa_gen_max_iter.content = "0" ] ; then
            reformat_hhsuite.pl fas a3m %alignment_two.path db.a3m -M first
            ffindex_build -as db_a3m_wo_ss.ff{data,index} db.a3m
    else
            ffindex_from_fasta -s db_fas.ffdata db_fas.ffindex %alignment_two.path
            mpirun -np %THREADS ffindex_apply_mpi db_fas.ffdata db_fas.ffindex -i db_a3m_wo_ss.ffindex -d db_a3m_wo_ss.ffdata -- hhblits -d %UNIPROT -i stdin -oa3m stdout -n %msa_gen_max_iter.content -cpu 1 -v 0
    fi
    mpirun -np %THREADS ffindex_apply_mpi db_a3m_wo_ss.ffdata db_a3m_wo_ss.ffindex -i db_a3m.ffindex -d db_a3m.ffdata -- addss.pl -v 0 stdin stdout
    mpirun -np %THREADS ffindex_apply_mpi db_a3m.ffdata db_a3m.ffindex -i db_hhm.ffindex -d db_hhm.ffdata -- hhmake -i stdin -o stdout -v 0
    OMP_NUM_THREADS=%THREADS cstranslate -A ${HHLIB}/data/cs219.lib -D ${HHLIB}/data/context_data.lib -x 0.3 -c 4 -f -i db_a3m -o db_cs219 -I a3m -b
    ffindex_build -as db_cs219.ffdata db_cs219.ffindex
    DBJOINED+="-d ../results/db"
    cd ../0
    echo "done" >> ../results/process.log
    curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
else
    #splitting input databases into array and completing with -d
    if [ "%hhsuitedb.content" != "false" ]
    then
        DBS=$(echo "%hhsuitedb.content" | tr " " "\n")
        DBJOINED+=`printf -- '-d %HHSUITE/%s ' ${DBS[@]}`
        #write selected databses into file
        printf "${DBS[@]}" >> ../params/dbs
        printf "\n" >> ../params/dbs
    fi
    if [ "%proteomes.content" != "false" ]
    then
        PROTEOMES=$(echo "%proteomes.content" | tr " " "\n")
        DBJOINED+=`printf -- '-d %HHSUITE/%s ' ${PROTEOMES[@]}`
        #write selected databses into file
        printf "${PROTEOMES[@]}" >> ../params/dbs
    fi
fi


if [  "%hhpred_align.content" = "true" ] ; then
      echo "#Comparing query profile HMM with template profile HMM." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
else
      echo "#Searching profile HMM database(s)." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
fi

# Perform HHsearch #
hhsearch -cpu %THREADS \
         -i ../results/${JOBID}.reduced.a3m \
         ${DBJOINED} \
         -o ../results/${JOBID}.hhr \
         -oa3m ../results/${JOBID}.a3m \
         -p %pmin.content \
         -P %pmin.content \
         -Z %max_lines.content \
         -%alignmode.content \
         -z 1 \
         -b 1 \
         -B %max_lines.content \
         -ssm %ss_scoring.content \
         -seq 1 \
         -dbstrlen 10000 \
         -cs ${HHLIB}/data/context_data.lib \
         -atab $(readlink -f ../results/hhsearch.start.tab) \
         %macmode.content \
         -mact %macthreshold.content


echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

echo "#Preparing output." >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1


hhviz.pl ${JOBID} ../results/ ../results/  &> /dev/null

# Generate Hitlist in JSON for hhrfile

hhr2json.py "$(readlink -f ../results/${JOBID}.hhr)" > ../results/${JOBID}.json

# Generate Query in JSON
fasta2json.py ../results/${JOBID}.fas ../results/query.json

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

#TODO display JOBID}.a3m and JOBID}.reduced.a3m; use the latter for forwarding

