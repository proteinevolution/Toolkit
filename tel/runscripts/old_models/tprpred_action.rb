class TprpredAction < Action
  TPRPRED = File.join(BIOPROGS, 'tprpred')
  
   if LOCATION == "Munich" && LINUX == 'SL6'
    TPRPREDPERL = "perl "+File.join(BIOPROGS, 'tprpred')
  else
    TPRPREDPERL = File.join(BIOPROGS, 'tprpred')
  end

  attr_accessor :sequence_input, :sequence_file, :mail, :jobid
  attr_accessor :evalue, :minhits, :maxrows, :Evaltprsel

  validates_input(:sequence_input, :sequence_file, {:informat => 'fas', 
                                                    :inputmode => 'sequence',
                                                    :max_seqs => '1',
                                                    :on => :create })

  validates_jobid(:jobid)
  
  validates_email(:mail)
  
  validates_shell_params(:jobid, :mail, :evalue, :minhits, :maxrows, {:on => :create})
  
  validates_format_of(:evalue, {:with => /^\d+\.?\d*(e|e-|E|E-|\.)?\d+$/, 
                                :on => :create,
                                :message => 'Invalid value!' })
  
  validates_format_of(:minhits, :maxrows, {:with => /^\d+$/, :on => :create, :message => 'Invalid value! Only integer values are allowed!'}) 
  
  def before_perform
    @basename = File.join(job.job_dir, job.jobid)
    @infile = @basename+".fasta"    
    @outfile = @basename+".results"
    params_to_file(@infile, 'sequence_input', 'sequence_file')

    pad_infile_min_length(@infile, 36, 'X')
    @commands = []

    @evalue = params['evalue'] ? params['evalue'] : '10000'
    @minhits = params['minhits'] ? params['minhits'] : '1'
    @maxrows = params['maxrows'] ? params['maxrows'] : '100'
    @pssm = params['pssm']
    @advanced = params['advanced_options']
    
  end

  def perform
    params_dump

    if (@advanced == "true")
    	@commands << "#{TPRPRED}/tprpred #{@infile} -r #{@pssm} -o #{@outfile} -E #{@evalue} -e #{@evalue} -N #{@minhits} -n #{@minhits} -L #{maxrows} &> #{job.statuslog_path}"
    else
    	@commands << "#{TPRPRED}/tprpred_wrapper.pl -in #{@infile} -cut #{params['Evaltprsel']} > #{@outfile}"
    end

    logger.debug "Commands:\n"+@commands.join("\n")
    queue.submit(@commands)
  end

# Pad the Input File to contain at least the minimal count of chars
# infile = @infile
# min_length = minimum length of String
# padding = 'X' The Padding Char
def pad_infile_min_length(infile, min_length, padding)
  file = File.open(infile)
  contents = ""
  counter = 0
  file.each {|line|
        counter = counter + 1
        if counter == 2
                line.chomp!
                line = line.ljust(min_length, padding)
        end
        contents = contents + line
  }
  file.close
  file = File.new(infile,"w+")
        file.puts(contents)

  file.close
end


end




