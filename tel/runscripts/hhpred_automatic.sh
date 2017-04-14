

JOBID=%jobid.content

# fetch parameters from the parent here
cp ../../%parentid.content/results/%parentid.content.hhr ../params
cp ../../%parentid.content/results/%parentid.content.reduced.a3m ../params
cp ../../%parentid.content/results/hhsearch.start.tab ../params


# Rename according to the requirements of selectTemplates
mv ../params/%parentid.content.hhr       ../params/${JOBID}.hhr
mv ../params/%parentid.content.reduced.a3m ../params/${JOBID}.a3m
mv ../params/hhsearch.start.tab ../params/${JOBID}.start.tab

hhmake -i $(readlink -f ../params/${JOBID}.a3m) \
       -diff 100 \
       -o $(readlink -f ../params/${JOBID}.hhm)

selectTemplates.pl -m 100 -i ../params/${JOBID} -o ../results/out.hhr -mode 'm'


