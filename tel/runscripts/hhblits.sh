#!/bin/bash


#% alignment : A2M


trap 'kill $(jobs -p)' EXIT

perl %BIOPROGS/helpers/reformat.pl -i=%alignment_format.content \
                                   -o=a2m \
                                   -f=%alignment.path \
                                   -a=temp/infile_a2m

# Run HHblits

exit


%BIOPROGS/tools/hhsuite/bin/hhblits -cpu 2 \
                                    -i temp/infile_a2m \
                                    -d %hhblitsdb.content \
                                    -psipred #{PSIPRED}/bin \
                                    -psipred_data #{PSIPRED}/data \
                                    -o #{@outfile}  \
                                    -oa3m #{@a3m_outfile} \
                                    -qhhm #{@qhhmfile    }
                                    #{msa_factor}
                                    -e #{@E_hhblits} \
                                    -n #{@maxit} \
                                    -p #{@Pmin} \
                                    -Z #{@max_lines} \
                                    -z 1  \
                                    -b 1  \
                                    -B #{@max_lines} \
                                    -seq #{@max_seqs} \
                                    -aliw #{@aliwidth} \
                                    -#{@ali_mode} \
                                    #{@realign} \
                                    #{@mact}  \
                                    #{@filter} \
                                    #{@cov_min} 




