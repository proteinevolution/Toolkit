#!/bin/bash

# Supposed to list all standard database files. A file with the name f is supposed to be a database
# if a corresponding f.pal file exists.


if [ -d  %STANDARD ] ; then

for f in %STANDARD/*.pal ; do
 
     DBNAME=$(echo $f | sed 's/.pal//')
    
     # Database must exist and be a regular file
     if [ -f $DBNAME ] ; then
        fbname=$(basename "$DBNAME")
	    echo "$fbname $fbname"
     fi
done

else 

echo "foo bar"
fi
