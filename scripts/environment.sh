#!/bin/bash
# BIOPROGS
export BIOPROGS="/ebio/abt1_share/toolkit_support1/code/bioprogs"
# HHBINS
export PATH="${BIOPROGS}/tools/hh-suite-build/bin:${PATH}"
# HHSCRIPTS
export PATH="${BIOPROGS}/tools/hh-suite-build/scripts:${PATH}"
# HHLIB
export HHLIB=/ebio/abt1_share/toolkit_support1/code/bioprogs/tools/hh-suite-build

#DBS
export scopdir=/ebio/abt1_share/toolkit_sync/databases/hh-suite/scope/unpack/
export pdbdir=/ebio/abt1_share/toolkit_sync/databases/hhpred/new_dbs/pdb70_23Feb17/
export mmcifdir=/ebio/abt1_share/toolkit_sync/databases/hh-suite/mmcif70/unpack