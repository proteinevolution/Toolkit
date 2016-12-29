#!/bin/bash

# Supposed to list all standard database files. A file with the name f is supposed to be a database
# if a corresponding f.pal file exists.


if [ -d  %NR ] ; then

for f in %NR/*.pin ; do 
 
     DBNAME=$(echo $f | sed 's/.pin//')
    
     # Database must exist and be a regular file
     if [ -f $DBNAME ] ; then

	    echo "$DBNAME $(basename $DBNAME)" 	
     fi
done

else 

echo "foo bar"
fi
