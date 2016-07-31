#!/bin/bash

# Supposed to list all standard database files. A file with the name f is supposed to be a database
# if a corresponding f.pal file exists.


if [ -d /ebio/abt1_share/toolkit_sync/databases/hhpred/new_dbs ] ; then

for f in /ebio/abt1_share/toolkit_sync/databases/hhpred/new_dbs/* ; do 
    

	echo $f `echo $f | sed "s/\/ebio\/abt1_share\/toolkit_sync\/databases\/hhpred\/new_dbs\///"` 	
done

else 

echo "foo bar"
fi
