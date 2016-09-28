#!/bin/bash

trap 'kill $(jobs -p)' EXIT

export HMMER_BINARIES='%BIOPROGS/tools/hmmer3/binaries/'
export CLUSTALW=''
export BLASTDB=''
export TAXDIR=''

java -Xmx3G -jar %BIOPROGS/tools/blammer.jar \
                           -conf %BIOPROGS/tools/blammer.conf \
                           -infile %infile.content \
                           -coverage %minimalcoverage.content \
                           -blastmax %maxevalue.content \
                           -cluwidth %minimalanchor.content \
                           -s/c %minimalscore.content \
                           -seqs %maxseqalignment.content \
                           -maxsim %maxseqidentity.content \
                           -html %html.content \
                           -oformat %outformat.content \
                           -dohmmb f \
                           -dohmms f \
                           -dohmma f \
                           -doext f \
                           -dotax f \
                           -verbose 2 \
                           -hmmer $HMMER_BINARIES \
                           -clustalw $CLUSTALW \
                           -blastdb $BLASTDB \
                           -taxdir $TAXDIR \
