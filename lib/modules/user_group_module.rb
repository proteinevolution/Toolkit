module UserGroupModule
  def is_active?(tool)
    if (@user.nil?)
      # no user is logged in

      # get IP
      ip = ""
      if ENV['RAILS_ENV'] == "development"
        ip = request.remote_ip
      else
        ip = request.remote_ip
      end

      if is_internal?(ip) 
        # check if tool is visible for the 'internal' group
        return tool['active'].include?('internal')
      else
        # check if tool is visible for the 'external' group
        return tool['active'].include?('external')
      end
    else
      #logger.debug "User: "+ @user.login
      #logger.debug "Groups: "+ @user.groups
      # get groups of the user
      groups = @user.groups.split(',')
      if groups.include?('admin') then return true end
      groups.each do |group|
      #logger.debug "Group: " + group
      #logger.debug tool["active"]
        if tool['active'].include?(group)
          return true
        end  	
      end
      ip = request.remote_ip
      if is_internal?(ip)
        return tool['active'].include?('internal')
      end
    end
    return false
  end

#########################################################################################
# Method needed for module method  is_active?
#
#########################################################################################  
  def is_internal?(ip)
   begin
      ip = IPAddr.new(ip)
      INT_IPS.each do |mask|
        if mask.include?(ip) then return true end		
      end	
      return false
    rescue Exception => e
      return false
    end
  end
  
  def is_ip_blocked?(ip)
    begin
      ip = IPAddr.new(ip)
      BLOCK_IPS.each do |mask|
        if mask.include?(ip) then return true end   
      end 
      return false
    rescue Exception => e
      return false
    end
  end

end
