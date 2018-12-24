#!/bin/bash
############# IMPORTANT ###############
# do not add blank lines to this file #
# might throw errors because unix/dos #
#######################################
#Databases
export STANDARD="${DATABASES}/standard"
#HHomp database
export HHOMPDBPATH="${DATABASES}/hhomp/db"
#JAVA
##########################################################
export JAVA_HOME="${BIOPROGSROOT}/dependencies/jdk1.8.0"
export JRE_HOME="${BIOPROGSROOT}/dependencies/jdk1.8.0/jre"
#PERL
##########################################################
export PERL5LIB="${BIOPROGSROOT}/lib"
#COILS/PCOILS
export COILSDIR="${BIOPROGSROOT}/pcoils"
#ALI2D
export ALI2DPATH="${BIOPROGSROOT}/tools/ali2d"
#HHpred
export HHPRED_CONFIG="${BIOPROGSROOT}/env/hhpred.config"
#CLANS
export CLANSPATH="${BIOPROGSROOT}/tools/clans"
#REPPER
export REPPERDIR="${BIOPROGSROOT}/tools/repper"
#BACKTRANSLATOR
export BACKTRANSLATORPATH="${BIOPROGSROOT}/tools/backtranslate"
#SamCC
export SAMCCPATH="${BIOPROGSROOT}/tools/samcc"
#HHomp
export HHOMPPATH="${BIOPROGSROOT}/tools/hhomp"
#PolyPhobius
export POLYPHOBIUS="${BIOPROGSROOT}/tools/Phobius/PolyPhobius"
#SPOT-D and SPIDER2
export SPOTD="${BIOPROGSROOT}/tools/SPOT-disorder_local/misc"
#IUPred
export IUPred_PATH="${BIOPROGSROOT}/tools/iupred"
#PSSPRED
export PSSPRED="${BIOPROGSROOT}/tools/PSSpred_v3"
#DeepCNF_SS
export DEEPCNF="${BIOPROGSROOT}/tools/DeepCNF_SS_v1.02_release"
#PiPred
export PIPRED="${BIOPROGSROOT}/tools/pipred"
#DeepCoil
export DEEPCOIL="${BIOPROGSROOT}/tools/deepcoil"
#Setup HHrepid
export HHREPIDPATH="${BIOPROGSROOT}/tools/hhrepid"
#Setup HMMER
export HMMERPATH="${BIOPROGSROOT}/tools/hmmer/bin"
export HMMERBINARIES="${BIOPROGSROOT}/tools/hmmer/binaries"
#Setup Mafft
export MAFFT_BINARIES="${BIOPROGSROOT}/tools/mafft2/binaries"
#Setup MARCOIL
export MARCOILMTIDK="${BIOPROGSROOT}/tools/marcoil/R5.MTIDK"
export MARCOILMTK="${BIOPROGSROOT}/tools/marcoil/R5.MTK"
export MARCOILINPUT="${BIOPROGSROOT}/tools/marcoil/Inputs"
#PYTHONPATH FOR PDBX AND MODELLER
export PYTHONPATH="${BIOPROGSROOT}/tools/modeller/modlib/:${BIOPROGSROOT}/dependencies/pdbx"
#Setup PHYLIP (needed by the Perl Script of Phylip)
export PHYLIPBIN="${BIOPROGSROOT}/tools/phylip/current/bin64"
#Reformat version with PHYLIP Support (new reformat.pl does not have this support)
export REFORMAT_PHYLIP="${BIOPROGSROOT}/helpers/reformat_protblast.pl"

#HHLIB
#Rye and its nodes have slightly different architectures.
#hh-suite needs to be compiled separately on them
#TODO: get rid of environment dependency
if [[ $(hostname) = "rye" ]]; then
        export HHLIB="${BIOPROGSROOT}/tools/hh-suite-build-prod"
        export PATH="${BIOPROGSROOT}/tools/hh-suite-build-prod/scripts:${PATH}" #HHSCRIPTS
        export PATH="${BIOPROGSROOT}/tools/hh-suite-build-prod/bin:${PATH}" #HHBINS
else
        export HHLIB="${BIOPROGSROOT}/tools/hh-suite-build"
        export PATH="${BIOPROGSROOT}/tools/hh-suite-build/scripts:${PATH}" #HHSCRIPTS
        export PATH="${BIOPROGSROOT}/tools/hh-suite-build/bin:${PATH}" #HHBINS
fi

