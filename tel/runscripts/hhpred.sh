JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment  -c)

reformat_hhsuite.pl fas fas %alignment.path ${JOBID}.fas -l 32000 -uc
mv ${JOBID}.fas ../results
echo "#starting HHPred script" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

#CHECK IF MSA generation is required or not
if [ %msa_gen_max_iter.content == "0" ] && [ $SEQ_COUNT -gt "1" ] ; then
        reformat_hhsuite.pl fas a3m ../results/${JOBID}.fas query.a3m -M first
        mv query.a3m ../results/query.a3m
        addss.pl ../results/query.a3m
else
    #MSA generation required
    #Check what method to use (PSI-BLAST? HHblits?)

    #MSA generation by HHblits
    if [ %msa_gen_method.content == "hhblits" ] ; then
        hhblits -cpu %THREADS \
                -v 2 \
                -i ../results/${JOBID}.fas \
                -d %UNIPROT  \
                -oa3m ../results/query.a3m \
                -n %msa_gen_max_iter.content \
                -mact 0.35
    fi
    #MSA generation by HHblits
    if [ %msa_gen_method.content == "psiblast" ] ; then
        #Check if input is a single sequence or an MSA
        INPUT="query"
        if [ $SEQ_COUNT -gt 1 ] ; then
            INPUT="in_msa"
        fi

        psiblast -db ${STANDARDNEW}/nre70 \
                 -num_iterations %msa_gen_max_iter.content \
                 -evalue 0.001 \
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
        alignhits_html.pl   ../results/output_psiblastp.html ../results/query.a3m \
                    -Q ../results/${JOBID}.fas \
                    -e 0.001 \
                    -cov 20 \
                    -a3m \
                    -no_link \
                    -blastplus
    fi

    addss.pl ../results/query.a3m
fi


echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
# Here assume that the query alignment exists

# prepare histograms
# Reformat query into fasta format ('full' alignment, i.e. 100 maximally diverse sequences, to limit amount of data to transfer)
hhfilter -i ../results/query.a3m \
         -o ../results/query.reduced.a3m \
         -diff 100



# max. 160 chars in description
reformat_hhsuite.pl a3m fas "$(readlink -f ../results/query.reduced.a3m)" query.fas -d 160 -uc
mv query.fas ../results/query.fas

# Reformat query into fasta format (reduced alignment)  (Careful: would need 32-bit version to execute on web server!!)
hhfilter -i ../results/query.a3m \
         -o ../results/query.reduced.a3m \
         -diff 50

reformat_hhsuite.pl -r a3m fas "$(readlink -f ../results/query.reduced.a3m)" query.reduced.fas -uc
mv query.reduced.fas ../results/query.reduced.fas

rm ../results/query.reduced.a3m



# build histogram

reformat.pl a3m \
            fas \
            "$(readlink -f  ../results/query.a3m)" \
            "$(readlink -f  ../results/query.full.fas)"


hhfilter -i ../results/query.reduced.fas \
         -o ../results/query.top.a3m \
         -id 90 \
         -qid 0 \
         -qsc 0 \
         -cov 0 \
         -diff 10


reformat_hhsuite.pl a3m fas  "$(readlink -f ../results/query.top.a3m)" query.repseq.fas -uc
mv query.repseq.fas ../results/query.repseq.fas

DBJOINED=""
#create file in which selected dbs are written
touch ../params/dbs
# creating alignment of query and subject input
if [  "%hhpred_align.content" == "true" ]
then
    cd ../results

    if [ %msa_gen_max_iter.content == "0" ] ; then
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

echo "#HHsearch" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1


# Perform HHsearch # TODO Include more parameters
hhsearch -cpu %THREADS \
         -i ../results/query.a3m \
         ${DBJOINED} \
         -o ../results/${JOBID}.hhr \
         -p %pmin.content \
         -P %pmin.content \
         -Z %max_lines.content \
         -%alignmode.content \
         -z 1 \
         -b 1 \
         -B %max_lines.content \
         -ssm %ss_scoring.content \
         -seq 1 \
         -aliw %aliwidth.content \
         -dbstrlen 10000 \
         -cs ${HHLIB}/data/context_data.lib \
         -atab $(readlink -f ../results/hhsearch.start.tab) \
         %macmode.content \
         -mact %macthreshold.content


echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

hhviz.pl ${JOBID} ../results/ ../results/  &> /dev/null

tenrep.rb -i ../results/query.repseq.fas -h ../results/${JOBID}.hhr -p 40 -o ../results/query.tenrep_file

cp ../results/query.tenrep_file ../results/query.tenrep_file_backup

parse_jalview.rb -i ../results/query.tenrep_file -o ../results/query.tenrep_file


# Reformat tenrep file such that we can display it in the full alignment section
reformat.pl fas \
            clu \
            "$(readlink -f ../results/query.tenrep_file)" \
            "$(readlink -f ../results/alignment.clustalw_aln)"



# Generate Hitlist in JSON for hhrfile
 
hhr2json.py "$(readlink -f ../results/${JOBID}.hhr)" > ../results/${JOBID}.json

# Generate Query in JSON
fasta2json.py ../results/${JOBID}.fas ../results/query.json
