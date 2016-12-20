# -*- coding: utf-8 -*-
class HhpredAction < Action
  HHBLITS_DB = File.join(DATABASES, 'hhblits','uniprot20')  
  HHSUITE = File.join(BIOPROGS, 'hhsuite/bin')  

  attr_accessor :informat, :sequence_input, :sequence_file, :jobid, :mail,
                :width, :Pmin, :maxlines, :hhpred_dbs, :genomes_hhpred_dbs,:prefilter

  validates_input(:sequence_input, :sequence_file, {:informat_field => :informat,
                                                    :informat => 'fas',
                                                    :inputmode => 'alignment',
                                                    :max_length => 3000,
                                                    :max_seqs => 10000,
                                                    :on => :create,
                                                    :single_gaps =>false,
						                                        :ss_allow => true })

  validates_jobid(:jobid)

  validates_email(:mail)

  validates_db(:hhpred_dbs, {:genomes_dbs => 'genomes_hhpred_dbs', :on => :create})

  validates_shell_params(:jobid, :mail, :width, :Pmin, :maxlines, {:on => :create})

  validates_format_of(:width, :Pmin, :maxlines, {:with => /^\d+$/, :on => :create, :message => 'Invalid value! Only integer values are allowed!'})

  def before_perform
    @basename = File.join(job.job_dir, job.jobid)
    @seqfile = @basename+".in"

    params_to_file(@seqfile, 'sequence_input', 'sequence_file')
    @commands = []
    @informat = params['informat'] ? params['informat'] : 'fas'
    if (@informat != "a3m" && @informat != "a2m")
      reformat(@informat, "fas", @seqfile)
      @informat = "fas"
    end

    @prefilter = params['prefilter'] ? params['prefilter'] : 'hhblits'
    
    @dbs = params['hhpred_dbs'].nil? ? "" : params['hhpred_dbs']
    if @dbs.kind_of?(Array) then @dbs = @dbs.join(' ') end
    @genomes_dbs = params['genomes_hhpred_dbs'].nil? ? "" : params['genomes_hhpred_dbs']
    if @genomes_dbs.kind_of?(Array) then @genomes_dbs = @genomes_dbs.join(' ') end
    
    @dbs = @dbs + " " + @genomes_dbs
    @maxhhblitsit = params['maxhhblitsit'].nil? ? '2' : params['maxhhblitsit']
    @E_hhblits = params["Ehhblitsval"].nil? ? '' : "-e "+params["Ehhblitsval"]
    @cov_min = params["cov_min"].nil? ? '' : '-cov '+params["cov_min"]
    @qid_min = params["qid_min"].nil? ? '' : '-qid '+params["qid_min"]
    @ali_mode = params["alignmode"]
    @ss_scoring = "-ssm #{params['ss_scoring']}"
    @realign = params["realign"] ? "-realign" : "-norealign"
    if @realign == '-norealign'
      @mact = ''
    else
      if @ali_mode == 'global'
        @mact = '-mact 0.0'
      else
        @mact = params["mact"].nil? ? '' : '-mact '+params["mact"]
      end
    end
    @compbiascorr = params["compbiascorr"].nil? ? '' : (params["compbiascorr"]=='1'? '-sc 1' : '-sc 0 -shift -0.1')
    @Pmin = params["Pmin"].nil? ? 20 : params["Pmin"]
    @max_lines = params["maxlines"].nil? ? 100 : params["maxlines"]
    @max_seqs = params["maxseq"].nil? ? 1 : params["maxseq"]
    @aliwidth = params["width"].nil? ? 80 : params["width"]
    @relaunch = params["relaunch"]
    @hhviz = params["hhviz"]
    @inputmode = params["inputmode"]
    @match_mode = ((@informat == "a3m" || @informat == "a2m" || params["match_mode"].nil?) ? '' : params["match_mode"])
    @sequences = []
    @seqnames  = []
    @v = 1
    @diff = '-diff 100'
    @local_dir = '/tmp'

    # Check if the second line is too long and increase Memory allocation in Tuebingen 
    @memory = check_sequence_length

    process_databases

  end

  def process_databases

    # Expand cdd and interpro as list of member databases
    if (@dbs =~ /cdd_/)
      # kft Dec 2014: In Aug 2012, Jörn replaced usage of pfam by pfamA here.
      # Changed back to pfam now, because pfam meets the official documentation
      # of cdd and the reason of replacing it by pfamA is unknown.
      # (This change was discussed with Hongbo, who appreciated it, too)
      ['pfam_*', 'smart_*', 'KOG_*', 'COG_*', 'cd_*'].each do |db|
        db_path = Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', db))[0]
        if (!db_path.nil?)
          @dbs += " " + db_path
        end
      end