#PATH variable
export PATH="${BIOPROGSROOT}/dependencies/anaconda3/bin:${PATH}"
export PATH="${BIOPROGSROOT}/dependencies/Python-3.5.2/bin:${PATH}" #Python binary
export PATH="${BIOPROGSROOT}/pcoils:${PATH}" #PCOILS
export PATH="${BIOPROGSROOT}/dependencies/hh-suite_misc_scripts:${PATH}" # helper scripts from the old Toolkit
export PATH="${BIOPROGSROOT}/dependencies/psipred:${BIOPROGSROOT}/dependencies/psipred/bin:${PATH}" #PSIPRED
export PATH="${BIOPROGSROOT}/tools/mmseqs2/build/bin:${PATH}" #MMSEQS
export PATH="${BIOPROGSROOT}/dependencies/dssp:${PATH}" #DSSP
export PATH="${BIOPROGSROOT}/tools/tcoffee/bin:${PATH}" #T-COFFEE
export PATH="${BIOPROGSROOT}/tools/backtranslate:${PATH}" #Backtranslate
export PATH="${BIOPROGSROOT}/helpers:${PATH}" #Helpers
export PATH="${BIOPROGSROOT}/tools/blastplus/bin:${PATH}" #NCBI-BLAST+
export PATH="${BIOPROGSROOT}/tools/ancescon:${PATH}" #ANCESCON
export PATH="${BIOPROGSROOT}/tools/ali2d:${PATH}" #ALI2D
export PATH="${BIOPROGSROOT}/tools/memsat2:${PATH}" #MEMSAT
export PATH="${BIOPROGSROOT}/tools/clustalo/bin:${PATH}" #CLustalOmega
export PATH="${BIOPROGSROOT}/tools/glprobs:${PATH}" #GLProbs
export PATH="${BIOPROGSROOT}/tools/msaprobs:${PATH}" #MSAProbs
export PATH="${BIOPROGSROOT}/tools/muscle:${PATH}" #MUSCLE
export PATH="${BIOPROGSROOT}/tools/tprpred:${PATH}" #TPRPRED
export PATH="${BIOPROGSROOT}/tools/kalign:${PATH}" #KAlign
export PATH="${BIOPROGSROOT}/tools/mafft2/scripts:${PATH}" #MAFFT
export PATH="${BIOPROGSROOT}/tools/aln2plot:${PATH}"
export PATH="${BIOPROGSROOT}/tools/phylip:${PATH}" #PHYLIP
export PATH="${BIOPROGSROOT}/tools/marcoil:${PATH}" #MARCOIL
export PATH="${BIOPROGSROOT}/tools/modeller/bin:${PATH}" #MODELLER
export PATH="${BIOPROGSROOT}/dependencies/Python-3.5.2/bin:${PATH}"
export PATH="${BIOPROGSROOT}/tools/hmmer/binaries:${PATH}" #HMMER
export PATH="${BIOPROGSROOT}/tools/retrieveseq:${PATH}" #RetrieveSeq
export PATH="${BIOPROGSROOT}/tools/seq2id:${PATH}" #Seq2ID
export PATH="${BIOPROGSROOT}/tools/patternsearch:${PATH}" #PatternSearch
export PATH="${BIOPROGSROOT}/tools/sixframe:${PATH}" #6FrameTranslation
export PATH="${BIOPROGSROOT}/tools/hhrepid/bin:${PATH}" #HHrepid
export PATH="${BIOPROGSROOT}/tools/clans:${PATH}" #CLANS
export PATH="${BIOPROGSROOT}/tools/phyml/PhyML-3.1:${PATH}" #PhyML
export PATH="${BIOPROGSROOT}/tools/samcc:${PATH}"  #SamCC
export PATH="${BIOPROGSROOT}/tools/tmhmm/bin:${PATH}" #TMHMM
export PATH="${BIOPROGSROOT}/tools/Phobius:${PATH}" #PHOBIUS
export PATH="${BIOPROGSROOT}/tools/Phobius/PolyPhobius:${PATH}" #POLYPHOBIUS
export PATH="${BIOPROGSROOT}/tools/iupred:${PATH}" #IUPRED
export PATH="${BIOPROGSROOT}/tools/signalp-4.1:${PATH}" #SIGNALP
export PATH="${BIOPROGSROOT}/tools/blast-2.2.26/bin:${PATH}" #LEGACY BLAST
export PATH="${BIOPROGSROOT}/tools/DISOPRED:${PATH}" #DISOPRED
