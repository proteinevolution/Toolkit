

if [ %msageneration.content == "hhblits" ] ; then 

    hhblits -cpu 8 \
            -v 2 \
            -i %alignment.path \
            -d %UNIPROT  \
            -o ../results/msagen.hhblits \
            -oa3m ../results/query.a3m \
            -n %msa_gen_max_iter.content \
            -mact 0.35

else
    if [ %msageneration.content == "psiblast" ] ; then

#dependencies in buildali.pl are still wrong atm

        buildali.pl -nodssp \
                    -cpu 4 \
                    -v 1 \
                    -n %msa_gen_max_iter.content  \
                    -diff 1000 %inclusion_ethresh.content %min_cov.content \
                    -a2m ../results/query.a2m
        mv ../results/query.a3m ../results/query.a3m

    fi
fi


# Here assume that the query alignment exists

###


# prepare histograms
# Reformat query into fasta format ('full' alignment, i.e. 100 maximally diverse sequences, to limit amount of data to transfer)
hhfilter -i ../results/query.a3m \
         -o ../results/query.reduced.a3m \
         -diff 100

# max. 160 chars in description
reformat_hhsuite.pl a3m fas ../results/query.reduced.a3m query.fas -d 160 -uc
mv query.fas ../results/query.fas

# Reformat query into fasta format (reduced alignment)  (Careful: would need 32-bit version to execute on web server!!)
hhfilter -i ../results/query.a3m \
         -o ../results/query.reduced.a3m \
         -diff 50

reformat_hhsuite.pl -r a3m fas ../results/query.reduced.a3m query.reduced.fas -uc
mv query.reduced.fas ../results/query.reduced.fas

rm ../results/query.reduced.a3m



# build histogram

reformat.pl -i=a3m \
            -o=fas \
            -f=../results/query.a3m \
            -a=../results/query.full.fas


hhfilter -i ../results/query.reduced.fas \
         -o ../results/query.top.a3m \
         -id 90 \
         -qid 0 \
         -qsc 0 \
         -cov 0 \
         -diff 10


reformat_hhsuite.pl a3m fas ../results/query.top.a3m query.repseq.fas -uc
mv query.repseq.fas ../results/query.repseq.fas

# @commands << "#{HHSUITE}/hhsearch -cpu 4 -v #{@v} -i #{@basename}.hhm -d '#{@dbs}' -o #{@basename}.hhr -p #{@Pmin} -P #{@Pmin}
# -Z #{@max_lines} -z 1 -b 1 -B #{@max_lines} -seq #{@max_seqs} -aliw #{@aliwidth}
# -#{@ali_mode} #{@ss_scoring} #{@realign} #{@mact} #{@compbiascorr}
# -dbstrlen 10000 -cs ${HHLIB}/data/context_data.lib 1>> #{job.statuslog_path} 2>> #{job.statuslog_path}; echo 'Finished search'";


# Perform HHsearch # TODO Include more parameters
hhsearch -cpu 4 \
         -i ../results/query.a3m \
         -d '%hhsuitedb.content'  \
         -o ../results/hhsearch.hhr \
         -p %pmin.content \
         -P %pmin.content \
         -Z %max_lines.content \
          -z 1 \
          -b 1 \
          -B %max_lines.content \
          -dbstrlen 10000 \
          -cs ${HHLIB}/data/context_data.lib 

JOBID=%jobid.content



# Generate input files for hhviz
cp ../results/hhsearch.hhr ../results/${JOBID}.hhr

# Generate graphical display of hits
hhviz.pl ${JOBID} ../results/ ../results/  &> /dev/null

profile_logos.pl ${JOBID} ../results/ ../results/  &> /dev/null

tar xfvz ../results/${JOBID}.tar.gz -C ../results/

tenrep.rb -i ../results/query.repseq.fas -h ../results/${JOBID}.hhr -p 40 -o ../results/query.tenrep_file

parse_jalview.rb -i ../results/query.tenrep_file -o ../results/query.tenrep_file

cp ../results/${JOBID}.png ../results/hitlist.png
cp ../results/${JOBID}.html ../results/hitlist.html