#      @dbs += " " + Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', 'pfam_*'))[0]
#      @dbs += " " + Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', 'smart_*'))[0]
#      @dbs += " " + Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', 'KOG_*'))[0]
#      @dbs += " " + Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', 'COG_*'))[0]
#      @dbs += " " + Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', 'cd_*'))[0]

      @dbs.gsub!(/\s*\S+\/cdd_\S+\s+/, ' ')
    end
    if (@dbs =~ /interpro_/)
      ['pfamA_*', 'smart_*', 'panther_*', 'tigrfam_*', 'pirsf_*', 'supfam_*', 'CATH_*'].each do |db|
        db_path = Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', db))[0]
        if (!db_path.nil?)
          @dbs += " " + db_path
        end
      end

#      @dbs += " " + Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', 'pfamA_*'))[0]
#      @dbs += " " + Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', 'smart_*'))[0]
#      @dbs += " " + Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', 'panther_*'))[0]
#      @dbs += " " + Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', 'tigrfam_*'))[0]
#      @dbs += " " + Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', 'pirsf_*'))[0]
#      @dbs += " " + Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', 'supfam_*'))[0]
#      @dbs += " " + Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', 'CATH_*'))[0]

      @dbs.gsub!(/\s*\S+\/interpro_\S+\s+/, ' ')
    end

    # Replace pdb70_* with new version of pdb
    newpdb = Dir.glob(File.join(DATABASES, 'hhpred', 'new_dbs', 'pdb70_*'))[0]
    @dbs.gsub!(/\S*pdb70_\S+/, newpdb)

    @dbs = @dbs.split(' ')
    @dbs = @dbs.uniq.join(' ')

    # Save databases for realign
    @dbs_realign = @dbs.clone

    # Append /db/scop.hhm or /db/pdb.hhm or /db/cdd.hhm etc. to every database directory
    dbs=""
    @dbs.split(/\s+/).each do |db|
      if db.gsub!(/(cdd|COG|KOG|\/pfam|smart|cd|pfamA|pfamB)(_\S*)/, '\1\2/db/\1.hhm')
#      elsif db.gsub!(/(scop|pdb)([^_]\S*)/, '\1\2/db/\1.hhm')
      elsif db.gsub!(/(scop|pdb)(\S*)/, '\1\2/db/\1.hhm')
      elsif db.gsub!(/SCOPe(\S*)/, 'SCOPe\1/db/scop.hhm')
      elsif db.gsub!(/(panther|tigrfam|pirsf|supfam|CATH)(_\S*)/, '\1\2/db/\1.hmm')
      elsif db.gsub!(/([^\/]+)$/, '\1/db/\1.hhm' )
      end
      dbs += db+' '
    end
    dbs.strip!

    # Extract names from database directories for echo in log file
    @dbnames = dbs.dup
    @dbnames.gsub!(/\S*\/([^\/]*)\/db\/\S*/, '\1,')
    @dbnames.gsub!(/,\s*$/, '')
    @dbnames.gsub!(/,\s*(\S+)$/, ' and \1 databases')

    @dbs = dbs
  end

  # Prepare FASTA files for 'Show Query Alignemt', HHviz bar graph, and HMM histograms
  def prepare_fasta_hhviz_histograms_etc
    # Reformat query into fasta format ('full' alignment, i.e. 100 maximally diverse sequences, to limit amount of data to transfer)
    @commands << "hhfilter -i #{@basename}.a3m -o #{@local_dir}/#{job.jobid}.reduced.a3m -diff 100"
    @commands << "reformat.pl a3m fas #{@local_dir}/#{job.jobid}.reduced.a3m #{@basename}.fas -d 160 -uc"  # max. 160 chars in description

    # Reformat query into fasta format (reduced alignment)  (Careful: would need 32-bit version to execute on web server!!)
    @commands << "hhfilter -i #{@basename}.a3m -o #{@local_dir}/#{job.jobid}.reduced.a3m -diff 50"
    @commands << "reformat.pl -r a3m fas #{@local_dir}/#{job.jobid}.reduced.a3m #{@basename}.reduced.fas -uc"
    @commands << "rm #{@local_dir}/#{job.jobid}.reduced.a3m"

    # Generate graphical display of hits
    @commands << "hhviz.pl #{job.jobid} #{job.job_dir} #{job.url_for_job_dir} &> /dev/null"

    # Generate profile histograms
    @commands << "profile_logos.pl #{job.jobid} #{job.job_dir} #{job.url_for_job_dir} > /dev/null"
  end

  # Tool can forward to HHpred in different modes, the following modes are possible:
  # 1. :queryhmm  :
  # 2. :querymsa  :
  # 3. :hhsenser  :
  # 4. :realign   :
  # 5. nil        :
  def before_perform_on_forward
    pjob = job.parent
    @mode = pjob.params['mode']
    logger.debug("L202 Running before_perform_on_forward with @mode=#{@mode}")
    case @mode
    when 'queryhmm'
