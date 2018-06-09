#!/bin/bash
### ====================================================================== ###
##                                                                         ##
##  JingWei kill java  Script						   ##
##                                                                         ##
### ====================================================================== ###
### 2011-05-19 by qihao
PROG_NAME=$0
TYPE=$1

if [ `whoami` == "root" ]; then
  echo DO NOT use root user to launch me.
  exit 1;
fi

usage() {
    echo "Usage: ${PROG_NAME} {all|server|task}"
    exit 1;
}

if [ $# -lt 1 ]; then
    usage
fi

cd `dirname $0`/..
BASE_DIR="`pwd`"

end(){
	TASK_PIDS=(`ps aux|grep -v "grep"|grep "jingwei.type=${TYPE}"|awk '{print $2}'`)
	TASK_COUNT=${#TASK_PIDS[@]}

	if [ ${TASK_COUNT} -gt 0 ] ;then
		for TASK_PID in ${TASK_PIDS[*]} ;do
			echo "kill ${TYPE} PID: ${TASK_PID}"
			kill  ${TASK_PID}
		done
	fi
}

case "${TYPE}" in
    all)
        TYPE="server"
		end
        TYPE="task"
		end
        ;;
    server)
        TYPE="server"
        end
        ;;
    task)
        TYPE="task"
        end
        ;;
    *)
        usage
        ;;
esac