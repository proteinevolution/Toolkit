reformat_hhsuite.pl fas clu %alignment.path "$(readlink -f ../results/alignment.clu)"


# Remove CLUSTAL text in alignment.clu
sed -i '/CLUSTAL/Id' ../results/alignment.clu


#Write namesfile
< ../results/alignment.clu  awk 'BEGIN{i=0;count=0}{if($2){i=1; $2=$1;$1=count;count++;print;next};if(!$2 && i==1)exit}' > ../results/names.dat


# Rename sequences
< ../results/alignment.clu  awk 'BEGIN{i=0}{if(/^\s*$/){i=0} else {$1=sequence".i;i++}; print }' > ../results/alignment2.clu


ancestral -i ../results/alignment2.clu \
          -o ../results/anc_out 

cp ../results/alignment2.clu.tre  ../results/alignment2.clu.orig.tre
ancescontreemerger.pl -n ../results/names.dat -t ../results/alignment2.clu.tre

