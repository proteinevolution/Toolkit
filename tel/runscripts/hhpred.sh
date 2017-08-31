SEQ_COUNT=$(egrep '^>' ../params/alignment | wc -l)
CHAR_COUNT=$(wc -m < ../params/alignment)
A3M_INPUT=$(head -1 ../params/alignment | egrep "^#A3M#" | wc -l)


if [ ${CHAR_COUNT} -gt "10000000" ] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      updateProcessLog
      false
fi


if [ ${A3M_INPUT} = "1" ] ; then

    sed -i '1d' ../params/alignment

    reformatValidator.pl a3m fas \
           $(readlink -f ../params/alignment) \
           $(readlink -f ../params/alignment.tmp) \
           -d 160 -uc -l 32000

     if [ ! -f ../params/alignment.tmp ]; then
            echo "#Input is not in valid A3M format." >> ../results/process.log
            updateProcessLog
            false
     else
            echo "#Query is in A3M format." >> ../results/process.log
            updateProcessLog
            mv ../params/alignment.tmp ../params/alignment
            echo "done" >> ../results/process.log
            updateProcessLog
     fi
fi


if [ ${SEQ_COUNT} = "0" ] && [ ${FORMAT} = "0" ] ; then
      sed 's/[^a-z^A-Z]//g' ../params/alignment > ../params/alignment1
      CHAR_COUNT=$(wc -m < ../params/alignment1)

      if [ ${CHAR_COUNT} -gt "10000" ] ; then
            echo "#Single protein sequence inputs may not contain more than 10000 characters." >> ../results/process.log
            updateProcessLog
            false
      else
            sed -i "1 i\>Q_${JOBID}" ../params/alignment1
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
    updateProcessLog
    false
fi

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.fas | wc -l)

if [ ${SEQ_COUNT} -gt "10000" ] ; then
      echo "#Input contains more than 10000 sequences." >> ../results/process.log
      updateProcessLog
      false
fi

if [ ${SEQ_COUNT} -gt "1" ] ; then
       echo "#Query is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
       updateProcessLog
else
       echo "#Query is a single protein sequence." >> ../results/process.log
       updateProcessLog
fi

echo "done" >> ../results/process.log
updateProcessLog


if [ "%hhpred_align.content" = "true" ] ; then
        echo "#Pairwise comparison mode." >> ../results/process.log
        updateProcessLog

        echo "done" >> ../results/process.log
        updateProcessLog

        SEQ_COUNT2=$(egrep '^>' ../params/alignment_two | wc -l)
        CHAR_COUNT2=$(wc -m < ../params/alignment_two)
        FORMAT2=$(head -1 ../params/alignment_two | egrep "^CLUSTAL" | wc -l)
        A3M_INPUT2=$(head -1 ../params/alignment_two | egrep "^#A3M#" | wc -l)


        if [ ${CHAR_COUNT2} -gt "10000000" ] ; then
            echo "#Template sequence/MSA may not contain more than 10000000 characters." >> ../results/process.log
            updateProcessLog
            false
        fi


        if [ ${A3M_INPUT2} = "1" ] ; then

            sed -i '1d' ../params/alignment_two

            reformatValidator.pl a3m fas \
                $(readlink -f ../params/alignment_two) \
                $(readlink -f ../params/alignment_two.tmp) \
                -d 160 -uc -l 32000

            if [ ! -f ../params/alignment_two.tmp ]; then
                echo "#Template is not in valid A3M format." >> ../results/process.log
                updateProcessLog
                false
            else
                echo "#Template is in A3M format." >> ../results/process.log
                updateProcessLog
                mv ../params/alignment_two.tmp ../params/alignment_two
                echo "done" >> ../results/process.log
                updateProcessLog
            fi
        fi

        if [ ${SEQ_COUNT2} = "0" ] && [ ${FORMAT2} = "0" ] ; then
            sed 's/[^a-z^A-Z]//g' ../params/alignment_two > ../params/alignment2
            CHAR_COUNT2=$(wc -m < ../params/alignment2)

            if [ ${CHAR_COUNT2} -gt "10000" ] ; then
                echo "#Template protein sequence contains more than 10000 characters." >> ../results/process.log
                updateProcessLog
                false
            else
                sed -i "1 i\>T_${JOBID}" ../params/alignment2
                mv ../params/alignment2 ../params/alignment_two
            fi
        fi

        if [ ${FORMAT2} = "1" ] ; then
            reformatValidator.pl clu fas \
            $(readlink -f %alignment_two.path) \
            $(readlink -f ../results/${JOBID}.2.fas) \
            -d 160 -uc -l 32000
        else
            #remove empty lines
            sed -i '/^\s*$/d' ../params/alignment_two
            reformatValidator.pl fas fas \
            $(readlink -f %alignment_two.path) \
            $(readlink -f ../results/${JOBID}.2.fas) \
            -d 160 -uc -l 32000
        fi

        if [ ! -f ../results/${JOBID}.2.fas ]; then
            echo "#Template MSA is not in aligned FASTA/CLUSTAL format." >> ../results/process.log
            updateProcessLog
            false
        fi

        SEQ_COUNT2=$(egrep '^>' ../results/${JOBID}.2.fas | wc -l)

        if [ ${SEQ_COUNT2} -gt "10000" ] ; then
            echo "#Template MSA contains more than 10000 sequences." >> ../results/process.log
            updateProcessLog
            false
        fi

        if [ ${SEQ_COUNT2} -gt "1" ] ; then
            echo "#Template is an MSA with ${SEQ_COUNT} sequences." >> ../results/process.log
            updateProcessLog
        else
            echo "#Template is a single protein sequence." >> ../results/process.log
            updateProcessLog
         fi

        mv ../results/${JOBID}.2.fas ../params/alignment_two

        echo "done" >> ../results/process.log
        updateProcessLog
