JOBID=%jobid.content

SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)
FORMAT=$(head -1 ../params/alignment | egrep "^CLUSTAL" | wc -l)

if [ $CHAR_COUNT -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
      false
fi

if [ $SEQ_COUNT = "0" ] && [ $FORMAT = "0" ] ; then
      sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
      CHAR_COUNT=$(wc -m < ../params/alignment1)

      if [ $CHAR_COUNT -gt "10000" ] ; then
            echo "#Single protein sequence inputs may not contain more than 10000 characters." >> ../results/process.log
            curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
            false
      else
            sed -i "1 i\>${JOBID}" ../params/alignment1
            mv ../params/alignment1 ../params/alignment
      fi
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


if [ "%hhpred_align.content" = "true" ] ; then
        echo "#Pairwise comparison mode." >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

        SEQ_COUNT2=$(egrep '^>' ../params/alignment_two | wc -l)

        echo "done" >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
fi


#CHECK IF MSA generation is required or not
if [ "%msa_gen_max_iter.content" = "0" ] && [ ${SEQ_COUNT} -gt "1" ] ; then
        echo "#No MSA generation required for building A3M." >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
        reformat_hhsuite.pl fas a3m ../results/${JOBID}.fas ${JOBID}.a3m -M first
        mv ${JOBID}.a3m ../results/${JOBID}.a3m
        hhfilter -i ../results/${JOBID}.a3m \
                 -o ../results/${JOBID}.a3m \
                 -cov %min_cov.content\
                 -qid %min_seqid_query.content

        echo "done" >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
else
    #MSA generation required
    #Check what method to use (PSI-BLAST? HHblits?)

    if [ ${SEQ_COUNT} -gt "1" ] ; then
        echo "#MSA generation required." >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
    else
        echo "#MSA generation required." >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
    fi
    echo "done" >> ../results/process.log
    curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1

    #MSA generation by HHblits
    if [ "%msa_gen_method.content" = "hhblits" ] ; then
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
    if [ "%msa_gen_method.content" = "psiblast" ] ; then

        echo "#Running PSI-BLAST for query MSA and A3M generation." >> ../results/process.log
        curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
        #Check if input is a single sequence or an MSA
        INPUT="query"
        if [ $SEQ_COUNT -gt 1 ] ; then
            INPUT="in_msa"
        fi

        psiblast -db ${STANDARD}/nre70 \
                 -num_iterations %msa_gen_max_iter.content \
                 -evalue %hhpred_incl_eval.content \
                 -inclusion_ethresh 0.001 \
                 -num_threads %THREADS \
                 -num_descriptions 20000 \
                 -num_alignments 20000 \
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
fi

#Generate representative MSA for forwarding
hhfilter -i ../results/${JOBID}.a3m \
         -o ../results/alignment.fas \
         -diff 100

reformat_hhsuite.pl a3m fas \
         $(readlink -f ../results/alignment.fas) \
         $(readlink -f ../results/alignment.fas) \
         -d 160

addss.pl ../results/${JOBID}.a3m

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

    if [ "%msa_gen_max_iter.content" = "0" ] && [ ${SEQ_COUNT2} -gt "1" ] ; then
            reformat_hhsuite.pl fas a3m %alignment_two.path db.a3m -M first
    else
            hhblits -d %UNIPROT -i %alignment_two.path -oa3m db.a3m -n %msa_gen_max_iter.content -cpu %THREADS -v 2
    fi

    ffindex_build -as db_a3m_wo_ss.ff{data,index} db.a3m
    mpirun -np 2 ffindex_apply_mpi db_a3m_wo_ss.ffdata db_a3m_wo_ss.ffindex -i db_a3m.ffindex -d db_a3m.ffdata -- addss.pl -v 0 stdin stdout
    mpirun -np 2 ffindex_apply_mpi db_a3m.ffdata db_a3m.ffindex -i db_hhm.ffindex -d db_hhm.ffdata -- hhmake -i stdin -o stdout -v 0
    OMP_NUM_THREADS=2 cstranslate -A ${HHLIB}/data/cs219.lib -D ${HHLIB}/data/context_data.lib -x 0.3 -c 4 -f -i db_a3m -o db_cs219 -I a3m -b
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
         -Z %desc.content \
         -%alignmode.content \
         -z 1 \
         -b 1 \
         -B %desc.content \
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

#create full alignment json; use for forwarding
fasta2json.py ../results/alignment.fas ../results/reduced.json

hhviz.pl ${JOBID} ../results/ ../results/  &> /dev/null

#Generate query template alignment
hhmakemodel.pl -i ../results/${JOBID}.hhr -fas ../results/querytemplateMSA.fas -p %pmin.content
# Generate Query in JSON
fasta2json.py ../results/querytemplateMSA.fas ../results/querytemplate.json


# Generate Hitlist in JSON for hhrfile
hhr2json.py "$(readlink -f ../results/${JOBID}.hhr)" > ../results/${JOBID}.json

# Generate Query in JSON
fasta2json.py ../results/${JOBID}.fas ../results/query.json

# Generate Query in JSON
fasta2json.py %alignment.path ../results/query.json

# add DB to json
manipulate_json.py -k 'db' -v '%hhsuitedb.content' ../results/${JOBID}.json

# add Proteomes to json
manipulate_json.py -k 'proteomes' -v '%proteomes.content' ../results/${JOBID}.json

echo "done" >> ../results/process.log
curl -X POST http://%HOSTNAME:%PORT/jobs/updateLog/%jobid.content > /dev/null 2>&1
