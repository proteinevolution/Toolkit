class HhblitsAction < Action

  HELPER = File.join(BIOPROGS, "helper")
  attr_accessor :jobid, :hhblits_dbs, :informat, :inputmode, :maxit, :alignmode, :realign, :mact, :maxseq, :width, :Pmin, :maxlines,
                :sequence_input, :sequence_file, :mail, :otheradvanced

  validates_input(:sequence_input, :sequence_file, {:informat_field => :informat, 
                                                    :informat => 'fas', 
                                                    :inputmode => 'alignment',
                                                    :max_seqs => 9999,
                                                    :max_length => 6000,
                                                    :on => :create,
                                                    :ss_allow => true})

  validates_jobid(:jobid)
  validates_email(:mail)
  validates_db(:hhblits_dbs, {:on => :create})
  validates_shell_params(:jobid, :mail, :width, :Pmin, :maxlines, {:on => :create})
  validates_format_of(:width, :Pmin, :maxlines, {:with => /^\d+$/, :on => :create, :message => 'Invalid value! Only integer values are allowed!'}) 

 
 
  def before_perform
    @basename = File.join(job.job_dir, job.jobid)
    @infile = @basename+".in"    
    @outfile = @basename+".hhr"
    #@qhhmfile = @basename+".hhm"  # The -qhhm parameter does no longer exist in the current hhblits version
    @a3m_outfile = @basename+"_out.a3m"
    params_to_file(@infile, 'sequence_input', 'sequence_file')
    @informat = params['informat'] ? params['informat'] : 'fas'
    if (@informat != "a3m")
      reformat(@informat, "fas", @infile)
      @informat = "fas"
    end
    @commands = []

    @db = params['hhblits_dbs']

    @match_mode = ((@informat == "a3m" || @informat == "a2m" || params["match_mode"].nil?) ? '' : params["match_mode"])
    @maxit = params['maxit']
    @E_hhblits = params["EvalHHblits"]
    @cov_min = params["cov_min"].nil? ? '' : '-cov '+params["cov_min"]
    @ali_mode = params["alignmode"]
    @realign = params["realign"] ? "-realign" : "-norealign"
    @filter = params["nofilter"] ? "-noaddfilter" : ""
    if @realign == '-norealign' 
      @mact = ''
    else
      if @ali_mode == 'global' 
        @mact = '-mact 0.0'
      else
        @mact = params["mact"].nil? ? '' : '-mact '+params["mact"]
      end
    end
    @Pmin = params["Pmin"]
    @max_lines = params["maxlines"]
    @max_seqs = params["maxseq"]
    @aliwidth = params["width"]
    @v = 2
    @diff = '-diff 1000'
    @local_dir = "/tmp"

  end

  def before_perform_on_forward
    pjob = job.parent
    @mode = pjob.params['mode']
    logger.debug "L83 Running before_perform_on_forward with @mode=#{@mode}"
    case @mode
    when 'querymsa'
      files = Dir.entries("#{pjob.job_dir}")
      a3m_file = files.include?("#{pjob.jobid}.a3m") ? "#{pjob.jobid}.a3m" : files.detect {|f| f.match /#{pjob.jobid}.*\.a3m/}
      FileUtils.copy_file("#{pjob.job_dir}/#{a3m_file}", "#{@infile}")
      logger.debug "L93 Copy  #{pjob.job_dir}/#{pjob.jobid}.a3m to #{@infile}"
    end
  end

  # Prepare FASTA files for 'Show Query Alignemt', HHviz bar graph, and HMM histograms 
  def prepare_fasta_hhviz_histograms_etc
    # Reformat query into fasta format ('full' alignment, i.e. 100 maximally diverse sequences, to limit amount of data to transfer)
    @commands << "hhfilter -i #{@a3m_outfile} -o #{@local_dir}/#{job.jobid}.reduced.a3m -diff 100"
    @commands << "reformat.pl a3m fas #{@local_dir}/#{job.jobid}.reduced.a3m #{@basename}.fas -d 160"  # max. 160 chars in description 
    
    # Reformat query into fasta format (reduced alignment)  
    @commands << "hhfilter -i #{@a3m_outfile} -o #{@local_dir}/#{job.jobid}.reduced.a3m -diff 50"
    @commands << "reformat.pl -r a3m fas #{@local_dir}/#{job.jobid}.reduced.a3m #{@basename}.reduced.fas"
    
    # Reformat query into the consensus Alignemnt in Fasta Format Parameter -r 
    #@commands << "#{HH}/reformat.pl -r a3m fas #{@local_dir}/#{job.jobid}.reduced.a3m #{@basename}.ms.fas  -r"
    
    @commands << "rm #{@local_dir}/#{job.jobid}.reduced.a3m"
    
    # Generate graphical display of hits
    @commands << "hhviz.pl #{job.jobid} #{job.job_dir} #{job.url_for_job_dir} &> /dev/null"
    
    # Generate profile histograms
    @commands << "profile_logos.pl #{job.jobid} #{job.job_dir} #{job.url_for_job_dir} #{@db}_hhm_db > /dev/null"
    
    # Reformat the query Data (needed for Multiple Alignemnts)
    @commands << "reformat.pl -r a3m fas #{@basename}.in #{@basename}.ms.fas "
    
    # Generate jalview MS Alignment
    @commands << "masterslave_alignment.pl -q #{@basename}.ms.fas  -hhr #{@basename}.hhr -o #{@basename}.ms.out  &> /dev/null"
  end  
  
  def perform
    params_dump
    if (@mode == 'querymsa')
      msa_factor = ''
    else
      msa_factor = @match_mode.empty? ? '' : " -M #{@match_mode}"
    end
    #@commands << "export  PATH=$PATH:#{HHSUITE} "
     @commands << "source #{SETENV} "
    #@commands << "export  HHLIB=#{HHLIB} "
   
    # -qhhm parameter was removed here 
    @commands << "hhblits -cpu 8 -v #{@v} -i #{@infile} -d #{@db}  -o #{@outfile} -oa3m #{@a3m_outfile}  -e #{@E_hhblits} -n #{@maxit} -p #{@Pmin} -Z #{@max_lines} -z 1 -b 1 -B #{@max_lines} -seq #{@max_seqs} -aliw #{@aliwidth} -#{@ali_mode} #{@realign} #{@mact} #{@filter} #{@cov_min} 1>> #{job.statuslog_path} 2>> #{job.statuslog_path}; echo 'Finished search'"

    @commands << "addss.pl #{@a3m_outfile}"

    prepare_fasta_hhviz_histograms_etc    
    
    # Generate a full blown Fasta File for complete Jalview !
    @commands << "reformat.pl a3m fas #{@basename}_out.a3m #{@basename}.full.fas "
    
    @commands << "reformat.pl fas fas #{@basename}.reduced.fas #{@basename}.uc.fas -uc -r"
    @commands << "parse_jalview.rb -i #{@basename}.uc.fas -o #{@basename}.j.fas"
    @commands << "source #{UNSETENV}" 

    # Parse outfile
    @commands << "#{HELPER}/blast-parser.pl -i #{@outfile} --add-links > outfile_temp.hhr"
    @commands << "mv outfile_temp.hhr #{@outfile}"
    logger.debug "Commands:\n"+@commands.join("\n")
    #queue.submit(@commands, true, {'cpus' => '4', 'queue' => QUEUES[:hhblits]})
    # declare as much cpus as are used by the commands
    queue.submit(@commands, true, {'cpus' => '8', 'queue' => QUEUES[:hhblits]})
  end
end

