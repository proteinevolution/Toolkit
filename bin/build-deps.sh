#!/usr/bin/env bash
set -e

dir=$(mktemp -d)
echo "Building in $dir"
cd "$dir"

git clone https://github.com/Sanoma-CDA/maxmind-geoip2-scala
cd maxmind-geoip2-scala
sbt publish-local
cd ..