#!/bin/bash



if [ -d /ebio/abt1_share/toolkit_support1/code/databases/hh-suite ] ; then

for f in /ebio/abt1_share/toolkit_support1/code/databases/hh-suite/*.cs219 ; do 

     DBNAME=`echo $f | sed 's/.cs219//'`
     echo $DBNAME $(basename $DBNAME) 	
done

else 

echo "foo bar"
fi
