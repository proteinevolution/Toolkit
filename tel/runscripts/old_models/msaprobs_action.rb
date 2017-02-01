class MsaprobsAction < Action
  MSAPROBS = File.join(BIOPROGS, 'MSAProbs')



  attr_accessor :sequence_input, :sequence_file
 
  attr_accessor :jobid, :mail

  validates_input(:sequence_input, :sequence_file, {:informat => 'fas', 
  																	 :on => :create, 
  																	 :min_seqs => 2,
  																	 :max_seqs => 5000,
                                     :header_length => 2000,
  																	 :inputmode => 'sequences'})
  																	 
  validates_shell_params(:jobid, :mail, {:on => :create})

  validates_jobid(:jobid)
  
  validates_email(:mail)
  
  def before_perform
    @basename = File.join(job.job_dir, job.jobid)
    @infile = @basename+".in"
    @outfile = @basename+".aln"
    @version = params['version']   

    params_to_file(@infile, 'sequence_input', 'sequence_file')
    @commands = []

  
  end

  def perform
    params_dump
    cpus = "6-64"
    memory = nil

    shellvar = "NTHREADS"

    # useful values to be determined
    #if File.size(@infile) > 4000
    #  cpus = 32-64
    #  memory = 36
    #end

    @commands << "#{MSAPROBS}/msaprobs #{@infile} -o #{@outfile} -clustalw -num_threads $#{shellvar} &> #{job.statuslog_path}"	


    logger.debug "Commands:\n"+@commands.join("\n")
    queue.submit(@commands, true, { 'cpus' => cpus.to_s(), 'memory' => memory, 'ncpuvar' => shellvar })

  end

end

