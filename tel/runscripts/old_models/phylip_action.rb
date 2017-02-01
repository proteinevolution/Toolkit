class PhylipAction < Action
  PHYLIP = File.join(BIOPROGS, 'phylip')

  if LOCATION == "Munich" && LINUX == 'SL6'
      PHYLIP   = "perl "+File.join(BIOPROGS, 'phylip')
  else
     PHYLIP = File.join(BIOPROGS, 'phylip')
  end

  attr_accessor :informat, :sequence_input, :sequence_file, :jobid, :mail
  attr_accessor :replicates, :gammavalue

  validates_input(:sequence_input, :sequence_file, {:informat_field => :informat, 
                                                    :informat => 'fas', 
                                                    :inputmode => 'alignment',
                                                    :min_seqs => 3,
                                                    :max_seqs => 200,
                                                    :on => :create })

  validates_jobid(:jobid)
  
  validates_email(:mail)
  
  validates_shell_params(:jobid, :mail, :replicates, :gammavalue, {:on => :create})
  
  validates_int_into_list(:replicates, {:in => 0..200, :message => "should be between 0 and 200", 
  											   		:allow_nil => true, :on => :create})
  													
  validates_format_of(:gammavalue, {:with => /^\d+\.?\d*$/, 
                                    :on => :create,
                                    :allow_nil => true,
                                    :message => 'Invalid value!' })
  
  def before_perform
    @basename = File.join(job.job_dir, job.jobid)
    @infile = @basename+".in"
    @outfile = @basename+".out"            
    params_to_file(@infile, 'sequence_input', 'sequence_file')
    @commands = []
    @informat = params['informat'] ? params['informat'] : 'fas'
    reformat(@informat, "fas", @infile)
    @informat = "fas"
    
    @replicates = params["replicates"] ? params["replicates"] : "100"
    @bootstrap = params["bootstrap"] ? "-b #{@replicates}" : ""
    @gamma = params["gammavalue"] ? "-g #{@gammavalue}" : ""            
    @type = params["ali_type"]            
    @protMat = params["protMat"]
    @dnaMat = params["dnaMat"]
    
    if (@type == "prot")
    	@type = "-t a"
    	@model = "-M #{@protMat}"
    else
      @type = "-t d"
    	@model = "-M #{@dnaMat}"      
    end
    
    @seed = rand(9999) * 4 + 1

  end
  
  def perform
    params_dump
    
    #here you run the phylip program
    @commands << "#{PHYLIP}/execNeighbor.pl -i #{@basename} -s #{@seed} -L 200 #{@type} #{@model} #{@bootstrap} #{@gamma} &> #{job.statuslog_path}"


    logger.debug "Commands:\n"+@commands.join("\n")
    queue.submit(@commands)
    
  end

end




