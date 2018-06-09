#!/bin/sh
URL=$1
SAVE_PATH=$2
#wget http://10.13.43.86/jingwei/uploads/tars/DAILY-UNION-CPS-XO.tar.gz -T 3 -t 3 -P /home/admin/jingwei-server/plugin/target
wget ${URL} -P ${SAVE_PATH}
exit