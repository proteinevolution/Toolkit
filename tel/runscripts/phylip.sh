cp ../params/alignment ../params/alignment.in

execNeighbor.pl -i %alignment.path \
                -s $RANDOM \
                -L 200 \
                -t a \
                -M %matrix_phylip.content \
                -b 10 \
                -g 1

cp ../params/alignment* ../results/
