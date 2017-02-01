class BlammerAction < Action
  BLAMMER = File.join(BIOPROGS, 'blammer')
  CLUSTALW = File.join(BIOPROGS, 'clustal', 'clustalw')
  HMMER = File.join(BIOPROGS, 'hmmer')

  attr_accessor :sequence_input, :sequence_file, :blastid, :maxevalue, :minimalcoverage, :minimalscore 
 
  attr_accessor :jobid, :mail, :minimalanchor, :maxseqidentity, :maxseqalignment
    																	 
  validates_shell_params(:jobid, :mail, :blastid, :maxevalue, :minimalcoverage, :minimalscore,
                         :minimalanchor, :maxseqidentity, :maxseqalignment, {:on => :create})
  
  validates_format_of(:minimalcoverage, :minimalscore, :minimalanchor, :maxseqidentity, :maxseqalignment,
                      {:with => /^\d+\.?\d*$/, :on => :create, :message => 'Invalid value!'}) 

  validates_blastid(:blastid, {:input => :sequence_input, :file => :sequence_file, :on => :create})
  
  validates_jobid(:jobid)
  
  validates_email(:mail)
  
  def before_perform
    @basename = File.join(job.job_dir, job.jobid)
    @infile = @basename+".in"
    @outfile = @basename+".aln"
   
    @blastid = params['blastid'] ? params['blastid'] : ""
    if !@blastid.empty?
      blastjob = Job.find(:first, :conditions => [ "jobid = ?", @blastid])
     	blast_file = File.join(blastjob.job_dir, blastjob.jobid)
     	if (blastjob.tool == "psi_blast")
     	  blast_file += ".psiblast"
     	elsif (blastjob.tool == "prot_blast")
     	  blast_file += ".protblast"
	elsif (blastjob.tool == "cs_blast")
	  blast_file += ".csblast"
        elsif (blastjob.tool == "psi_blastp")
          blast_file += ".psiblastp"
        elsif (blastjob.tool == "prot_blastp")
          blast_file += ".protblastp"
     	else
     	  blast_file += ".nucblast"     	
     	end
      FileUtils.cp(blast_file, @infile)
    else
      params_to_file(@infile, 'sequence_input', 'sequence_file')
    end
        
    @commands = []

    @maxevalue = params['maxevalue']
    @minimalcoverage = params['minimalcoverage']
    @minimalscore = params['minimalscore']
    @minimalanchor = params['minimalanchor']
    @maxseqidentity = params['maxseqidentity']
    @maxseqalignment = params['maxseqalignment']
    @outformat = params['outformat']
    @html = params['html'] ? "T" : "F"
  end

  def perform
    params_dump
   
    @commands << "#{JAVA_EXEC} -Xmx3G -jar #{BLAMMER}/blammer.jar -conf #{BLAMMER}/blammer.conf -infile #{@infile} -coverage #{@minimalcoverage} -blastmax #{@maxevalue} -cluwidth #{@minimalanchor} -s/c #{@minimalscore} -seqs #{@maxseqalignment} -maxsim #{@maxseqidentity} -html #{@html} -oformat #{@outformat} -dohmmb f -dohmms f -dohmma f -doext f -dotax f -verbose 2 -hmmer #{HMMER}/binaries/ -clustalw #{CLUSTALW}/clustalw -blastdb #{DATABASES}/standard_new/nr -taxdir #{DATABASES}/taxonomy/ &> #{job.statuslog_path}"
 
    logger.debug "Commands:\n"+@commands.join("\n")
    queue.submit(@commands)
  end
end
