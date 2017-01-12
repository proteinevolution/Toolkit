class AncesconJob < Job
  
  @@export_ext = ".export"
  def set_export_ext(val)
    @@export_ext = val  
  end
  def get_export_ext
    @@export_ext
  end
  
  # export results
  def export
    ret = IO.readlines(File.join(job_dir, jobid + @@export_ext)).join
  end
    
  
  attr_reader :lines, :newick
  
  def before_results(controller_params)
    @lines = []
    @newick = IO.readlines(File.join(job_dir, jobid + ".aln.tre"))
    res = IO.readlines(File.join(job_dir, jobid + ".out"))
    
    i = 0
    while (i < res.size)
      if (res[i] =~ /The prediction for all ancestral nodes:/) then break end
      i += 1
    end
    
    @lines = res.slice(i..-1)

  end

  
  
  def export_results
    @basename = File.join(job_dir, jobid)
    out = File.new(@basename + ".export", "w+")
    
    out.write(IO.readlines(@basename + ".names"))
    out.write("\n")
    out.write(IO.readlines(@basename + ".out"))
    
    out.close
  end
  
  def export_tree
    @basename = File.join(job_dir, jobid)
    out = File.new(@basename + ".export", "w+")
    
    out.write(IO.readlines(@basename + ".aln.tre"))
    out.write("\n")
    out.write(IO.readlines(@basename + ".names"))
    out.write("\n")
    
    res = IO.readlines(@basename + ".out")
    i = 0
    while (i < res.size)
      if (res[i] =~ /The prediction for all ancestral nodes:/) then break end
      i += 1
    end
    
    out.write(res.slice(i..-1).join)
    
    out.close
  end
end
