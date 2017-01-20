reformat_protblast.pl -f=%alignment.path \
                      -a=%alignment.path \
                      -i=fas \
                      -o=fas \






def perform
#    params_dump
#     #if (params['informat'] == 'gi')
#        #check_GI
#    #end
#    # TEST if this reformats our wrecked input
#    @commands << "source #{SETENV}" 
#    @commands << "#{UTILS}/reformat_protblast.pl -f=#{@infile} -a=#{@infile} -i=fas -o=fas &> #{@infile}.reform_log " 
#    @commands << "echo 'Starting BLAST+ search!' &> #{job.statuslog_path}"
#    #@commands << "#{BLAST}/#{@program} -i #{@infile} -e #{@expect} -F #{@filter} -M #{@mat_param} -G #{@gapopen} -E #{@gapext} #{@ungapped_alignment} -v #{@descriptions} -b #{@alignments} -T T -o #{@outfile} -d \"#{@db_path}\" -I T -a #{@nthreads} #{@other_advanced} >>#{job.statuslog_path}"
#    @commands << "#{BLASTP}/#{@program} -db \"#{@db_path}\" -query #{@infile} -matrix #{@mat_param} -evalue #{@expect} -gapopen #{@gapopen} -gapextend #{@gapext} -num_threads #{@nthreads} -num_descriptions #{@descriptions} -num_alignments #{@alignments} -out #{@outfile} -html -show_gis -seg #{@filter} #{@ungapped_alignment} #{@other_advanced} >>#{job.statuslog_path}"
#    @commands << "echo 'Finished BLAST+ search!' >> #{job.statuslog_path}"
#    @commands << "#{UTILS}/fix_blast_errors.pl -i #{@outfile} &>#{@basename}.log_fix_errors"
 #  
 #  
 #   @commands << "echo 'Visualizing Blast Output... ' >> #{job.statuslog_path}"
 #   @commands << "#{UTILS}/blastviz.pl #{@outfile} #{job.jobid} #{job.job_dir} #{job.url_for_job_dir_abs} &> #{@basename}.blastvizlog";
 #   @commands << "echo 'Generating Blast Histograms... ' >> #{job.statuslog_path}"
 #   @commands << "#{UTILS}/blasthisto.pl  #{@outfile} #{job.jobid} #{job.job_dir} &> #{@basename}.blasthistolog";
 #   
 #   #create alignment
 #   @commands << "echo 'Processing Alignments... ' >> #{job.statuslog_path}"
 #   @commands << "#{UTILS}/alignhits_html.pl #{@outfile} #{@basename}.align -fas -no_link -e #{@expect}"
 #   
 #   @commands << "reformat.pl fas fas #{@basename}.align #{@basename}.ralign -M first -r"
 #   @commands << "if [ -s #{@basename}.ralign ]; then hhfilter -i #{@basename}.ralign -o #{@basename}.ralign -diff 50; fi"
 #   @commands << "echo 'Creating Jalview Input... ' >> #{job.statuslog_path}"
 #   @commands << "#{RUBY_UTILS}/parse_jalview.rb -i #{@basename}.ralign -o #{@basename}.j.align"
 #   @commands << "reformat.pl fas fas #{@basename}.j.align #{@basename}.j.align -r"
 # 
 #   @commands << "#{HELPER}/blast-parser.pl -i #{@outfile} --add-links > #{@outfile}_out"
 #  @commands << "mv  #{@outfile}_out #{@outfile}"
 # 
 #   @commands << "source #{UNSETENV}"

 #   logger.debug "Commands:\n"+@commands.join("\n")
 #   queue.submit(@commands, true, { 'cpus' => "#{@nthreads}" })
#
#  end  
#end

