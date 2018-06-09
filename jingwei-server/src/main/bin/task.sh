#!/bin/bash

### ====================================================================== ###
##                                                                        	##
##  JingWei Task Startup Script												##
##                                                                          ##
### ====================================================================== ###
### 2011-05-19 by qihao

MAIN_CLASS=$1
TASK_NAME=$2
TASK_TYPE=$3
SERVER_NAME=$4
LOCK_INDEX=$5
GROUP_NAME=$6
WORK_PATH=$7
#get param from 8th to end
JAVA_OPT=${@:8}

usage() {
    echo "please set startup Args String"
    exit 1;
}

if [ $# -lt 3 ]; then
    usage
fi

if [ `whoami` == "root" ]; then
    echo DO NOT use root user to launch me.
    exit 1;
fi

cd `dirname $0`/..
BASE_DIR="`pwd`"
CONF_DIR=${BASE_DIR}/conf

export JAVA_HOME=`cat ${BASE_DIR}/bin/javaHome`
export PATH=${JAVA_HOME}/bin:$PATH
export LD_LIBRARY_PATH=/opt/taobao/oracle/lib:${BASE_DIR}/lib:${LD_LIBRARY_PATH}
export NLS_LANG=AMERICAN_AMERICA.ZHS16GBK
export LANG=zh_CN.GB18030

JAVA_OPTS="-Djingwei.type=task -Djingwei.task.name=${TASK_NAME}"

if [ ! ${GROUP_NAME} == "DEFAULT_GROUP" ]; then
    JAVA_OPTS="${JAVA_OPTS} -Djingwei.group=${GROUP_NAME}"
else
    GROUP_NAME=""
fi

if [ ${JAVA_OPT} == "DEFAULT_JAVA_OPT" ]; then
    JAVA_OPTS="${JAVA_OPTS} -server -Xms256m -Xmx256m -XX:PermSize=64m -XX:MaxPermSize=64m -Xss1m"
    #JAVA_OPTS="${JAVA_OPTS} -XX:NewSize=128m -XX:MaxNewSize=128m"
    JAVA_OPTS="${JAVA_OPTS} -XX:+UseParallelGC"
    JAVA_OPTS="${JAVA_OPTS} -XX:ParallelGCThreads=4"
else
    JAVA_OPTS="${JAVA_OPTS} ${JAVA_OPT}"
fi

JAVA_OPTS="${JAVA_OPTS} -Djava.net.preferIPv4Stack=true"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintGCDetails"
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintGCDateStamps"
JAVA_OPTS="${JAVA_OPTS} -XX:+DisableExplicitGC"
JAVA_OPTS="${JAVA_OPTS} -Xloggc:${BASE_DIR}/logs/gc-${TASK_NAME}.log"
#JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=8181,server=y,suspend=y"

if [ ! -f "${JAVA_HOME}/bin/java" ]; then
    echo "please set JAVA_HOME"
    exit 1;
fi

#如果是定制任务,添加classpath
if [ ${TASK_TYPE} == "CUSTOMER" ]; then
    CONF_DIR=${BASE_DIR}/plugin/work/${WORK_PATH}/conf
    cd ${BASE_DIR}/plugin/work/${WORK_PATH}
    for jar in `ls ${BASE_DIR}/plugin/work/${WORK_PATH}/lib/*.jar`
    do
        CLASSPATH="${CLASSPATH}:""${jar}"
    done

#如果是内置任务，添加classpath
else
    CONF_DIR=${BASE_DIR}/conf
    for jar in `ls ${BASE_DIR}/lib/*.jar`
    do
        CLASSPATH="${CLASSPATH}:""${jar}"
    done
fi

LOG_PATH=${BASE_DIR}/logs
START_LOG=${LOG_PATH}/${TASK_NAME}_start.log

echo "=========================================================================" >> ${START_LOG}
echo "" >> ${START_LOG}
echo "  JingWei Task Startup Environment" >> ${START_LOG}
echo "" >> ${START_LOG}
echo "  BASE_DIR: ${BASE_DIR}" >> ${START_LOG}
echo "" >> ${START_LOG}
echo "  JAVA_HOME: ${JAVA_HOME}" >> ${START_LOG}
echo "" >> ${START_LOG}
echo "  JAVA_VERSION: `${JAVA_HOME}/bin/java -version`" >> ${START_LOG}
echo "" >> ${START_LOG}
echo "  JAVA_OPTS: ${JAVA_OPTS}" >> ${START_LOG}
echo "" >> ${START_LOG}
echo "  CLASS_PATH: ${CLASSPATH}" >> ${START_LOG}
echo "" >> ${START_LOG}
echo "=========================================================================" >> ${START_LOG}
echo "" >> ${START_LOG}

#init logPath and logFile
if [ ! -d "${LOG_PATH}" ]; then
    mkdir ${LOG_PATH}
fi
if [ ! -f "${START_LOG}" ]; then
    touch ${START_LOG}
fi

#Start Java Process
${JAVA_HOME}/bin/java ${JAVA_OPTS} -classpath ${CONF_DIR}:${CLASSPATH}:. com.taobao.jingwei.server.group.CandidateTaskCoreLoader "mainClass=${MAIN_CLASS},taskName=${TASK_NAME},serverName=${SERVER_NAME},groupName=${GROUP_NAME},confPath=${BASE_DIR}/conf/server.ini,lockIndex=${LOCK_INDEX}" >> ${START_LOG} 2 >&1 &