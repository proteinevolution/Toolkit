#!/bin/bash

# Supposed to list all standard database files. A file with the name f is supposed to be a database
# if a corresponding f.pal file exists.


if [ -d /ebio/abt1_share/toolkit_sync/databases/hhblits ] ; then

for f in /ebio/abt1_share/toolkit_sync/databases/hhblits/*.cs219 ; do 

     DBNAME=`echo $f | sed 's/.cs219//'`
   
      	echo $(basename $DBNAME) `echo $DBNAME | sed "s/\/ebio\/abt1_share\/toolkit_sync\/databases\/hhblits\///"`
done

else 

echo "foo bar"
fi
