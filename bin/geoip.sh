#!/usr/bin/env bash

mkdir -p data
cd data
wget http://geolite.maxmind.com/download/geoip/database/GeoLite2-City.mmdb.gz
gunzip -q GeoLite2-City.mmdb.gz
cd -