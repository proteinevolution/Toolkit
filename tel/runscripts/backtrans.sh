#perl ../../scripts/makeCUT.pl -i=#{@cutorganism} \
                              #-o=#{@cutfile} \

backtranslate.pl  -o=../results/output \
                       -i=%alignment.path \
                       -oformat=%inc_amino.content \
                       -g=%genetic_code.content
