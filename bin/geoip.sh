#!/usr/bin/env bash

mkdir -p /ebio/abt1_share/toolkit_support1/data
cd /ebio/abt1_share/toolkit_support1/data
wget http://geolite.maxmind.com/download/geoip/database/GeoLite2-City.mmdb.gz
gunzip -q GeoLite2-City.mmdb.gz
cd -