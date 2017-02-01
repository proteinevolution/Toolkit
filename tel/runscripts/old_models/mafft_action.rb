class MafftAction < Action
  MAFFT = File.join(BIOPROGS, 'mafft')

  attr_accessor :sequence_input, :sequence_file, :otheradvanced, :gapopen, :offset
 
  attr_accessor :jobid, :mail

  validates_input(:sequence_input, :sequence_file, {:informat => 'fas', 
  																	 :on => :create, 
  																	 :max_seqs => 5000,
  																	 :min_seqs => 2,
  																	 :inputmode => 'sequences'})
  																	 
  validates_shell_params(:jobid, :mail, :otheradvanced, :gapopen, :offset, {:on => :create})
  
  validates_format_of(:gapopen, :offset, {:with => /^\d+\.*\d*$/, :on => :create, :message => 'Invalid value! Only integer values are allowed!'}) 

  validates_jobid(:jobid)
  
  validates_email(:mail)
  
  def before_perform
    @basename = File.join(job.job_dir, job.jobid)
    @infile = @basename+".in"
    @outfile = @basename+".aln"
   
    params_to_file(@infile, 'sequence_input', 'sequence_file')
    @commands = []

    @clustalw = params['clustalw'] ? "--clustalout" : ""
    @alnorder = params['alnorder'] ? "--reorder" : ""
    @gapopen = params['gapopen'] ? params['gapopen'].to_f : 1.53
    @offset = params['offset'] ? params['offset'].to_f : 0.00
    @otheradvanced = params['otheradvanced'] ? params['otheradvanced'] : ""

    if (@gapopen < 0) then @gapopen = 1.53 end
    if (@offset < 0) then @offset = 0.00 end
  end

  def perform
    params_dump

    @commands << "export MAFFT_BINARIES='#{MAFFT}/binaries/'"
    @commands << "#{MAFFT}/scripts/mafft --op #{@gapopen} --ep #{@offset} #{@alnorder} #{@clustalw} #{@other_advanced} #{@infile} > #{@outfile} 2> #{job.statuslog_path}"
 
    logger.debug "Commands:\n"+@commands.join("\n")
    queue.submit(@commands)

  end

end
