#!/bin/sh
# sh runbatch_direct.sh batch_params.xml
#
# Run in batch mode

#if [ "$#" -eq "0" ]; then
#	echo "Please specify batch parameters file."
#	return 1
#elif [ ! -f "$1" ]; then
#	echo "Parameters file '$1' does not exist."
#	return 2
#fi
# Replace bellow with the location of eclipse plugins directory
CP_BASE="/opt/eclipse/plugins"
CP_PARAMS="/home/local/VANDERBILT/zhangh24/code-release/solarPanelAdoption/batch/"
CP_PROJ="/home/local/VANDERBILT/zhangh24/code-release/solarPanelAdoption/bin"

# Replace bellow with the location of the .rs directory (usually within model directory)
MODEL_RSDIR="/home/local/VANDERBILT/zhangh24/code-release/solarPanelAdoption/solarPanelAdoption.rs"

# Replace bellow with the location of your .class files
MODEL_BIN="/home/local/VANDERBILT/zhangh24/code-release/solarPanelAdoption/solarPanelAdoption.rs/bin"

RSVERSION="2.1.0"
CP_RS_VMATH="${CP_BASE}/libs.ext_2.1.0/lib/*"
#CP_RS_VMATH="${CP_BASE}/libs.ext_2.1.0/lib/vecmath.jar"
CP_RS_BATCH_BIN="${CP_BASE}/repast.simphony.batch_${RSVERSION}/bin"
CP_RS_BATCH_LIB="${CP_BASE}/repast.simphony.batch_${RSVERSION}/lib/*" 
CP_RS_RUNTIME_BIN="${CP_BASE}/repast.simphony.runtime_${RSVERSION}/bin" 
CP_RS_RUNTIME_LIB="${CP_BASE}/repast.simphony.runtime_${RSVERSION}/lib/*"
CP_RS_CORE_LIB="${CP_BASE}/repast.simphony.core_${RSVERSION}/lib/*"
CP_RS_CORE_BIN="${CP_BASE}/repast.simphony.core_${RSVERSION}/bin"
CP_RS_BIN="${CP_BASE}/repast.simphony.bin_and_src_${RSVERSION}/*"
CP_RS_DATA_LIB="${CP_BASE}/repast.simphony.data_${RSVERSION}/lib/*"
CP_RS_CORE_BIN="${CP_BASE}/repast.simphony.core_${RSVERSION}/bin" 
CP_RS_SCEN_BIN="${CP_BASE}/repast.simphony.scenario_${RSVERSION}/bin" 
CP_RS_DATLD_BIN="${CP_BASE}/repast.simphony.dataLoader_${RSVERSION}/bin" 
CP_RS_GROOVY_LIB="${CP_BASE}/org.codehaus.groovy_1.7.10.xx-20120301-1300-e36-RELEASE/lib/*" 
CP_RS_SQL_LIB="${CP_BASE}/repast.simphony.sql_${RSVERSION}/lib/*"

#RUN batch
java -Xss100M -Xmx4000M -cp ${CP_PROJ}:${CP_RS_VMATH}:${CP_RS_GROOVY_LIB}:${CP_RS_DATLD_BIN}:${CP_RS_SCEN_BIN}:${CP_RS_BATCH_BIN}:${CP_RS_BATCH_LIB}:${CP_RS_RUNTIME_LIB}:${CP_RS_RUNTIME_BIN}:${CP_RS_CORE_LIB}:${CP_RS_CORE_BIN}:${CP_RS_DATA_LIB}:${CP_RS_DATA_BIN}:${CP_RS_BIN}:${MODEL_BIN}:${CP_RS_SQL_LIB} repast.simphony.runtime.RepastBatchMain -params ${CP_PARAMS}/$1 ${MODEL_RSDIR}

#CP_REPAST="${CP_BASE}/*"
#java -Xss100M -Xmx4000M -Djava.library.path=${CP_RED} -cp ${CP_PROJ}:${CP_RE}:${CP_REPAST}:${MODEL_BIN} repast.simphony.runtime.RepastBatchMain -params ${CP_PARAMS}/$1 ${MODEL_RSDIR}

#rc=$?
#if [[ $rc == 0 ]] ; then
#    exit $rc
#fi



