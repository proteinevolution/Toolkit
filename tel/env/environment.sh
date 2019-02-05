#!/bin/bash

#TK_ROOT is required by the PCOILS binary
export TK_ROOT="${BIOPROGSROOT}"

export BIOPROGS="${BIOPROGSROOT}/bioprogs"

export STANDARD="${DATABASES}/standard"

#HHomp database
export HHOMPDBPATH="${DATABASES}/hhomp/db"

#JAVA
##########################################################
export JAVA_HOME="${BIOPROGS}/dependencies/jdk1.8.0"
export JRE_HOME="${BIOPROGS}/dependencies/jdk1.8.0/jre"
#PERL
##########################################################
export PERL5LIB="${BIOPROGS}/lib"
#COILS/PCOILS
export COILSDIR="${BIOPROGS}/pcoils"
#ALI2D
export ALI2DPATH="${BIOPROGS}/tools/ali2d"
#HHpred
export HHPRED_CONFIG="${BIOPROGS}/env/hhpred.config"
#CLANS
export CLANSPATH="${BIOPROGS}/tools/clans"
#REPPER
export REPPERDIR="${BIOPROGS}/tools/repper"
#BACKTRANSLATOR
export BACKTRANSLATORPATH="${BIOPROGS}/tools/backtranslate"
#SamCC
export SAMCCPATH="${BIOPROGS}/tools/samcc"
#HHomp
export HHOMPPATH="${BIOPROGS}/tools/hhomp"
#PolyPhobius
export POLYPHOBIUS="${BIOPROGS}/tools/Phobius/PolyPhobius"
#SPOT-D and SPIDER2
export SPOTD="${BIOPROGS}/tools/SPOT-disorder_local/misc"
#IUPred
export IUPred_PATH="${BIOPROGS}/tools/iupred"
#PSSPRED
export PSSPRED="${BIOPROGS}/tools/PSSpred_v3"
#DeepCNF_SS
export DEEPCNF="${BIOPROGS}/tools/DeepCNF_SS_v1.02_release"
#PiPred
export PIPRED="${BIOPROGS}/tools/pipred"
#DeepCoil
export DEEPCOIL="${BIOPROGS}/tools/deepcoil"
#Setup HHrepid
export HHREPIDPATH="${BIOPROGS}/tools/hhrepid"
#Setup HMMER
export HMMERPATH="${BIOPROGS}/tools/hmmer/bin"
export HMMERBINARIES="${BIOPROGS}/tools/hmmer/binaries"
#Setup Mafft
export MAFFT_BINARIES="${BIOPROGS}/tools/mafft2/binaries"
#Setup MARCOIL
export MARCOILMTIDK="${BIOPROGS}/tools/marcoil/R5.MTIDK"
export MARCOILMTK="${BIOPROGS}/tools/marcoil/R5.MTK"
export MARCOILINPUT="${BIOPROGS}/tools/marcoil/Inputs"
#PYTHONPATH FOR PDBX AND MODELLER
export PYTHONPATH="${BIOPROGS}/tools/modeller/modlib/:${BIOPROGS}/dependencies/pdbx"
#Setup PHYLIP (needed by the Perl Script of Phylip)
export PHYLIPBIN="${BIOPROGS}/tools/phylip/current/bin64"
#Reformat version with PHYLIP Support (new reformat.pl does not have this support)
export REFORMAT_PHYLIP="${BIOPROGS}/helpers/reformat_protblast.pl"
#HHLIB
#Rye and its nodes have slightly different architectures.
#hh-suite needs to be compiled separately on them
if [[ $(hostname) = "rye" ]]; then
        export HHLIB="${BIOPROGS}/tools/hh-suite-build-prod"
        export PATH="${BIOPROGS}/tools/hh-suite-build-prod/scripts:${PATH}" #HHSCRIPTS
        export PATH="${BIOPROGS}/tools/hh-suite-build-prod/bin:${PATH}" #HHBINS
else
        export HHLIB="${BIOPROGS}/tools/hh-suite-build"
        export PATH="${BIOPROGS}/tools/hh-suite-build/scripts:${PATH}" #HHSCRIPTS
        export PATH="${BIOPROGS}/tools/hh-suite-build/bin:${PATH}" #HHBINS
