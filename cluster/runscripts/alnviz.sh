#!/bin/bash

trap 'kill $(jobs -p)' EXIT

perl !{BIO}/helpers/reformat.pl -i=${alignment_format} -o=clu -f=#{alignment} -a=@{result}
