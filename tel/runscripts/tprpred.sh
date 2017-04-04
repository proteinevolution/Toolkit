JOBID=%jobid.content

reformat_hhsuite.pl fas fas %alignment.path ${JOBID}.fas -l 32000 -uc

mv ${JOBID}.fas ../results

tprpred_wrapper.pl -in ../results/${JOBID}.fas \
                   -v 1 \
                   -cut %eval_tpr.content > ../results/${JOBID}.tpr

#convert TPRpred output to JSON
tprpred2json.pl ../results/${JOBID}.tpr > ../results/${JOBID}.json