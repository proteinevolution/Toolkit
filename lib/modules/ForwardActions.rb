module ForwardActions
  
  
  # Forwarding Params, provides needed Parameters 
  # +tool_from+:: String Name of the tool we want to forward from
  # +tool_to:: String Name of the tool we want to forward to
  def ForwardActions.forwarding_params(tool_from, tool_to) 

  
  #forward_alignment_tools(tool_from, tool_to)
  
  end

 # Forwarding to Alignment tools, provides needed Parameters 
 # +params+:: Array of Parameters given from Frontend
 # +forwarding_file_path+:: String Path to the forwardin file
 def forward_alignment_tools()
   
    res = IO.readlines(File.join(job.job_dir, job.jobid + ".forward"))
    mode = params['fw_mode']
    informat = 'fas'
    if (res[0] =~ /CLUSTAL/ || res[0] =~ /MSAPROBS/ ) then informat = "clu" end
    
    logger.debug "L24 Informat : #{informat}, Res: #{res.length}   "  
    inputmode = "alignment"
    if (!mode.nil? && mode == "sequence")
      inputmode = "sequence"
    end

    controller = params['forward_controller']
    if (controller == "patsearch")
      logger.debug "L32 patsearch"
      {'db_input' => res.join, 'std_dbs' => ""}
    elsif (controller == "pcoils")
      logger.debug "L35 pcoils"
      {'sequence_input' => res.join, 'informat' => informat,'inputmode' => '2'}
    else
      logger.debug "L38 forwarding to: #{params['forward_controller']}"
      {'sequence_input' => res.join, 'inputmode' => inputmode, 'informat' => informat}
    end
end

def  test
  "  -- >  #{self.class} (\##{self.id}): #{self.to_s}"
  
end
  
end
