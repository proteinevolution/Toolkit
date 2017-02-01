class MuscleAction < Action
  MUSCLE = 'muscle'

  attr_accessor :sequence_input, :sequence_file, :otheradvanced, :maxrounds
 
  attr_accessor :jobid, :mail

  validates_input(:sequence_input, :sequence_file, {:informat => 'fas', 
  																	 :on => :create, 
  																	 :max_seqs => 5000,
  																	 :min_seqs => 2,
  																	 :inputmode => 'sequences'})
  																	 
  validates_shell_params(:jobid, :mail, :otheradvanced, :maxrounds, {:on => :create})
  
  validates_format_of(:maxrounds, {:with => /^\d+$/, :on => :create, :message => 'Invalid value! Only integer values are allowed!'}) 

  validates_jobid(:jobid)
  
  validates_email(:mail)
  
  def before_perform
    @basename = File.join(job.job_dir, job.jobid)
    @infile = @basename+".in"
    @outfile = @basename+".aln"
   
    params_to_file(@infile, 'sequence_input', 'sequence_file')
    @commands = []

    @outformat = params['outformat']
    @outorder = params['outorder']
    if !@outorder.nil?
      @outorder = " -#{@outorder}"
    end

    @maxrounds = params['maxrounds'] ? params['maxrounds'].to_i : 1
    @otheradvanced = params['otheradvanced'] ? params['otheradvanced'] : ""

    if (@outformat == "clustal")
      @outformat = "-clwstrict"
    else
      @outformat = ""
    end
    
    if (@maxrounds < 1) then @maxrounds = 1 end
    if (@maxrounds > 50) then @maxrounds = 50 end
  end

  def perform
    params_dump

   
    @commands << "#{MUSCLE} -in #{@infile} -out #{@outfile} -maxiters #{@maxrounds}#{@outorder} #{@outformat} #{@otheradvanced} &> #{job.statuslog_path}"
 

    logger.debug "Commands:\n"+@commands.join("\n")
    queue.submit(@commands)

  end

end

