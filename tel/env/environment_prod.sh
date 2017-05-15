#!/usr/bin/env bash

BIOPROGS="/cluster/toolkit/production/bioprogs/"
DATABASES="/cluster/toolkit/production/databases/"

# Databases
export STANDARDNEW="${DATABASES}/standard"
export TAXONOMY="${DATABASES}/standard/taxonomy"

# JAVA
##########################################################
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
export JRE_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre

# PERL
##########################################################
export PERL5LIB=/cluster/toolkit/production/bioprogs/lib

# HHLIB
export HHLIB=/cluster/toolkit/production/bioprogs/tools/hh-suite-build

# HHpred
export HHPRED_CONFIG=/cluster/toolkit/production/bioprogs/env/hhpred.config

# COILS/PCOILS
export COILSDIR=/cluster/toolkit/production/bioprogs/pcoils

#BACKTRANSLATOR
export BACKTRANSLATORPATH=/cluster/toolkit/production/bioprogs/tools/backtranslate

#SamCC
export SAMCCPATH=/cluster/toolkit/production/bioprogs/tools/samcc

# PATH variable
export PATH="${BIOPROGS}/dependencies/Python-3.5.2/bin:${PATH}" # Python binary
export PATH="${BIOPROGS}/dependencies/hh-suite_misc_scripts:$PATH" # helper scripts from the old Toolkit
export PATH="${BIOPROGS}/tools/hh-suite-build/scripts:${PATH}" # HHSCRIPTS
export PATH="${BIOPROGS}/tools/hh-suite-build/bin:${PATH}" # HHBINS
export PATH="${BIOPROGS}/dependencies/psipred:/ebio/abt1_share/toolkit_support1/code/bioprogs/dependencies/psipred/bin:${PATH}" # PSIPRED
export PATH="${BIOPROGS}/tools/mmseqs2/build/bin:${PATH}" # MMSEQS
export PATH="${BIOPROGS}/dependencies/dssp:${PATH}" # DSSP
export PATH="${BIOPROGS}/tools/tcoffee/bin:${PATH}" # T-COFFEE
export PATH="${BIOPROGS}/tools/backtranslate:${PATH}" # Backtranslate
export PATH="${BIOPROGS}/helpers:${PATH}" # Helpers
export PATH="${BIOPROGS}/tools/blastplus/bin:${PATH}" # NCBI-BLAST
export PATH="${BIOPROGS}/tools/ancescon:${PATH}" # ANCESCON
export PATH="${BIOPROGS}/tools/clustalo/bin:${PATH}" #CLustalOmega
export PATH="${BIOPROGS}/tools/glprobs:${PATH}" # GLProbs
export PATH="${BIOPROGS}/tools/msaprobs:${PATH}" # MSAProbs
export PATH="${BIOPROGS}/tools/muscle:${PATH}" # MUSCLE
export PATH="${BIOPROGS}/tools/tprpred:${PATH}" # TPRPRED
export PATH="${BIOPROGS}/tools/kalign:${PATH}" # KAlign
export PATH="${BIOPROGS}/tools/mafft2/scripts:${PATH}"
export PATH="${BIOPROGS}/tools/aln2plot:${PATH}"
export PATH="${BIOPROGS}/tools/phylip:${PATH}" # PHYLIP
export PATH="${BIOPROGS}/tools/pcoils:${PATH}" # PCOILS
export PATH="${BIOPROGS}/tools/marcoil:${PATH}" # MARCOIL
export PATH="${BIOPROGS}/tools/modeller_9.15/bin:${PATH}" # MODELLER
export PATH="${BIOPROGS}/helpers/anolea_bin:${PATH}" # Helpers MODELLER QUALITY CHECK
export PATH="${BIOPROGS}/helpers/Solvx:${PATH}" # Helpers MODELLER QUALITY CHECK
export PATH="${BIOPROGS}/helpers/verify3d:${PATH}" # Helpers MODELLER QUALITY CHECK
export PATH="${BIOPROGS}/dependencies/Python-3.5.2/bin:${PATH}"
export PATH="${BIOPROGS}/tools/hmmer/binaries:${PATH}" # HMMER
export PATH="${BIOPROGS}/tools/retrieveseq:${PATH}" # RetrieveSeq
export PATH="${BIOPROGS}/tools/seq2id:${PATH}" # Seq2ID
export PATH="${BIOPROGS}/tools/mmseqs2-build/bin:${PATH}" # MMseqs2
export PATH="${BIOPROGS}/tools/patternsearch:${PATH}" # PatternSearch
export PATH="${BIOPROGS}/tools/sixframe:${PATH}" # 6FrameTranslation
export PATH="${BIOPROGS}/tools/hhrepid/bin:${PATH}" # HHrepid

#Setup Ali2D
export ALI2DPATH="${BIOPROGS}/tools/ali2d"

#Setup SamCC
export SAMCCPATH="${BIOPROGS}/tools/samcc"

# Setup HHrepid
export HHREPIDPATH="${BIOPROGS}/tools/hhrepid"

#Setup HMMER
export HMMERPATH="${BIOPROGS}/tools/hmmer/bin"

# Setup Blammer
export BLAMMERJAR="${BIOPROGS}/tools/blammer/blammer.jar"
export BLAMMERCONF="${BIOPROGS}/tools/blammer/blammer.conf"

export HMMERBINARIES="${BIOPROGS}/tools/hmmer/binaries"

# Setup Mafft
export MAFFT_BINARIES="${BIOPROGS}/tools/mafft2/binaries"

# Setup MARCOIL
MARCOILMTIDK="${BIOPROGS}/tools/marcoil/R5.MTIDK"
MARCOILMTK="${BIOPROGS}/tools/marcoil/R5.MTK"
MARCOILINPUT="${BIOPROGS}/tools/marcoil/Inputs"

# PYTHONPATH FOR PDBX AND MODELLER
export PYTHONPATH=${BIOPROGS}/tools/modeller_9.15/modlib/:${BIOPROGS}/dependencies/pdbx

# Setup PHYLIP (needed by the Perl Script of Phylip)
export PHYLIPBIN=${BIOPROGS}/tools/phylip/current/bin64


# Reformat version with PHYLIP Support (new reformat.pl does not have this support)
export REFORMAT_PHYLIP=${BIOPROGS}/helpers/reformat_protblast.pl