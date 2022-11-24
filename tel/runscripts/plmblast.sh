cp %alignment.path ../results/${JOBID}.fas
SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.fas | wc -l)
CHAR_COUNT=$(grep -v '>' ../results/${JOBID}.fas | wc -m)

if [[ ${CHAR_COUNT} -gt "1000" ]] ; then
      echo "#Input may not contain more than 1000 characters." >> ../results/process.log
      echo "done" >> ../results/process.log
      false
fi

if [[ ${FORMAT} = "1" ]] || [[ ${SEQ_COUNT} -gt "1" ]] ; then
      echo "#Input is a multiple sequence alignment; expecting a single protein sequence." >> ../results/process.log
      echo "done" >> ../results/process.log
      false
fi

if [[ ${SEQ_COUNT} = "0" ]] ; then
      sed 's/[^a-z^A-Z]//g' ../results/${JOBID}.fas > ../results/${JOBID}.fas1
      perl -pe 's/\s+//g' ../results/${JOBID}.fas1 > ../results/${JOBID}.fas
      CHAR_COUNT=$(wc -m < ../results/${JOBID}.fas)
      rm ../results/${JOBID}.fas1

      if [[ ${CHAR_COUNT} -gt "1000" ]] ; then
            echo "#Input may not contain more than 1000 characters." >> ../results/process.log
            echo "done" >> ../results/process.log
            false
      else
            sed -i "1 i\>${JOBID}" ../results/${JOBID}.fas
      fi
fi
source ${BIOPROGS}/dependencies/anaconda3/etc/profile.d/conda.sh
conda activate plm-blast

echo "#Calculating embedding for query sequence." >> ../results/process.log

# calculate query embedding
python3.9 $PLMBLASTPATH/scripts/query_emb.py \
          ../results/${JOBID}.fas \
          ../results/${JOBID}.pt_emb.p \
          ../results/${JOBID}.csv
echo "done" >> ../results/process.log

export MKL_NUM_THREADS=1
export NUMEXPR_NUM_THREADS=1
export OMP_NUM_THREADS=1

echo "#Searching %plmblastdb.content." >> ../results/process.log


python3.9 $PLMBLASTPATH/scripts/plm_blast.py %PLMBLAST/%plmblastdb.content \
                                             ../results/${JOBID} \
                                             ../results/${JOBID}.hits.csv \
                                             -cosine_percentile_cutoff %cosine_percentile_cutoff.content \
                                             -alignment_cutoff %alignment_cutoff.content \
                                             -max_targets %desc.content \
                                             -workers %THREADS
echo "done" >> ../results/process.log

echo "#Preparing output." >> ../results/process.log

if [[ %merge_hits.content = "1" ]] ; then

# pLM-BLAST tends to yield rather short hits therefore it is beneficial to merge those associated
# with a single database sequence; additionally, a more strict score cut-off is used
python3.9 $PLMBLASTPATH/scripts/merge.py ../results/${JOBID}.hits.csv \
                                         ../results/${JOBID}.hits_merged.csv -score \
                                         %alignment_cutoff.content
mv ../results/${JOBID}.hits_merged.csv ../results/${JOBID}.hits.csv

fi

python3.9 $PLMBLASTPATH/scripts/csv2nice.py ../results/${JOBID}.hits.csv > ../results/${JOBID}.hits.txt

plmblast_csv_to_json.py ../results/${JOBID}.hits.csv ../results/results.json

# add DB to json
manipulate_json.py -k 'db' -v '%plmblastdb.content' ../results/results.json

plmblastviz.pl ${JOBID} ../results/ ../results/

# Generate Query in JSON
sed 's/[\.\-]//g' ../results/${JOBID}.fas > ../results/query.fas
fasta2json.py ../results/query.fas ../results/query.json

echo "done" >> ../results/process.log