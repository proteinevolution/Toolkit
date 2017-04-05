JOBID=%jobid.content

java -Xmx4G -cp $BIOPROGS/tools/sixframe translate \
                      -i %alignment.path \
                      -o ../results/${JOBID}.out \
                      -seq %inc_nucl.content \
                      -mode %codon_table.content \
                      -annot %amino_nucl_rel.content &> ../results/status.log
