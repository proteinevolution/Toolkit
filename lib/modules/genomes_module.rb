# this module can be used to patch a controller-class
# to be able to display the genomes-tree with AJAX

module GenomesModule

  def send_gtree
    if( params[:data_type]=="pep") then
        render(:layout => false, :template => "genomes/_proteintree")
    elsif  ( params[:data_type]=="dna") then
        render(:layout => false, :template => "genomes/_dnatree")
    else
      logger.error("Unknown data_type for gtree (pep/dna) '#{params[:data_type]}'");
    end
  end


  def send_ids
    if( params[:genomes_expr] =~ /[^a-zA-Z0-9|&!]/ ) then 
      render(:text => "ERROR: Invalid character found!")
      return
    end
    genomesjar = File.join(BIOPROGS, "genomes", "genomes.jar");
    # do not use the value of params[:data_type] directly - it would be part of a shell execution
    if(params[:data_type]=="pep") then
      cmd = "#{JAVA_1_5_EXEC} -jar #{genomesjar} -pep -taxid -nolog -s '#{params[:genomes_expr]}' 2>&1"
    elsif(params[:data_type]=="dna") then
      cmd = "#{JAVA_1_5_EXEC} -jar #{genomesjar} -dna -taxid -nolog -s '#{params[:genomes_expr]}' 2>&1" 
    else
      logger.error("Unknown data_type for gtree search (pep/dna) '#{params[:data_type]}'");
      #flash[:genomes]='Invalid data_type!'
      redirect_to( :action => 'index' );
    end

    # execute a program and cature its stdout stream stderr is redirected into stdout
    proc = IO.popen(cmd)
    # read all data from stdout before closing the popen process
    output = proc.readlines;
    # close the stream when the program execution has finished to set the exit status of execution in $?
    proc.close
    exit_status = $?.exitstatus
    if( exit_status!=0 ) then
      logger.error("ERROR in execution of #{cmd}")    
      logger.error("ERROR_EXIT_CODE: #{exit_status}")
      logger.error("ERROR_MESSAGE: #{output}")
      # clear @taxids - it is used in ids.rhtml
      @taxids=""    
      # redirect? - no do nothing in this case
    else
      @gtaxids=output
      logger.debug("Expression '#{params[:genomes_expr]}' matched genomes with taxids:\n  #{@gtaxids}");
    end  
    render(:layout => false, :template => "genomes/ids")
  end

  # called from the before_perfom method of action.rb 
  # returns a string containing databases of selected genomes
  def getDBs(data_type) 
    ret = []
    if( params['taxids'] )
      genomesjar = File.join(BIOPROGS, "genomes", "genomes.jar");
      if(data_type=="pep") then
        cmd = "#{JAVA_1_5_EXEC} -jar #{genomesjar} -pep -nolog -dbs #{params['taxids']}"
      elsif(data_type=="dna") then
        cmd = "#{JAVA_1_5_EXEC} -jar #{genomesjar} -dna -nolog -dbs #{params['taxids']}" 
      else
        logger.error("Unknown data_type for gtree search (pep/dna) '#{data_type}'");
        raise("Unknown data_type for gtree search (pep/dna) '#{data_type}'");
      end
      proc = IO.popen(cmd)
      ret = proc.readlines.map{|line| line.chomp};
      proc.close
      exit_status = $?.exitstatus
      if( exit_status!=0 ) then
        raise("ERROR in execution of #{cmd}\nERROR_EXIT_CODE: #{exit_status}\nERROR_MESSAGE: #{ret}\nPARAMS: #{data_type}");    
      end   
    end
    ret
  end
  


end
