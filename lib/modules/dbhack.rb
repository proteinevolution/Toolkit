module Dbhack

  module ClassMethods

    def find(*args)
		excep = nil
		repeat = 0
		while (repeat < 6)
			begin    	
    			return super
    		rescue Exception => e
    			excep = e
    			repeat += 1
    			ActiveRecord::Base.establish_connection(ActiveRecord::Base.remove_connection())
    		end
    	end
    	raise excep
    end
    
    def save!
		excep = nil
		repeat = 0
		while (repeat < 10)
			begin    	
    			return super
    		rescue Exception => e
    			excep = e
    			repeat += 1
    			ActiveRecord::Base.establish_connection(ActiveRecord::Base.remove_connection())
    		end
    	end
    	raise excep
    end
    
  end
  
  def self.included(receiver)
    receiver.extend(ClassMethods)
  end

end
