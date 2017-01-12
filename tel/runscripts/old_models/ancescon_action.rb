class AncesconAction < Action
  ANCESCON = File.join(BIOPROGS, 'ancescon')

  attr_accessor :informat, :sequence_input, :sequence_file, :jobid, :mail, :otheradvanced, :full_name

  validates_input(:sequence_input, :sequence_file, {:informat_field => :informat,
                                                    :informat => 'fas',
                                                    :inputmode => 'alignment',
                                                    :min_seqs => 2,
                                                    :max_seqs => 10000,
                                                    :on => :create })

  validates_jobid(:jobid)

  validates_email(:mail)

  validates_shell_params(:jobid, :mail, :otheradvanced, {:on => :create})

  #validates_tree_file(:treefile)

  def before_perform
    @basename = File.join(job.job_dir, job.jobid)
    @seqfile = @basename+".in"
    @alnfile = @basename+".aln"
    @namesfile = @basename+".names"
    @outfile = @basename+".out"
    params_to_file(@seqfile, 'sequence_input', 'sequence_file')


    #Don't use the name "tree_file" for the upload tree file because this expression is already in use when generating a tree output!

    @owntreefile = params['treefile']
    #filename = File.join(job.job_dir, 'treefile')
    #params['tree_file'] = filename

    @commands = []
    @informat = params['informat'] ? params['informat'] : 'fas'
    reformat(@informat, "fas", @seqfile)
    @informat = "fas"
    @full_name = params['full_name'] ? params['full_name']: 0


    @otheradvanced = params["otheradvanced"] ? params["otheradvanced"] : ""

    #check other advanced options
    @options = ""
    if (@otheradvanced =~ /-o|-O/) then @options += " -O" end

    #the three cases below are parameters which were disabled so far. Enabled by SZ Nam July 2014
    if (@otheradvanced =~ /-ri|-RI/) then @options += " -RI" end
    if (@otheradvanced =~ /-pa|-PA/) then @options += " -PA" end
    if (@otheradvanced =~ /-ps|-PS/) then @options += " -PS" end

    #the -t parameter is used for uploading an additional tree file. this functionality is not working so far
    if (@otheradvanced =~ /-t|-T/) then @options += " -T" end

    if (@otheradvanced =~ /-c|-C/) then @options += " -C" end
    if (@otheradvanced =~ /-d|-D/) then @options += " -D" end
    if (@otheradvanced =~ /-r|-R/) then @options += " -R" end
    if (@otheradvanced =~ /-ro|-RO|-R0/) then @options += " -RO" end
    if (@otheradvanced =~ /-pp|-PP/) then @options += " -PP" end
    if (@otheradvanced =~ /-pd|-PD/) then @options += " -PD" end
    if (@otheradvanced =~ /-g (\d+)/ || @otheradvanced =~ /-G (\d+)/) then @options += " -G $1" end

  end


  def init_tree(filename)

    @tree = false
    if (File.exists?(filename) && File.readable?(filename) && !File.zero?(filename))
      @tree = true
    end
    logger.debug "Tree: #{@tree}"

  end


  def perform
    params_dump

    filename = File.join(job.job_dir, 'tree_file')
    init_tree(filename)

    if(!@tree)

    # Replace sequence names 
    seq_num = 0
    res = IO.readlines(@seqfile)
    names = File.new(@namesfile, "w+")
    out = File.new(@seqfile, "w+")

    res.each do |line|
      if (line =~ /^>\s*(\S+?)\s+/)
      	if (@full_name.to_i == 1)
          name = line[0,150]
        else
          name = $1
        end

      	line = "Sequence#{seq_num}".ljust(20)
      	names.write(line + name + "\n")
      	line = ">" + line + "\n"
      	seq_num += 1
      end
      out.write(line)
    end

    names.close
    out.close
  end

    reformat("fas", "clu", @seqfile)

    # Remove CLUSTAL at the beginning of the alignment
    res = IO.readlines(@seqfile)
    res.delete_at(0)
    out = File.new(@alnfile, "w+")
    out.write(res)
    out.close


    #here you run the ancescon program
    @commands << "echo 'Starting Tree Generation... ' >> #{job.statuslog_path}"

    if (@owntreefile)
       @commands << "cp treefile #{@alnfile}.org.tre"
       @commands << "#{ANCESCON}/treenormalizer.pl -n #{@namesfile} -t treefile &> #{job.statuslog_path}"
       @commands << "#{ANCESCON}/ancestral -R -i #{@alnfile} -t treefile -o #{@outfile} #{@options} &> #{job.statuslog_path}"
       @commands << "cp treefile #{@alnfile}.tre"
       @commands << "#{ANCESCON}/ancescontreemerger.pl -n #{@namesfile} -t #{@alnfile}.tre #{job.statuslog_path}"


    else     
        @commands << "#{ANCESCON}/ancestral -i #{@alnfile} -o #{@outfile} #{@options} &> #{job.statuslog_path}"
    @commands << "echo 'Finished Tree Generation... ' >> #{job.statuslog_path}"


    @commands << "cp #{@alnfile}.tre #{@alnfile}.org.tre"
    @commands << "#{ANCESCON}/ancescontreemerger.pl -n #{@namesfile} -t #{@alnfile}.tre &> #{job.statuslog_path}" end
    @commands << "echo 'Finished Tree Labeling... ' >> #{job.statuslog_path}"

    logger.debug "Commands:\n"+@commands.join("\n")
    queue.submit(@commands)
  end
end