fi
#PATH variable
export PATH="${BIOPROGS}/dependencies/anaconda3/bin:${PATH}"
export PATH="${BIOPROGS}/dependencies/Python-3.5.2/bin:${PATH}" #Python binary
export PATH="${BIOPROGS}/pcoils:${PATH}" #PCOILS
export PATH="${BIOPROGS}/dependencies/hh-suite_misc_scripts:${PATH}" # helper scripts from the old Toolkit
export PATH="${BIOPROGS}/dependencies/psipred:${BIOPROGS}/dependencies/psipred/bin:${PATH}" #PSIPRED
export PATH="${BIOPROGS}/tools/mmseqs2/build/bin:${PATH}" #MMSEQS
export PATH="${BIOPROGS}/dependencies/dssp:${PATH}" #DSSP
export PATH="${BIOPROGS}/tools/tcoffee/bin:${PATH}" #T-COFFEE
export PATH="${BIOPROGS}/tools/backtranslate:${PATH}" #Backtranslate
export PATH="${BIOPROGS}/helpers:${PATH}" #Helpers
export PATH="${BIOPROGS}/tools/blastplus/bin:${PATH}" #NCBI-BLAST+
export PATH="${BIOPROGS}/tools/ancescon:${PATH}" #ANCESCON
export PATH="${BIOPROGS}/tools/ali2d:${PATH}" #ALI2D
export PATH="${BIOPROGS}/tools/memsat2:${PATH}" #MEMSAT
export PATH="${BIOPROGS}/tools/clustalo/bin:${PATH}" #CLustalOmega
export PATH="${BIOPROGS}/tools/glprobs:${PATH}" #GLProbs
export PATH="${BIOPROGS}/tools/msaprobs:${PATH}" #MSAProbs
export PATH="${BIOPROGS}/tools/muscle:${PATH}" #MUSCLE
export PATH="${BIOPROGS}/tools/tprpred:${PATH}" #TPRPRED
export PATH="${BIOPROGS}/tools/kalign:${PATH}" #KAlign
export PATH="${BIOPROGS}/tools/mafft2/scripts:${PATH}" #MAFFT
export PATH="${BIOPROGS}/tools/aln2plot:${PATH}"
export PATH="${BIOPROGS}/tools/phylip:${PATH}" #PHYLIP
export PATH="${BIOPROGS}/tools/marcoil:${PATH}" #MARCOIL
export PATH="${BIOPROGS}/tools/modeller/bin:${PATH}" #MODELLER
export PATH="${BIOPROGS}/dependencies/Python-3.5.2/bin:${PATH}"
export PATH="${BIOPROGS}/tools/hmmer/binaries:${PATH}" #HMMER
export PATH="${BIOPROGS}/tools/retrieveseq:${PATH}" #RetrieveSeq
export PATH="${BIOPROGS}/tools/seq2id:${PATH}" #Seq2ID
export PATH="${BIOPROGS}/tools/patternsearch:${PATH}" #PatternSearch
export PATH="${BIOPROGS}/tools/sixframe:${PATH}" #6FrameTranslation
export PATH="${BIOPROGS}/tools/hhrepid/bin:${PATH}" #HHrepid
export PATH="${BIOPROGS}/tools/clans:${PATH}" #CLANS
export PATH="${BIOPROGS}/tools/phyml/PhyML-3.1:${PATH}" #PhyML
export PATH="${BIOPROGS}/tools/samcc:${PATH}"  #SamCC
export PATH="${BIOPROGS}/tools/tmhmm/bin:${PATH}" #TMHMM
export PATH="${BIOPROGS}/tools/Phobius:${PATH}" #PHOBIUS
export PATH="${BIOPROGS}/tools/Phobius/PolyPhobius:${PATH}" #POLYPHOBIUS
export PATH="${BIOPROGS}/tools/iupred:${PATH}" #IUPRED
export PATH="${BIOPROGS}/tools/signalp-4.1:${PATH}" #SIGNALP
export PATH="${BIOPROGS}/tools/blast-2.2.26/bin:${PATH}" #LEGACY BLAST
export PATH="${BIOPROGS}/tools/DISOPRED:${PATH}" #DISOPRED
