JOBID=%jobid.content

tprpred_wrapper.pl -in %alignment.path \
                   -v 1 \
                   -cut %eval_tpr.content > ../results/${JOBID}.tpr

#convert TPRpred output to JSON
tprpred2json.pl ../results/${JOBID}.tpr > ../results/${JOBID}.json