#!/usr/bin/env bash

set -e
set -o pipefail

git clone https://github.com/felixgabler/maxmind-geoip2-scala.git $HOME/repo/pizza
cd $HOME/repo/pizza; sbt publishLocal; cd -