#      FileUtils.copy_file("#{pjob.job_dir}/#{pjob.jobid}.a3m", "#{@basename}.a3m")
#      FileUtils.copy_file("#{pjob.job_dir}/#{pjob.jobid}.hhm", "#{@basename}.hhm")
      files = Dir.entries("#{pjob.job_dir}")
      a3m_file = files.include?("#{pjob.jobid}.a3m") ? "#{pjob.jobid}.a3m" : files.detect {|f| f.match /#{pjob.jobid}.*\.a3m/}
      hhm_file = files.include?("#{pjob.jobid}.hhm") ? "#{pjob.jobid}.hhm" : files.detect {|f| f.match /#{pjob.jobid}.*\.hhm/}
      FileUtils.copy_file("#{pjob.job_dir}/#{a3m_file}", "#{@basename}.a3m")
      FileUtils.copy_file("#{pjob.job_dir}/#{hhm_file}", "#{@basename}.hhm")
    when 'querymsa'
      files = Dir.entries("#{pjob.job_dir}")
      a3m_file = files.include?("#{pjob.jobid}.a3m") ? "#{pjob.jobid}.a3m" : files.detect {|f| f.match /#{pjob.jobid}.*\.a3m/}
      FileUtils.copy_file("#{pjob.job_dir}/#{a3m_file}", "#{@basename}.a3m")
    when 'hhsenser'
      FileUtils.copy_file("#{pjob.job_dir}/#{pjob.jobid}.a3m", "#{@basename}.a3m")
    when 'realign'
      FileUtils.copy_file("#{pjob.job_dir}/#{pjob.jobid}.a3m", "#{@basename}.a3m")
      FileUtils.copy_file("#{pjob.job_dir}/#{pjob.jobid}.hhr", "#{@basename}_parent.hhr")
      dbs = pjob.actions.first.params['hhpred_dbs']
      # TODO: update database paths
      params['hhpred_dbs'] = dbs
      self.save!
      @dbs = dbs.join(' ')
      process_databases
    end
  end

  def perform
    params_dump
    cpus = 1
    a3mFile = "#{@basename}.a3m"
    # Export variable needed for HHSuite
    @commands << "source #{SETENV}"
    # Create a fasta File later on used for the domain resubmission of the results

    if job.parent.nil? || @mode.nil?
      msa_factor = @match_mode.empty? ? '' : " -M #{@match_mode}"
      a2mBase = "#{@basename}.resub_domain"
      a2mFile = "#{a2mBase}.a2m"
      @commands << "reformat.pl #{@informat} a2m #{@seqfile} #{a2mFile}#{msa_factor}"
      # Create alignment

      if(@prefilter=='psiblast')
         cpus = 4
         @commands << "echo 'Running Psiblast for MSA Generation' >> #{job.statuslog_path}"
         if (@informat == "a2m" || @informat == "a3m")
           @commands << "buildali.pl -nodssp -cpu 4 -v #{@v} -n #{@maxhhblitsit} -diff 1000 #{@E_hhblits} #{@cov_min} -#{@informat} #{@seqfile} &>> #{job.statuslog_path}"
         else
           @commands << "buildali.pl -nodssp -cpu 4 -v #{@v} -n #{@maxhhblitsit} -diff 1000 #{@E_hhblits} #{@cov_min} -a2m #{a2mFile} &>> #{job.statuslog_path}"
           @commands << "mv #{a2mBase}.a3m #{a3mFile}"
         end
      else
          if @maxhhblitsit == '0'
            @commands << "echo 'No MSA Generation Set... ...' >> #{job.statuslog_path}"
            @commands << "reformat.pl #{@informat} a3m #{@seqfile} #{a3mFile}#{msa_factor}"
          else
              cpus = 8
              @commands << "echo 'Running HHblits for MSA Generation... ...' >> #{job.statuslog_path}"
              @commands << "hhblits -cpu 8 -v 2 -i #{@basename}.resub_domain.a2m #{@E_hhblits} -d #{HHBLITS_DB} -o #{@basename}.hhblits -oa3m #{a3mFile} -n #{@maxhhblitsit} -mact 0.35 1>> #{job.statuslog_path} 2>> #{job.statuslog_path}"
          end
      end
      @commands << "addss.pl #{a3mFile}"
    end
    if job.parent.nil? || @mode.nil? || @mode == "querymsa"
      # Make HMM file
      @commands << "echo 'Making profile HMM from alignment ...' >> #{job.statuslog_path}"
      @commands << "hhmake -v #{@v} #{@cov_min} #{@qid_min} #{@diff} -i #{a3mFile} -o #{@basename}.hhm 1>> #{job.statuslog_path} 2>> #{job.statuslog_path}"
    end

    if @mode == 'hhsenser'
      if cpus < 4
        cpus = 4
      end
      # Trim alignment
      @commands << "buildali.pl -nodssp -cpu 4 -v #{@v} -n 0 -maxres 300 -diff 1000 -a3m #{a3mFile} &>> #{job.statuslog_path}"
      # Start HHsenser
      @commands << "buildinter.pl -v #{@v} -cpu 4 -Emax 0.1 -e 0.001 -Ey 0.01 -E 0.001 -Ymax 100 -accmax 10 -rejmax 10 -idmax 0 -extnd 20  #{a3mFile} &>> #{job.statuslog_path}"

      # Copy -Y.a3m file to .a3m file
      @commands << "cp #{@basename}-Y.a3m #{a3mFile}"
      # Make HMM file
      @commands << "echo 'Making profile HMM from alignment ...' >> #{job.statuslog_path}"
      @commands << "hhmake -v #{@v} #{@cov_min} #{@qid_min} #{@diff} -i #{@a3mFile} -o #{@basename}.hhm 1>> #{job.statuslog_path} 2>> #{job.statuslog_path}"
    end

    if @mode == 'realign'

      # Make HMM file
      @commands << "echo 'Making profile HMM from alignment ...' >> #{job.statuslog_path}"
      @commands << "hhmake -v #{@v} #{@cov_min} #{@qid_min} #{@diff} -i #{a3mFile} -o #{@basename}.hhm 1>> #{job.statuslog_path} 2>> #{job.statuslog_path}"

      # If cov_min is 20 and qid_min is 0, we can realign with hhm files instead of a3m files ($hhrealign_options="-hhm").
      # This speeds up realignment a lot because we don't have to filter all template alignments.
      realign_options = "-hhm"
      if @cov_min != 20 || @qid_min != 0 then realign_options="#{@cov_min} #{@qid_min} #{@diff}" end

      @commands << "hhrealign.pl -v 2 -resort -i #{@basename}_parent.hhr -o #{@basename}.hhr -q #{@basename}.hhm -d #{@dbs_realign} #{realign_options} #{@ss_scoring} -seq #{@max_seqs} -aliw #{@aliwidth} -#{@ali_mode} #{@realign} #{@mact} #{@compbiascorr} 1>> #{job.statuslog_path} 2>&1";
    else

      if cpus < 4
        cpus = 4
      end
      ####################################################
      ### NO CALIBRATION WITH NEW HHSEARCH VERSION
      #
      #   # Do we need to calibrate query HMM before search?
      #   cal = '-cal'
      #   if @dbs !~ /scop/
      #     cal = ''
      #     @commands << "echo 'Calibrating query HMM ...' >> #{job.statuslog_path}"
      #     @commands << "#{HH}/hhsearch -cpu 4 -v #{@v} -i #{@basename}.hhm -d #{CAL_HHM} -cal -#{@ali_mode} #{@ss_scoring} #{@compbiascorr} -norealign 1>> #{job.statuslog_path} 2>> #{job.statuslog_path}"
      #   end
      #
      #   # HHsearch with query HMM against HMM database
      #   @commands << "echo 'Searching #{@dbnames} ...' >> #{job.statuslog_path}"
      #   @commands << "#{HH}/hhsearch #{cal} -cpu 4 -v #{@v} -i #{@basename}.hhm -d '#{@dbs}' -o #{@basename}.hhr -p #{@Pmin} -P #{@Pmin} -Z #{@max_lines} -B #{@max_lines} -seq #{@max_seqs} -aliw #{@aliwidth} -#{@ali_mode} #{@ss_scoring} #{@realign} #{@mact} #{@compbiascorr} -dbstrlen 10000 1>> #{job.statuslog_path} 2>> #{job.statuslog_path}; echo 'Finished search'";
      #
      ####################################################

      # HHsearch with query HMM against HMM database
      @commands << "echo 'Searching #{@dbnames} ...' >> #{job.statuslog_path}"
      @commands << "#{HHSUITE}/hhsearch -cpu 4 -v #{@v} -i #{@basename}.hhm -d '#{@dbs}' -o #{@basename}.hhr -p #{@Pmin} -P #{@Pmin} -Z #{@max_lines} -z 1 -b 1 -B #{@max_lines} -seq #{@max_seqs} -aliw #{@aliwidth} -#{@ali_mode} #{@ss_scoring} #{@realign} #{@mact} #{@compbiascorr} -dbstrlen 10000 -cs ${HHLIB}/data/context_data.lib 1>> #{job.statuslog_path} 2>> #{job.statuslog_path}; echo 'Finished search'";
    end

    prepare_fasta_hhviz_histograms_etc
    
    @commands << "reformat.pl a3m fas #{a3mFile} #{@basename}.full.fas "

    @commands << "hhfilter -i #{@basename}.reduced.fas -o #{@basename}.top.a3m -id 90 -qid 0 -qsc 0 -cov 0 -diff 10 1>> #{job.statuslog_path} 2>> #{job.statuslog_path}"
    @commands << "reformat.pl a3m fas #{@basename}.top.a3m #{@basename}.repseq.fas -uc 1>> #{job.statuslog_path} 2>> #{job.statuslog_path}"
    @commands << "tenrep.rb -i #{@basename}.repseq.fas -h #{@basename}.hhr -p 40 -o #{@basename}.tenrep_file"
    @commands << "parse_jalview.rb -i #{@basename}.tenrep_file -o #{@basename}.tenrep_file"


    logger.debug "L332 Commands:\n"+@commands.join("\n")
    # queue.submit(@commands, true, {'cpus' => '3', 'memory' => @memory})
    # declare as much cpus as are specified in the commands
    queue.submit(@commands, true, {'cpus' => cpus.to_s(), 'memory' => @memory})
  end

  # Check the length of the first Sequence to determine the access Memory needed for WYE and the large UNIPROT DB
  def check_sequence_length
    # Init local vars
    f = File.open(@seqfile)
    data = f.readlines
      sequence_length = data[1].size
    f.close

    # minimum memory set in worker classes

    if sequence_length > 800
      memory = 64
    end
    if sequence_length > 2000
      memory = 96
    end
    if sequence_length > 2500
      memory = 128
    end

    logger.debug "L357 Memory Allocation - HHpred - : #{memory}"
    memory
  end

end
