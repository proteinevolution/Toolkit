#!/bin/bash

# This script is run once TEL has started and can set further constants that can be used
# within runscripts. This script must print "key=value" to standard out once invoked, so
# TEL can parse the output. In this way, it is intended to be more flexible than CONSTANTS,
# but also a bit harder to configure. 

# Definitions in CONSTANTS have prevalence over definitions made here. 


# You might (and should) set the special variable 'CONTEXT', which will define the context script
# the runscript will be invoked. If this value ist set to LOCAL, the runscript will be invoked 
# directly without context parent.


L_HOSTNAME=`hostname`

if [ $L_HOSTNAME = "olt" ] || [ $L_HOSTNAME = "rye" ]  ; then

	echo "CONTEXT=sge"
else
	echo "CONTEXT=LOCAL"
fi		 

