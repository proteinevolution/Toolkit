class SamccAction < Action

  SAMCC = File.join(BIOPROGS, 'samcc')

  attr_accessor :mail, :jobid, :sequence_input, :sequence_file
  attr_accessor :chain1_letter, :chain1_start, :chain1_end, :chain2_letter, :chain2_start, :chain2_end, :chain3_letter, :chain3_start, :chain3_end, :chain4_letter, :chain4_start, :chain4_end
  attr_accessor :first_position1, :first_position2, :first_position3, :first_position4

  validates_input(:sequence_input, :sequence_file, {:informat => 'pdb', :on => :create})

  validates_jobid(:jobid)

  validates_email(:mail)

  validates_format_of(:chain1_letter, :chain2_letter, :chain3_letter, :chain4_letter, {:on => :create, :with => /\w/, :message => 'Field must be a character.'})

  validates_format_of(:chain1_start, :chain2_start, :chain3_start, :chain4_start, {:on => :create, :with => /\d/, :message => 'Field must be a number.'})

  validates_format_of(:chain1_end, :chain2_end, :chain3_end, :chain4_end, {:on => :create, :with => /\d/, :message => 'Field must be a number.'})

  validates_format_of(:first_position1, :first_position2, :first_position3, :first_position4, {:on => :create, :with => /[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz]/, :message => 'Field must be an alphabetical character.'})


  # Put action initialisation code in here
  def before_perform
    init

    @pdbfile = @basename+".pdb"
    params_to_file(@pdbfile, 'sequence_input', 'sequence_file')
    @outfile = @basename+".out"
    logger.debug "Outfile: #{@outfile}"
    @paramsfile = @basename+".params"
    logger.debug "Paramsfile: #{@paramsfile}"
    #parameters

    @chain1_letter = params['chain1_letter'] ? params['chain1_letter'] : ""
    @chain1_letter.upcase!
    @chain1_start = params['chain1_start'] ? params['chain1_start'] : ""
    @chain1_end = params['chain1_end'] ? params['chain1_end'] : ""
    @chain2_letter = params['chain2_letter'] ? params['chain2_letter'] : ""
    @chain2_letter.upcase!
    @chain2_start = params['chain2_start'] ? params['chain2_start'] : ""
    @chain2_end = params['chain2_end'] ? params['chain2_end'] : ""
    @chain3_letter = params['chain3_letter'] ? params['chain3_letter'] : ""
    @chain3_letter.upcase!
    @chain3_start = params['chain3_start'] ? params['chain3_start'] : ""
    @chain3_end = params['chain3_end'] ? params['chain3_end'] : ""
    @chain4_letter = params['chain4_letter'] ? params['chain4_letter'] : ""
    @chain4_letter.upcase!
    @chain4_start = params['chain4_start'] ? params['chain4_start'] : ""
    @chain4_end = params['chain4_end'] ? params['chain4_end'] : ""
    @periodicity = params['periodicity'] ? params['periodicity'] : ""
    @firstpos1 = params['first_position1'] ? params['first_position1'] : ""
    @firstpos1.downcase!
    @firstpos2 = params['first_position2'] ? params['first_position2'] : ""
    @firstpos2.downcase!
    @firstpos3 = params['first_position3'] ? params['first_position3'] : ""
    @firstpos3.downcase!
    @firstpos4 = params['first_position4'] ? params['first_position4'] : ""
    @firstpos4.downcase!
    @crick = params['crick'] ? "1" : "0"

    logger.debug "Chain1: letter #{@chain1_letter}; start #{@chain1_start}; end #{@chain1_end}"
    logger.debug "Chain2: letter #{@chain2_letter}; start #{@chain2_start}; end #{@chain2_end}"
    logger.debug "Chain3: letter #{@chain3_letter}; start #{@chain3_start}; end #{@chain3_end}"
    logger.debug "Chain4: letter #{@chain4_letter}; start #{@chain4_start}; end #{@chain4_end}"
    logger.debug "Periodicity: #{@periodicity}"
    logger.debug "Crick: #{@crick}"

    save_parameters
  end


  # Put action code in here
  def perform
    @commands << "/usr/bin/python #{SAMCC}/samcc.py #{@paramsfile} #{@outfile} >> #{job.statuslog_path} 2>&1"

    for i in 0..3
      @commands << "cd #{job.job_dir}; /usr/bin/gnuplot temp#{i}.run"
    end
    logger.debug "Commands:\n"+@commands.join("\n")
    queue.submit(@commands)
  end

  def init
    @basename = File.join(job.job_dir, job.jobid)
    @commands = []
  end

  def save_parameters
    res = []
    res << "!:crick:#{@crick}\n"
    res << "!:periodicity:#{@periodicity}\n"
    res << "!:firstpos:#{@firstpos1},#{@firstpos2},#{@firstpos3},#{@firstpos4}\n"
    #res << "!:firstpos2:#{@firstposAP}\n"
    res << "!:pdb:#{@pdbfile}\n"
    res << "#{@chain1_letter}:1:#{@chain1_start}-#{@chain1_end}\n"
    res << "#{@chain2_letter}:2:#{@chain2_start}-#{@chain2_end}\n"
    res << "#{@chain3_letter}:3:#{@chain3_start}-#{@chain3_end}\n"
    res << "#{@chain4_letter}:4:#{@chain4_start}-#{@chain4_end}\n"
    res << "!:ref:#{SAMCC}/beammotifcc_heptad.pdb.res\n"
    res << "1:1:1-1\n"
    res << "2:2:1-1\n"
    res << "3:3:1-1\n"
    res << "4:4:1-1\n"
    res << "!:end\n"

    param = File.open(@paramsfile, "w")
    param.write(res)
    param.close
  end


end




