class MarcoilAction < Action
  PCOILS = File.join(BIOPROGS, 'pcoils')
  MARCOIL = File.join(BIOPROGS, 'marcoil_mod')
  UTILS = File.join(BIOPROGS, 'perl')
  COILSDIR = "COILSDIR=#{PCOILS}"
  attr_accessor :sequence_input, :sequence_file, :informat, :mail, :jobid

  validates_input(:sequence_input, :sequence_file, {:informat_field => :informat, 
                                                    :inputmode => 'alignment',
                                                    :max_seqs => 1000,
                                                    :on => :create })

  validates_jobid(:jobid)
  
  validates_email(:mail)
  
  validates_shell_params(:jobid, :mail, {:on => :create})
  
  def before_perform
    @outdir  = job.job_dir.to_s
    @basename = File.join(job.job_dir, job.jobid)
    @infile = @basename+".in"    
    @outfile = @basename+".results"
    params_to_file(@infile, 'sequence_input', 'sequence_file')
    @informat = params['informat'] ? params['informat'] : 'fas'
    reformat(@informat, "fas", @infile)
    @commands = []
    
    #Additional scoring Values
    @is_userdefined = params['is_userdefined']
    @param_i = params['param_i']
    @param_t = params['param_t']
    @param_r = params['param_r']

    @transprob = params['transprob']
    @algorithm = params['algo']
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
     if((@matrix =="-C"|| @matrix =="-C -i"))
         @commands << "#{MARCOIL}/matrix_copy.sh #{MARCOIL}/R5.MTK #{@outdir}/R5.MTK"
         @commands << "#{MARCOIL}/matrix_copy.sh #{MARCOIL}/R5.MTIDK #{@outdir}/R5.MTIDK"
     end
     # case run COILS (no Alignment)
     transprob_file = "#{MARCOIL}/Inputs/R3.transProbHigh"
     if(@transprob =="-L")
         transprob_file ="#{MARCOIL}/Inputs/R3.transProbLow"
     end
     user_values =""  
     if(@is_userdefined)
     user_values = "+r #{@param_r} +t #{@param_t} +i #{@param_i}"
     end

       @commands << "#{MARCOIL}/marcoil #{@algorithm}  #{@matrix}  +dssSl #{user_values}  -T #{transprob_file} -E #{MARCOIL}/Inputs/R2.emissProb  -P #{@basename}    #{@infile} "
 
      if(@matrix =="-C"|| @matrix =="-C -i")
      	@commands << "#{MARCOIL}/prepare_marcoil_gnuplot.pl #{@basename} #{@basename}.ProbListPSSM  "
      else
	@commands << "#{MARCOIL}/prepare_marcoil_gnuplot.pl #{@basename} #{@basename}.ProbList "
      end
	
	

      @commands << "ruby #{MARCOIL}/create_numerical_marcoil.rb #{@outdir}/ "
      # generate numerical output
      #@commands << "#{PCOILS}/create_numerical.rb -i #{@basename} -m #{@matrix.to_s} -s #{@infile.to_s} -w #{@weighting.to_i} "

   
    logger.debug "Commands:\n"+@commands.join("\n")
    queue.submit(@commands)
  end
  
end

