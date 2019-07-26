cp %alignment.path ../results/${JOBID}.in

SEQ_COUNT=$(egrep '^>' ../results/${JOBID}.in | wc -l)
CHAR_COUNT=$(wc -m < ../results/${JOBID}.in)

if [[ ${CHAR_COUNT} -gt "10000000" ]] ; then
      echo "#Input may not contain more than 10000000 characters." >> ../results/process.log
      false
fi

if [[ ${SEQ_COUNT} -lt "2" ]] && [[ ${FORMAT} = "0" ]] ; then
      echo "#Invalid input format. Input should be in aligned FASTA/CLUSTAL/A3M format." >> ../results/process.log
      false
fi

if [[ ${FORMAT} = "1" ]] ; then
      reformatValidator.pl clu fas \
            $(readlink -f ../results/${JOBID}.in) \
            $(readlink -f ../results/${JOBID}.tmp)

    if [[ ! -f ../results/${JOBID}.tmp ]]; then
        IN_FORMAT=%in_format.content
        echo "#Input is not in valid CLUSTAL format." >> ../results/process.log
        false
    else

        mv ../results/${JOBID}.tmp ../results/${JOBID}.in
    fi

fi

IN_FORMAT=%in_format.content
OUT_FORMAT=%out_format.content
LINE_LENGTH=100

if [[ ${IN_FORMAT} = "a3m" ]] ; then
    A3M_INPUT=$(head -1 ../results/${JOBID}.in | egrep "^#A3M#" | wc -l)
    if [[ ${A3M_INPUT} = "1" ]] ; then
        sed -i '1d' ../results/${JOBID}.in
    fi
fi

if [[ ${OUT_FORMAT} = "clu" ]] ; then
    LINE_LENGTH=60
fi

echo "#Converting input from ${IN_FORMAT^^} to ${OUT_FORMAT^^} format." >> ../results/process.log

CASE="-uc"
if [[ ${OUT_FORMAT} = "a3m" ]] ; then
   CASE=""
fi

reformatValidator.pl ${IN_FORMAT} ${OUT_FORMAT} \
	        $(readlink -f ../results/${JOBID}.in) \
            $(readlink -f ../results/${JOBID}.out) \
            -l ${LINE_LENGTH} ${CASE}

if [[ ${OUT_FORMAT} = "a3m" ]] ; then
    sed -i "1 i\#A3M#" ../results/${JOBID}.out
fi


if [[ ! -f ../results/${JOBID}.out ]]; then
    IN_FORMAT=%in_format.content
    echo "#Input is not in valid ${IN_FORMAT^^} format." >> ../results/process.log
    false
fi
