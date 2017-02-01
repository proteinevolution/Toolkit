class ClustalwAction < Action
  CLUSTALW = File.join(BIOPROGS, 'clustalw-2.1')
  CLUSTALO = File.join(BIOPROGS, 'clustal-omega')


  attr_accessor :sequence_input, :sequence_file, :otheradvanced
 
  attr_accessor :jobid, :mail

  validates_input(:sequence_input, :sequence_file, {:informat => 'fas', 
  																	 :on => :create, 
  																	 :min_seqs => 2,
  																	 :max_seqs => 5000,
                                     :header_length => 2000,
  																	 :inputmode => 'sequences'})
  																	 
  validates_shell_params(:jobid, :mail, :otheradvanced, {:on => :create})

  validates_jobid(:jobid)
  
  validates_email(:mail)
  
  def before_perform
    @basename = File.join(job.job_dir, job.jobid)
    @infile = @basename+".in"
    @outfile = @basename+".aln"
    @version = params['version']   

    params_to_file(@infile, 'sequence_input', 'sequence_file')
    @commands = []

    @otheradvanced = params['otheradvanced'] ? params['otheradvanced'] : ""
  
  end

  def perform
    params_dump

    if (@version != '-o')
        cpus = "4-126"
        # How these limits were computed:
        # be em a sufficient memory limit of the tool explored by experience (here 10G).
        # em is used in sge_worker for creating the -l h_vmem request.
        # The value of -l h_vmem is ceil(em / lower_limit), called req_vmem below.
        # lower limit has to satisfy the following formula:
        # req_vmem * min(upper limit, number of cpus) <= available memory
        # First estimate for lower_limit:
        # lower_limit > number of cpus of node * em / available memory of node,
        # here 8 * 10G / 31G = 2.6 or 64 * 10G / 504G = 1.3.
        # Try lower limit = 3:
        # (ceil (10G / 3)) * 8 = 32G. (ceil (10G / 3)) * 64 = 256G.
        # (ceil (10G / 3)) * 48 = 192G.
        # because we want to use only 31G of the small node, we use a lower limit of 4:
        # (ceil (10G / 4)) * 8 = 24G.
        # Then, upper limit has to satisfy reqvmem * upper_limit <= available memory:
        # upper_limit <= available memory / req_vmem
        # upper_limit <= 31G / 3G = 10.3. Because the small nodes only have 8 cpus,
        # that restriction can be ignored.
        # upper_limit <= 504G / 3G = 168. upper_limit <= 251G / 3G = 83.7
        # If we could use 3 as the lower bound,
        # upper_limit <= 504G / 4G = 126. upper_limit <= 251G / 4G = 62.8
        # Because the large nodes have only 64 cpus and the small nodes of the
        # internal cluster only have 48 cpus, that can be ignored.
        # Because we have to provide a value, we use 126.
        # If we'd use an upper limit of 7, we could reduce the lower limit to 3: (ceil (10G / 3)) * 7 = 28G < 31G
        # If we'd use an upper limit of 6, we could reduce the lower limit to 2: (ceil (10G / 2)) * 6 = 30G < 31G
        # If we'd use an upper limit of 3, we could reduce the lower limit to 1: (ceil (10G / 1)) * 3 = 30G < 31G
        shellvar = "NTHREADS"
      @commands << "#{CLUSTALO}/clustalo -i #{@infile} -o #{@outfile} --outfmt=clustal -v --force --threads=$#{shellvar} #{@otheradvanced}  &> #{job.statuslog_path}"
        logger.debug "Commands:\n"+@commands.join("\n")
        queue.submit(@commands, true, { 'cpus' => cpus.to_s(), 'ncpuvar' => shellvar })
    else
    	@commands << "#{CLUSTALW}/clustalw2 -infile=#{@infile} -align #{@otheradvanced} &> #{job.statuslog_path}"

        logger.debug "Commands:\n"+@commands.join("\n")
        queue.submit(@commands)
    end
  end
end

