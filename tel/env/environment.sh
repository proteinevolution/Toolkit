#!/bin/bash


BIOPROGS="/ebio/abt1_share/toolkit_support1/code/bioprogs"
DATABASES="/ebio/abt1_share/toolkit_sync/databases"



# Databases
export STANDARDNEW="${DATABASES}/standard/NewToolkitDBs"
export TAXONOMY="${DATABASES}/standard/taxonomy"

# JAVA
##########################################################
export JAVA_HOME=/ebio/abt1_share/toolkit_support1/code/bioprogs/dependencies/jdk1.8.0_112
export JRE_HOME=/ebio/abt1_share/toolkit_support1/code/bioprogs/dependencies/jdk1.8.0_112/jre


# PERL
##########################################################
export PERL5LIB=/ebio/abt1_share/toolkit_support1/code/bioprogs/lib

# HHLIB
export HHLIB=/ebio/abt1_share/toolkit_support1/code/bioprogs/tools/hh-suite-build

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
export PATH="${BIOPROGS}/tools/ncbi-blast-2.5.0+/bin:${PATH}" # NCBI-BLAST
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

# Setup Blammer
export BLAMMERJAR="${BIOPROGS}/tools/blammer/blammer.jar"
export BLAMMERCONF="${BIOPROGS}/tools/blammer/blammer.conf"
export HMMERBINARIES="${BIOPROGS}/tools/hmmer3/binaries"


# Setup Mafft
export MAFFT_BINARIES="${BIOPROGS}/tools/mafft2/binaries"

# Setup MARCOIL
MARCOILMTIDK="${BIOPROGS}/tools/marcoil/R5.MTIDK"
MARCOILMTK="${BIOPROGS}/tools/marcoil/R5.MTK"
MARCOILINPUT="${BIOPROGS}/tools/marcoil/Inputs"

# PYTHONPATH FOR PDBX
export PYTHONPATH=/ebio/abt1_share/toolkit_support1/code/bioprogs/tools/modeller_9.15/modlib/:/ebio/abt1_share/toolkit_support1/code/bioprogs/dependencies/pdbx

