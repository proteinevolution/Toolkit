class HhalignAction < Action
  
  attr_accessor :sequence_input, :sequence_file, :informat, :seqid, :qid
  attr_accessor :target_input, :target_file, :target_informat
  attr_accessor :jobid, :mail, :otheradvanced

  validates_input(:sequence_input, :sequence_file, {:informat_field => :informat, :on => :create, :max_seqs => 5000, :min_seqs => 2, :inputmode => 'alignment'})

  validates_input(:target_input, :target_file, {:informat_field => :target_informat, :on => :create, :allow_nil => true, :max_seqs => 5000, :inputmode => 'alignment'})
  
  validates_shell_params(:jobid, :mail, :otheradvanced, :seqid, :qid, {:on => :create})

  validates_format_of(:seqid, :qid, {:with => /^\d+$/, :on => :create, :message => 'Invalid value! Only integer values are allowed!'}) 
  
  validates_jobid(:jobid)
  
  validates_email(:mail)
  
  def before_perform
    @basename = File.join(job.job_dir, job.jobid)
    @inbasename = @basename+".1"
    @infile = @inbasename + ".in"
    @targetbasename = @basename+".2"    
    @targetfile = @targetbasename + ".in"
    @outfile = @basename+".hhr"
    
    params_to_file(@infile, 'sequence_input', 'sequence_file')
    @target = params_to_file(@targetfile, 'target_input', 'target_file')
    
    logger.debug "Target: #{@target}"
    
    @informat = params['informat']
    reformat(@informat, "fas", @infile)
    
    if (@target)
      @target_informat = params['target_informat']
      reformat(@target_informat, "fas", @targetfile)
    end
    
    @commands = []
    
    @v = 1
    
    @seqid = params['seqid']
    @qid = params['qid']
    @otheradvanced = params['otheradvanced'] ? params['otheradvanced'] : ""
    
  end
  
  def perform
    params_dump
    
    @commands << "source #{SETENV}"
    
    # Add secondary structure prediction
    @commands << "buildali.pl -v #{@v} -fas -n 0 #{@infile} &> #{job.statuslog_path}"
    
    if (@target)
      # Add secondary structure prediction
      @commands << "buildali.pl -v #{@v} -fas -n 0 #{@targetfile} 2>&1 1>> #{job.statuslog_path}"
      @target = "#{@targetbasename}.a3m"
    else
      @target = "#{@inbasename}.a3m"
    end
    
    # build hmm for the query sequence (Not necessary, hhalign can directly consume a3m/a2m/fasta )
    # @commands << "hhmake -i #{@inbasename}.a3m -qid #{@qid} -id #{@seqid} -o #{@inbasename}.hhm 2>&1 1>> #{job.statuslog_path}"
    
    # if target-sequence exist, build hmm for the target sequence
    #if (!@target.empty?)
    #  @commands << "hhmake -i #{@targetbasename}.a3m -qid #{@qid} -id #{@seqid} -o #{@targetbasename}.hhm 2>&1 1>> #{job.statuslog_path}"
    #end
    
    # No longer supported in hh-suite 3
    #@commands << "#{HHSUITE}/hhsearch -i #{@inbasename}.hhm -d #{DATABASES}/hhpred/cal.hhm -cal"
    
    @commands << "hhalign -v #{@v} -i  #{@inbasename}.a3m  -t #{@target} -o #{@outfile}  -qid #{@qid} -id #{@seqid}  #{@otheradvanced} 2>&1 1>> #{job.statuslog_path}"		
   
   @commands << "source #{UNSETENV}" 

    logger.debug "Commands:\n"+@commands.join("\n")
    queue.submit(@commands)
  end
end




