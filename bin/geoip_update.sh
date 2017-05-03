#!/usr/bin/env bash

#set $http_proxy 
source ~/.bashrc

DBNAME="GeoLite2-City"

function die {

    echo "$1"
    exit "$2"
}

function log {

    echo $(date -u) $1 | tee -a maxmind.log
}

# Check number of arguments
[ "$#" -eq 1 ] || die "Wrong number of arguments. Provide the path where the database should be installed to" 1

# Check whether directory exists
[ -d $1 ] || die "$1 is not a directory" 2

# Try to change into that directory
cd "$1" || die "$1 could not be accessed" 3 

log "Update of Maxmind Database has started"

[ ! -f "${DBNAME}.mmdb.gz" ] || rm  "${DBNAME}.mmdb,gz"


# Make a Backup of the old database
if [ -f "${DBNAME}.mmdb" ] ; then
    


    BAK="${DBNAME}_backup_`date -u | tr ' ' '_'`"
    mkdir "$BAK"   
    mv "${DBNAME}.mmdb" "$BAK" 
    log "Old Database has been updated to `pwd`/$BAK"
fi



wget -q http://geolite.maxmind.com/download/geoip/database/GeoLite2-City.mmdb.gz -O GeoLite2-City.mmdb.gz


gunzip  "GeoLite2-City.mmdb.gz" || die "Error extracting file" 4


if [ "$?" -eq 0 ] ; then

    log "Update has been completed successfully"
    log "Database is available at `pwd`/GeoLite2-City.mmdb.gz" 
else 

    log "Error updating Maxmind Database. Error code was $?" 
fi



