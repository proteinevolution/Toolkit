module Biolinks

  # return an url for the scopid
  def Biolinks.scop_sid_href(scopid)
    "http://scop.berkeley.edu/sid=#{scopid}"
  end

  # return an url on the pdb code
  def Biolinks.scop_pdb_href(pdbcode)
    # "http://scop.mrc-lmb.cam.ac.uk/scop/pdb.cgi?pdb=#{pdbcode}"
    "http://scop.berkeley.edu/pdb/code=#{pdbcode}"
  end

  # return a html link on the SCOP family
  def Biolinks.scop_family_link(family)
    "<a href=\"http://scop.berkeley.edu/sccs=#{family}\" target=\"_blank\">#{family}</a>"
  end

  # return a html picture link using the SCOPe logo
  def Biolinks.scope_picture_link(href, link_attr, logo_attr)
    # old scop logo
    # "<a href=\"#{href}\" target=\"_blank\" #{link_attr} ><img src=\"#{DOC_ROOTURL}/images/hhpred/logo_SCOP.jpg\" alt=\"SCOP\" title=\"SCOP\" #{logo_attr} height=\"25\"><\/a>"
    # SCOPe logo from scop.berkeley.edu
    "<a href=\"#{href}\" target=\"_blank\" #{link_attr} ><img src=\"http://scop.berkeley.edu//images/scope_logo_tiny.png\" alt=\"SCOP\" title=\"SCOP\" #{logo_attr} height=\"20\"><\/a>"
    # Local copy of SCOPe logo
    #"<a href=\"#{href}\" target=\"_blank\" #{link_attr} ><img src=\"#{DOC_ROOT_URL}/images/hhpred/scope_logo_tiny.png\" alt=\"SCOP\" title=\"SCOP\" #{logo_attr} height=\"20\"><\/a>"
  end

  # return a large, new SCOPe database for looking up pdb files etc.
  # return nil, if none found.
  def Biolinks.scope_db_dir
    dirs = Dir.glob("#{DATABASES}/hhpred/new_dbs/SCOPe95*")
    if (0 == dirs.length)
      dirs = Dir.glob("#{DATABASES}/hhpred/new_dbs/SCOPe70*")
    end
    if (0 < dirs.length)
      # The available ruby guide does not tell about the order of the result,
      # but it seems to be alphabetically in many cases.
      # Then the last one (greatest value) should be the newest.
      return dirs[dirs.length - 1]
    else
      return nil
    end
  end
end
