#!/bin/sh
PROG_NAME=$0
ZK_HOST=$1

if [ ! ${ZK_HOST} ]; then
    ZK_HOST="172.24.113.126:2181,10.246.134.93:2181,172.23.171.3:2181"
fi

cd `dirname $0`/..
BASE_DIR="`pwd`"
CONF_DIR=${BASE_DIR}/conf
export JAVA_HOME=`cat ${BASE_DIR}/bin/javaHome`
export PATH=${JAVA_HOME}/bin:$PATH
export LD_LIBRARY_PATH=${BASE_DIR}/lib:${LD_LIBRARY_PATH}
export LANG=zh_CN.GB18030

JAVA_OPTS="-Djingwei.type=server"
JAVA_OPTS="${JAVA_OPTS} -Djava.net.preferIPv4Stack=true"
JAVA_OPTS="${JAVA_OPTS} -server -Xms96m -Xmx96m -XX:PermSize=32m -XX:MaxPermSize=32m -Xss1m"
JAVA_OPTS="${JAVA_OPTS} -XX:+UseParallelGC"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintGCDetails"
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintGCDateStamps"
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintHeapAtGC"
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintClassHistogram"
JAVA_OPTS="${JAVA_OPTS} -Xloggc:${BASE_DIR}/logs/gc-server.log"
#JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y"

if [ ! -f "${JAVA_HOME}/bin/java" ]; then
    echo "please set JAVA_HOME"
    exit 1;
fi

for jar in `ls ${BASE_DIR}/lib/*.jar`
do
    CLASSPATH="${CLASSPATH}:""${jar}"
done

echo "  JAVA_VERSION: `${JAVA_HOME}/bin/java -version`"
echo ""
echo "  JAVA_OPTS: ${JAVA_OPTS}"
echo ""
echo "  CLASS_PATH: ${CLASSPATH}"
echo ""

#Start Java Process
${JAVA_HOME}/bin/java ${JAVA_OPTS}  -classpath ${CONF_DIR}:${CLASSPATH}:. com.taobao.jingwei.server.util.UnKnowHostClean ${ZK_HOST}