fi

head -n 2 ../results/${JOBID}.fas > ../results/firstSeq0.fas
sed 's/[\.\-]//g' ../results/firstSeq0.fas > ../results/firstSeq.fas

TMPRED=`tmhmm ../results/firstSeq.fas -short`

run_Coils -c -min_P 0.8 < ../results/firstSeq.fas >& ../results/firstSeq.cc
COILPRED=$(egrep ' 0 in coil' ../results/firstSeq.cc | wc -l)

rm ../results/firstSeq0.fas ../results/firstSeq.cc


#CHECK IF MSA generation is required or not
if [ "%msa_gen_max_iter.content" = "0" ] ; then
        echo "#No MSA generation required for building A3M." >> ../results/process.log
        updateProcessLog
        reformat_hhsuite.pl fas a3m ../results/${JOBID}.fas ${JOBID}.a3m -M first
        mv ${JOBID}.a3m ../results/${JOBID}.a3m
        hhfilter -i ../results/${JOBID}.a3m \
                 -o ../results/${JOBID}.a3m \
                 -cov %min_cov.content\
                 -qid %min_seqid_query.content

        echo "done" >> ../results/process.log
        updateProcessLog
else
    #MSA generation required
    #Check what method to use (PSI-BLAST? HHblits?)

        echo "#Query MSA generation required." >> ../results/process.log
        updateProcessLog
        echo "done" >> ../results/process.log
        updateProcessLog

        if [ %msa_gen_max_iter.content -lt "1" ] ; then
            ITERS=3
        else
            ITERS=%msa_gen_max_iter.content
        fi

    #MSA generation by HHblits
    if [ "%msa_gen_method.content" = "hhblits" ] ; then
        echo "#Running ${ITERS} iteration(s) of HHblits for query MSA generation." >> ../results/process.log
        updateProcessLog

        reformat_hhsuite.pl fas a3m \
                            $(readlink -f ../results/${JOBID}.fas) \
                            $(readlink -f ../results/${JOBID}.in.a3m)

        hhblits -cpu %THREADS \
                -v 2 \
                -e %hhpred_incl_eval.content \
                -i ../results/${JOBID}.in.a3m \
                -d %UNIPROT  \
                -oa3m ../results/${JOBID}.a3m \
                -n ${ITERS} \
                -qid %min_seqid_query.content \
                -cov %min_cov.content \
                -mact 0.35
        rm ../results/${JOBID}.in.a3m

        echo "done" >> ../results/process.log
        updateProcessLog

    fi
    #MSA generation by PSI-BLAST
    if [ "%msa_gen_method.content" = "psiblast" ] ; then

        echo "#Running ${ITERS} iteration(s) of PSI-BLAST for query MSA generation." >> ../results/process.log
        updateProcessLog
        #Check if input is a single sequence or an MSA
        INPUT="query"
        if [ ${SEQ_COUNT} -gt 1 ] ; then
            INPUT="in_msa"
        fi

        psiblast -db ${STANDARD}/nre70 \
                 -num_iterations ${ITERS} \
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
        updateProcessLog
    fi
fi

echo "#Generating query A3M." >> ../results/process.log
updateProcessLog

#Generate representative MSA for forwarding

hhfilter -i ../results/${JOBID}.a3m \
         -o ../results/reduced.a3m \
         -neff 12.0 \
         -diff 100

sed -i "1 i\#A3M#" ../results/reduced.a3m

reformat_hhsuite.pl a3m a3m \
         $(readlink -f ../results/${JOBID}.a3m) \
         $(readlink -f ../results/tmp0) \
         -d 160 -l 32000

head -n 400 ../results/tmp0 > ../results/tmp1

reformat_hhsuite.pl a3m fas \
         $(readlink -f ../results/tmp1) \
         $(readlink -f ../results/reduced.fas) \
         -d 160 -l 32000 -uc

