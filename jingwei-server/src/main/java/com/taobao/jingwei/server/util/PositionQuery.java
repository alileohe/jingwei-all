package com.taobao.jingwei.server.util;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.node.PositionNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.server.node.GroupNode;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: <a href="mailto:qihao@taobao.com">qihao</a>
 * Date: 13-7-24
 * Time: 23:38
 * version 1.0
 */
public class PositionQuery implements JingWeiConstants {

    public static final String ZK_HOST_KEY = "zkHost";

    public static final String GROUP_NAMES_KEY = "groupName";

    public static final String TASK_NAMES_KEY = "taskName";

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String HZ_TIME_ZONE = "GMT+8";

    public static final String US_TIME_ZONE = "PST";

    private static ThreadLocal<Map<String, SimpleDateFormat>> THREAD_LOCAL_FORMATS = new ThreadLocal<Map<String, SimpleDateFormat>>();

    public static void main(String[] args) throws JSONException {
        if (args.length <= 0) {
            System.out.println(" please set Parameter!");
        }
        initThreadLocal();
        Map<String, String> argMap = JingWeiUtil.handleArgs(args[0]);
        //init zk Manager
        ZkConfigManager zkConfigManager = new ZkConfigManager();
        String zkHost = argMap.get(ZK_HOST_KEY);
        if (StringUtil.isNotBlank(argMap.get(ZK_HOST_KEY))) {
            ZkConfig zkConfig = new ZkConfig(zkHost);
            zkConfigManager.setZkConfig(zkConfig);
        }
        zkConfigManager.init();

        List<String> groupNameList = null;
        //get group TaskNames
        String groupName = argMap.get(GROUP_NAMES_KEY);
        if (StringUtil.isNotBlank(groupName)) {
            String groupTakPath = GroupNode.getDataIdOrNodePath(groupName.trim()) + FILE_SEP + JINGWEI_GROUP_TASKS_NAME;
            Set<String> groupTaskSet = zkConfigManager.getChildDatas(groupTakPath, null).keySet();
            if (groupTaskSet.isEmpty()) {
                System.out.println("groupName: " + groupName + " tasks is Empty!");
                Runtime.getRuntime().halt(0);
            }
            if (!groupTaskSet.isEmpty()) {
                groupNameList = new ArrayList<String>(groupTaskSet.size());
                groupNameList.addAll(groupTaskSet);
            }
        } else {
            //get TaskName
            String taskName = argMap.get(TASK_NAMES_KEY);
            if (StringUtil.isNotBlank(taskName)) {
                groupNameList = new ArrayList<String>(1);
                groupNameList.add(taskName.trim());
            } else {
                System.out.println(" please set groupName=xxx or taskName=xxx");
                Runtime.getRuntime().halt(0);
            }
        }
        int i = 1;
        System.out.println("====================================================================================================================");
        for (String taskName : groupNameList) {
            String positionStr = StringUtil.EMPTY_STRING;
            String hzTimeStr;
            String usTimeStr;
            //get positionNode Data
            SyncTaskNode taskNode = new SyncTaskNode();
            taskNode.setName(taskName);
            PositionNode positionNode = new PositionNode();
            positionNode.setOwnerDataIdOrPath(taskNode.getDataIdOrNodePath());
            String positionPath = positionNode.getDataIdOrNodePath();
            String positionData = zkConfigManager.getData(positionPath);
            if (StringUtil.isNotBlank(positionData)) {
                positionNode.jsonStringToNodeSelf(positionData);
                positionStr = positionNode.getPosition();
            }
            Date time = PositionQuery.getPositionTime(positionStr);
            hzTimeStr = PositionQuery.formatDateByZone(time, HZ_TIME_ZONE);
            usTimeStr = PositionQuery.formatDateByZone(time, US_TIME_ZONE);
            System.out.println(i + "    " + taskName + "    " + positionStr + "     " + hzTimeStr + "     " + usTimeStr);
            i++;
        }
        Runtime.getRuntime().halt(0);
    }

    private static void initThreadLocal() {
        Map<String, SimpleDateFormat> formatMap = new HashMap<String, SimpleDateFormat>();
        THREAD_LOCAL_FORMATS.set(formatMap);
    }

    public static Date getPositionTime(String positionStr) {
        Date time = null;

        Long millis = JingWeiUtil.getPositionMillis(positionStr);

        if (millis > 0) {
            time = new Date(millis);
        }
        return time;
    }

    private static String formatDateByZone(Date date, String timeZone) throws JSONException {
        String timeStr = StringUtil.EMPTY_STRING;
        if (null != date) {
            Map<String, SimpleDateFormat> formatMap = THREAD_LOCAL_FORMATS.get();
            SimpleDateFormat sdf = formatMap.get(timeZone);
            if (null == sdf) {
                sdf = new SimpleDateFormat(DATE_FORMAT);
                sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
                formatMap.put(timeZone, sdf);
            }
            timeStr = sdf.format(date);
        }
        return timeStr;
    }
}