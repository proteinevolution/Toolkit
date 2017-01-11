
#perl ../../scripts/makeCUT.pl -i=#{@cutorganism} \
                              #-o=#{@cutfile} \


perl %BIOPROGS/tools/backtranslate/backtranslate.pl  -o=../results/backtrans_out \
                                     -i=%alignment.path \
                                     -oformat=fas \
                                     -g=%genetic_code.content