rm ../results/tmp0 ../results/tmp1


# Here assume that the query alignment exists
# prepare histograms
# Reformat query into fasta format ('full' alignment, i.e. 100 maximally diverse sequences, to limit amount of data to transfer)

mv ../results/${JOBID}.a3m ../results/full.a3m

addss.pl ../results/full.a3m

echo "done" >> ../results/process.log
updateProcessLog

DBJOINED=""
#create file in which selected dbs are written
touch ../params/dbs
# creating alignment of query and subject input
if [  "%hhpred_align.content" = "true" ]
then
    echo "#Running 3 iterations of HHblits for template MSA and A3M generation." >> ../results/process.log
    updateProcessLog

    cd ../results

    if [ "%msa_gen_max_iter.content" = "0" ] && [ ${SEQ_COUNT2} -gt "1" ] ; then
            reformat_hhsuite.pl fas a3m %alignment_two.path db.a3m -M first
    else
            reformat_hhsuite.pl fas a3m \
                  $(readlink -f %alignment_two.path) \
                  $(readlink -f ../results/${JOBID}.in2.a3m)

        hhblits -d %UNIPROT -i ../results/${JOBID}.in2.a3m -oa3m db.a3m -n 3 -cpu %THREADS -v 2
        rm ../results/${JOBID}.in2.a3m
    fi

    ffindex_build -as db_a3m_wo_ss.ff{data,index} db.a3m
    mpirun -np 2 ffindex_apply_mpi db_a3m_wo_ss.ffdata db_a3m_wo_ss.ffindex -i db_a3m.ffindex -d db_a3m.ffdata -- addss.pl -v 0 stdin stdout
    mpirun -np 2 ffindex_apply_mpi db_a3m.ffdata db_a3m.ffindex -i db_hhm.ffindex -d db_hhm.ffdata -- hhmake -i stdin -o stdout -v 0
    OMP_NUM_THREADS=2 cstranslate -A ${HHLIB}/data/cs219.lib -D ${HHLIB}/data/context_data.lib -x 0.3 -c 4 -f -i db_a3m -o db_cs219 -I a3m -b
    ffindex_build -as db_cs219.ffdata db_cs219.ffindex
    DBJOINED+="-d ../results/db"
    cd ../0
    echo "done" >> ../results/process.log
    updateProcessLog
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
      updateProcessLog
else
      echo "#Searching profile HMM database(s)." >> ../results/process.log
      updateProcessLog
fi

if [ "%alignmode.content" = "loc" ] ; then
    MACT_SCORE=%macthreshold.content

    if [ "%macmode.content" = "-realign" ] ; then
        MACT="-realign -mact ${MACT_SCORE}"
    else
        MACT="-norealign"
    fi
fi

if [ "%alignmode.content" = "glob" ] ; then
    MACT="-realign -mact 0.0"
fi

# Perform HHsearch #
hhsearch -cpu %THREADS \
         -i ../results/full.a3m \
         ${DBJOINED} \
         -o ../results/${JOBID}.hhr \
         -oa3m ../results/${JOBID}.a3m \
         -p %pmin.content \
         -Z %desc.content \
         -%alignmode.content \
         -z 1 \
         -b 1 \
         -B %desc.content \
         -ssm %ss_scoring.content \
         -sc 1 \
         -seq 1 \
         -dbstrlen 10000 \
         ${MACT} \
         -maxres 32000 \
         -contxt ${HHLIB}/data/context_data.crf


echo "done" >> ../results/process.log
updateProcessLog

echo "#Preparing output." >> ../results/process.log
updateProcessLog

#create full alignment json; use for forwarding
fasta2json.py ../results/reduced.fas ../results/reduced.json

hhviz.pl ${JOBID} ../results/ ../results/  &> /dev/null

#Generate query template alignment
hhmakemodel.pl -i ../results/${JOBID}.hhr -fas ../results/alignment.fas -p %pmin.content
# Generate Query in JSON
fasta2json.py ../results/alignment.fas ../results/querytemplate.json


# Generate Hitlist in JSON for hhrfile
hhr2json.py "$(readlink -f ../results/${JOBID}.hhr)" > ../results/${JOBID}.json

# Generate Query in JSON
fasta2json.py ../results/firstSeq.fas ../results/query.json

# add DB to json
manipulate_json.py -k 'db' -v '%hhsuitedb.content' ../results/${JOBID}.json

# add Proteomes to json
manipulate_json.py -k 'proteomes' -v '%proteomes.content' ../results/${JOBID}.json


# add transmembrane prediction info to json
manipulate_json.py -k 'TMPRED' -v "${TMPRED}" ../results/${JOBID}.json

# add coiled coil prediction info to json
manipulate_json.py -k 'COILPRED' -v "${COILPRED}" ../results/${JOBID}.json




echo "done" >> ../results/process.log
updateProcessLog
