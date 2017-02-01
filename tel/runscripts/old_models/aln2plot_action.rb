class Aln2plotAction < Action
  ALN2PLOT = File.join(BIOPROGS, 'aln2plot')
  
  attr_accessor :informat, :sequence_input, :sequence_file, :jobid, :mail

  validates_input(:sequence_input, :sequence_file, {:informat_field => :informat, 
                                                    :informat => 'fas', 
                                                    :inputmode => 'alignment',
                                                    :max_seqs => 1000,
                                                    :min_seqs => 2,
                                                    :on => :create })

  validates_jobid(:jobid)
  
  validates_email(:mail)
  
  validates_shell_params(:jobid, :mail, {:on => :create})
  
  def before_perform

    @basename = File.join(job.job_dir, job.jobid)
    @infile = @basename+".aln"
    
    @commands = []
                
    params_to_file(@infile, 'sequence_input', 'sequence_file')
    
    @informat = params['informat'] ? params['informat'] : 'fas'
    reformat(@informat, "fas", @infile)
    @informat = "fas"
    
  end
  
  def perform
    params_dump
    
    @commands << "#{ALN2PLOT}/aln2plot.pl #{@infile} &> #{job.statuslog_path}"

    logger.debug "Commands:\n"+@commands.join("\n")
    queue.submit(@commands)
  
  end

end
