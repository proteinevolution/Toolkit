class BacktransAction < Action

  BACKTRANS = File.join(BIOPROGS, 'backtranslate')
  
  
  attr_accessor :sequence_input, :sequence_file, :mail, :jobid

  validates_input(:sequence_input, :sequence_file, {:informat => 'fas', 
                                                    :inputmode => 'sequence',
                                                    :on => :create })

  validates_jobid(:jobid)
  
  validates_email(:mail)
  
  validates_shell_params(:jobid, :mail, {:on => :create})
  
  def before_perform
    @basename = File.join(job.job_dir, job.jobid)
    @infile  = @basename+".fasta"
    @cutfile = @basename+".cut"    
    @outfile = @basename+".out"
    params_to_file(@infile, 'sequence_input', 'sequence_file')

    @commands = []

    @showseqs = params['showseqs'] ? "amino" : "fas"
    @gencode = params['gencode']
    @cut = params['cut'] ? true : false
    @cutorganism = params['cutorganism']
  end

  def perform
    params_dump
    
    if @cut && !@cutorganism.nil? && !@cutorganism.empty?
    
      @cutorganism.strip!
      @commands << "#{BACKTRANS}/makeCUT.pl -i '#{@cutorganism}' -o #{@cutfile} &> #{job.statuslog_path}.cut"
      
      @commands << "#{BACKTRANS}/backtranslate.pl -i=#{@infile} -o=#{@outfile} -oformat=#{@showseqs} -g=#{@gencode} -c=#{@cutfile} -cformat=1 &> #{job.statuslog_path}"
    
    else

      @commands << "#{BACKTRANS}/backtranslate.pl -i=#{@infile} -o=#{@outfile} -oformat=#{@showseqs} -g=#{@gencode} &> #{job.statuslog_path}"
    
    end

    logger.debug "Commands:\n"+@commands.join("\n")
    queue.submit(@commands)
  end
end
