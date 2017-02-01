class HhmakemodelAction < Action
  
  attr_accessor :hits
  
  validates_checkboxes(:hits, {:on => :create})
  
  def before_perform
    @basename = File.join(job.job_dir, job.jobid)
    @local_dir = '/tmp'
    
    @mode = params['mode']
    hits = params['hits']		
    
    logger.debug "Mode: #{@mode}"		
    
    @hits = hits.split(' ')		
    @hits = @hits.uniq
    @hits = @hits.join(" ")
    
    @dbs = job.parent.actions.first.params['hhpred_dbs'].nil? ? "" : job.parent.actions.first.params['hhpred_dbs']
    if @dbs.kind_of?(Array) then @dbs = @dbs.join(' ') end

    @commands = []
  end
  
  def before_perform_on_forward
    pjob = job.parent
    @parent_basename = File.join(pjob.job_dir, pjob.jobid)
    @parent_jobdir = pjob.job_dir
    
    FileUtils.copy_file("#{@parent_basename}.a3m", "#{@basename}.a3m")		
    FileUtils.copy_file("#{@parent_basename}.hhm", "#{@basename}.hhm")
		
  end
  
  def perform
    params_dump
 
    @commands << "source #{SETENV}"   
    if (@mode == 'filter')
      #old: @commands << "#{HH}/hhmeta.pl -v 2 -i #{@parent_basename}.hhr -o #{@basename} ... &> #{job.statuslog_path}"
      @commands << "selectTemplates_hhsuite2.pl -i #{@parent_basename} -o #{@basename} -mode 'm' &> #{job.statuslog_path}"      
      prepare_fasta_hhviz_histograms_etc
    else
      #hhmakemodel aufrufen
      #old: @commands << "#{HH}/hhmakemodel.pl -v 2 -m #{@hits} -i #{@parent_basename}.hhr -pir #{@basename}.out"
      @commands << "checkTemplates.pl -i #{@parent_basename}.hhr -q #{@parent_basename}.a3m -pir #{@basename}.out -m #{@hits} -hhdbs #{@dbs} &> #{job.statuslog_path}" 
    end
   
    @commands << "source #{UNSETENV}" 
    logger.debug "Commands:\n"+@commands.join("\n")
    queue.submit(@commands)
  end
  
  # Prepare FASTA files for 'Show Query Alignemt', HHviz bar graph, and HMM histograms 
  def prepare_fasta_hhviz_histograms_etc
    # Reformat query into fasta format ('full' alignment, i.e. 100 maximally diverse sequences, to limit amount of data to transfer)
    @commands << "hhfilter -i #{@basename}.a3m -o #{@local_dir}/#{job.jobid}.reduced.a3m -diff 100"
    @commands << "reformat.pl a3m fas #{@local_dir}/#{job.jobid}.reduced.a3m #{@basename}.fas -d 160"  # max. 160 chars in description 
    
    # Reformat query into fasta format (reduced alignment)  (Careful: would need 32-bit version to execute on web server!!)
    @commands << "hhfilter -i #{@basename}.a3m -o #{@local_dir}/#{job.jobid}.reduced.a3m -diff 50"
    @commands << "reformat.pl -r a3m fas #{@local_dir}/#{job.jobid}.reduced.a3m #{@basename}.reduced.fas"
    @commands << "rm #{@local_dir}/#{job.jobid}.reduced.a3m"
    
    # Generate graphical display of hits
    @commands << "hhviz.pl #{job.jobid} #{job.job_dir} #{job.url_for_job_dir} &> /dev/null"
    
    # Generate profile histograms
    @commands << "profile_logos.pl #{job.jobid} #{job.job_dir} #{job.url_for_job_dir} 1>> #{job.statuslog_path} 2>> #{job.statuslog_path}"
  end
  
  def forward_params
    return { 'sequence_input' => IO.readlines(File.join(job.job_dir, job.jobid + ".out")).join, 'informat' => 'pir' }
  end
  
end


