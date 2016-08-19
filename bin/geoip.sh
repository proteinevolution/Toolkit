#!/usr/bin/env bash

echo $(date -u) "maxmind db updated" >> ../logs/maxmind.log

mkdir -p /ebio/abt1_share/toolkit_support1/data
cd /ebio/abt1_share/toolkit_support1/data
wget http://geolite.maxmind.com/download/geoip/database/GeoLite2-City.mmdb.gz
gunzip -qf GeoLite2-City.mmdb.gz
cd -
