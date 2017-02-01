class PcoilsAction < Action
  PCOILS = File.join(BIOPROGS, 'pcoils')
  COILSDIR = "COILSDIR=#{PCOILS}"
  
  if LOCATION == "Munich" && LINUX == 'SL6'
      PCOILSPERL = "perl "+File.join(BIOPROGS, 'pcoils')
      UTILS      = "perl "+File.join(BIOPROGS, 'perl')
  else
      UTILS = File.join(BIOPROGS, 'perl')
      PCOILSPERL = File.join(BIOPROGS, 'pcoils')
  end
  
  
  
  
  attr_accessor :sequence_input, :sequence_file, :informat, :mail, :jobid

  validates_input(:sequence_input, :sequence_file, {:informat_field => :informat, 
                                                    :inputmode => 'alignment',
                                                    :max_seqs => 1000,
                                                    :on => :create })

  validates_jobid(:jobid)
  
  validates_email(:mail)
  
  validates_shell_params(:jobid, :mail, {:on => :create})
  
  def before_perform
    @basename = File.join(job.job_dir, job.jobid)
    @infile = @basename+".in"    
    @outfile = @basename+".results"
    params_to_file(@infile, 'sequence_input', 'sequence_file')
    @informat = params['informat'] ? params['informat'] : 'fas'
    reformat(@informat, "fas", @infile)
    @commands = []
    
    @inputmode = params['inputmode']
    @weighting = params['weighting']
    @matrix = params['matrix']
    @psipred = params['psipred'] ? "T" : "F"     
 
    @weight = ""
    if (@weighting == "0") 
      @weight = "-nw"
    end
 
    ### creating files ###
    @buffer           = @basename + ".buffer"
    @coils            = @basename + ".coils"
    @psi              = @basename + ".alignment.psi"
    @a3m              = @basename + ".alignment.a3m"
    @a3m_unfiltered   = @basename + ".alignment_unfiltered.a3m" 
    @psitmplog        = @basename + ".psitmp.log"
    @hhmake_output    = @basename + ".hhmake.out"
    @myhmmmake_output = @basename + ".myhmmmake.out" 
    @horizfile        = @basename + ".horiz"
     
  end

  def perform
    params_dump
    
    @commands << "source #{SETENV}"

    # case run COILS (no Alignment)
    if (@inputmode == "0")
      @program_for_matrix = ['run_Coils_iterated', 'run_Coils_pdb', 'run_Coils', 'run_Coils_old']

      @commands << "reformat.pl fas fas #{@infile} #{@infile} -uc -r -M first"
      @commands << "#{PCOILSPERL}/deal_with_sequence.pl #{@basename} #{@infile} #{@buffer}"

      @commands << "export #{COILSDIR}"

      ['14', '21', '28'].each do |size|
        @commands << "#{PCOILS}/#{@program_for_matrix[@matrix.to_i]} #{@weight} -win #{size} < #{@buffer} > #{@coils.sub(/^.*\/(.*)$/, '\1')}_n#{size}"
      end
      @commands << "#{PCOILSPERL}/prepare_coils_gnuplot.pl #{@basename} #{@coils}_n14 #{@coils}_n21 #{@coils}_n28"

      # generate numerical output
      @commands << "#{PCOILS}/create_numerical.rb -i #{@basename} -m #{@matrix.to_s} -s #{@infile.to_s} -w #{@weighting.to_i} "

    # case run PCOILS (Run PSI-Blast or Use input alignment)
    else
      @program_for_matrix = ['run_PCoils_iterated', 'run_PCoils_pdb', 'run_PCoils', 'run_PCoils_old']


      @commands << "reformat.pl fas fas #{@infile} #{@infile} -uc -r -M first"
      @commands << "#{PCOILSPERL}/deal_with_sequence.pl #{@basename} #{@infile} #{@buffer}"

      # case run PSI-BLAST
      if (@inputmode == "1")

        @commands << "#{PCOILSPERL}/runpsipred_coils.pl #{@buffer}"
        @commands << "#{UTILS}/alignhits.pl -psi -b 1.0 -e 1E-4 -q #{@infile} #{@psitmplog} #{@psi}"
        @commands << "reformat.pl -uc -num #{@psi} #{@a3m_unfiltered}"
        @commands << "hhfilter -i #{@a3m_unfiltered} -qid 40 -cov 20 -o #{@a3m}"
        @commands << "reformat.pl -M first -r -uc -num a3m fas #{@a3m} #{@infile}"
        @commands << "reformat.pl -M first -r -uc -num a3m psi #{@a3m} #{@psi}"

      end

      # calling psipred and ncoils
      @commands << "reformat.pl fas a3m #{@infile} #{@a3m} -uc -num -r -M first"
      @commands << "hhmake -i #{@a3m} -o #{@hhmake_output} -pcm 2 -pca 0.5 -pcb 2.5 -cov 20" 
      @commands << "#{PCOILSPERL}/deal_with_profile.pl #{@hhmake_output} #{@myhmmmake_output}"

      #@matrix=0: iterated
      #@matrix=1: PDB
      #@matrix=2: MTIDK -> in sourcecode new.mat
      #@matrix=3: MTK -> in sourcecode old.mat
      #@program_for_matrix = ['run_PCoils_iterated', 'run_PCoils_pdb', 'run_PCoils', 'run_PCoils_old']

      #run Coils over the sequence in the buffer file
      ['14', '21', '28'].each do |size|
        @commands << "cd #{job.job_dir}; #{PCOILS}/#{@program_for_matrix[@matrix.to_i]} #{@weight} -win #{size} -prof #{@myhmmmake_output.sub(/^.*\/(.*)$/, '\1')} < #{@buffer.sub(/^.*\/(.*)$/, '\1')} > #{@coils.sub(/^.*\/(.*)$/, '\1')}_n#{size}"
      end

      #calling psipred
      if (@psipred == "T" && @inputmode != "0")
        @commands << "#{PCOILSPERL}/runpsipred.pl #{@buffer}"
      end
    
      # prepare for gnuplot
      @commands << "#{PCOILSPERL}/prepare_for_gnuplot.pl #{@basename} #{@psipred} #{@inputmode} #{@coils}_n14 #{@coils}_n21 #{@coils}_n28 #{@horizfile}"
      
      #generate numerical output substitue parameter -a with -s and the complete sequences are parsed
      @commands << "#{PCOILS}/create_numerical.rb -i #{@basename} -w #{@weighting} -m #{@matrix} -a #{@infile}  "
    end
    @commands << "source #{UNSETENV}"
    logger.debug "Commands:\n"+@commands.join("\n")
    queue.submit(@commands)
  end
  
end

