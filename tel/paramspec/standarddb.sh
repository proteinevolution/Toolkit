#!/bin/bash

# Supposed to list all standard database files. A file with the name f is supposed to be a database
# if a corresponding f.pal file exists.


if [ -d /ebio/abt1_share/toolkit_sync/databases/standard/NewToolkitDBs ] ; then

for f in /ebio/abt1_share/toolkit_sync/databases/standard/NewToolkitDBs/*.pal ; do 
 
     DBNAME=`echo $f | sed 's/.pal//'`
    
     # Database must exist and be a regular file
     if [ -f $DBNAME ] ; then

	echo $DBNAME `echo $DBNAME | sed "s/\/ebio\/abt1_share\/toolkit_sync\/databases\/standard\/NewToolkitDBs\///"` 	
     fi
done

else 

echo "foo bar"
fi
