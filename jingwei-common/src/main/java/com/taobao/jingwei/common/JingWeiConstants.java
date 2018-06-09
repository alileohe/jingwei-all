package com.taobao.jingwei.common;

import com.alibaba.common.lang.StringUtil;

/**
 * <p/>
 * description:�����������֣��������ʵ�ָýӿڣ�����ֱ�Ӿ�̬��ʽʹ��
 * <p/>
 *
 * @author <a href="mailto:qihao@taobao.com">qihao</a>
 * @version 1.0
 * @{# JingWeiConstants.java Create on Aug 23, 2011 10:13:32 AM
 * <p/>
 * Copyright (c) 2011 by qihao.
 */
public interface JingWeiConstants {
    /**
     * ������ZK�洢�ĸ�·��
     */
    public final static String JINGWEI_ROOT_PATH = "/jingwei-v2";

    /**
     * ������zk�Ĵ洢��·��
     */
    public final static String JINGWEI_TASK_ROOT_PATH = JINGWEI_ROOT_PATH + "/tasks";

    /**
     * server��zk�Ĵ洢·��
     */
    public final static String JINGWEI_SERVER_ROOT_PATH = JINGWEI_ROOT_PATH + "/servers";

    /**
     * group��zk�Ĵ洢·��
     */
    public final static String JINGWEI_GROUP_ROOT_PATH = JINGWEI_ROOT_PATH + "/groups";

    /**
     * server�ڵ���tasks�Ľڵ���
     */
    public final static String JINGWEI_SERVER_TASKS_NAME = "tasks";

    /**
     * monitor�ڵ���tasks�Ľڵ���
     */
    public final static String JINGWEI_MONITOR_TASKS_NAME = "tasks";

    /**
     * monitor�ڵ���groups�Ľڵ���
     */
    public final static String JINGWEI_MONITOR_GROUPS_NAME = "groups";

    /**
     * group�ڵ���tasks�Ľڵ���
     */
    public final static String JINGWEI_GROUP_TASKS_NAME = "tasks";

    /**
     * group�ڵ���task��lock�ڵ���
     */
    public final static String JINGWEI_GROUP_TASK_LOCK_NAME = "lock";

    /**
     * server�ڵ���executors�Ľڵ���
     */
    public final static String JINGWEI_EXECUTORS_NAME = "executors";

    /**
     * server�ڵ���groups�Ľڵ���
     */
    public final static String JINGWEI_GROUPS_NAME = "groups";

    /**
     * monitor��zk�Ĵ洢·��
     */
    public final static String JINGWEI_MONITOR_ROOT_PATH = JINGWEI_ROOT_PATH + "/monitors";

    /**
     * e.g. /jingwei/monitors/monitors �ӽڵ㱣��ǳ־����ݣ��ڵ���ڴ���running״̬��
     */
    public final static String JINGWEI_MONITOR_MONITORS_NODE_NAME = "monitors";

    /**
     * e.g. /jingwei/monitors/tasks/**task/host
     * ��/jingwei/monitors/groups/**group/host���иø澯���õ�������
     */
    public final static String JINGWEI_MONITOR_TASK_HOST_NAME = "host";

    /**
     * �����ڵ�״̬�����ƣ�һ������JINGWEI_TASK_ROOT_PATH����JINGWEI_AGENT_ROOT_PATH
     * ������ã��г�/jingwei/tasks/xxtask/status����/jingwei/agents/xxagent/status
     */
    public final static String JINGWEI_STATUS_NODE_NAME = "status";

    /**
     * �����ڵ��������һ������JINGWEI_TASK_ROOT_PATH����JINGWEI_AGENT_ROOT_PATH
     * ������ã��г�/jingwei
     * /tasks/hosts/xxtask/operate����/jingwei/agents/xxagent/operate
     */
    public final static String JINGWEI_OPERATE_NODE_NAME = "operate";

    /**
     * ͳ�����ݵĽڵ㣬��JINGWEI_TASK_ROOT_PATH���ʹ�ã�����/jingwei/tasks/**task/**host/stats
     * ����**task��ʾtask�����֣�**host����**task��������һ̨������ֻ����һ��ͬ����task
     */
    public final static String JINGWEI_STATS_NODE_NAME = "stats";

    /**
     * /jingwei/tasks/**task/**host/alarm,�ýڵ��Ӧ���������ڼ��һЩ��ͳ���쳣����
     */
    public final static String JINGWEI_SCAN_ALARM_NODE = "alarm";

    /**
     * /jingwei/tasks/**task/hosts
     */
    public final static String JINGWEI_TASK_HOST_NODE = "hosts";

    /**
     * /jingwei/tasks/**task/locks
     */
    public final static String JINGWEI_TASK_LOCKS_NODE_NAME = "locks";

    /**
     * /jingwei/groups/**group/tasks/**task/s-locks
     */
    public final static String JINGWEI_GROUP_TASK_SERVER_LOCKS_NODE_NAME = "s-locks";

    /**
     * /jingwei/tasks/**task/t-locks
     */
    public final static String JINGWEI_INSTANCE_TASK_LOCKS_NODE_NAME = "t-locks";

    /**
     * /jingwei/tasks/**task/locks/lock
     */
    public final static String JINGWEI_INSTANCE_TASK_LOCK_NODE_PREFIX = "lock";

    /**
     * /jingwei/groups/**group/tasks/**task/s-locks/lock
     */
    public final static String JINGWEI_GROUP_TASK_SERVER_LOCK_NODE_PREFIX = "lock";

    /**
     * /jingwei/tasks/**task/lastComit
     */
    public final static String JINGWEI_TASK_POSITION_NODE_NAME = "lastComit";

    /**
     * /jingwei/tasks/**task/hosts/xxxhost/heartBeat
     */
    public final static String JINGWEI_HOST_HEART_BEAT_NODE_NAME = "heartBeat";

    /**
     * Ĭ�ϵ�ɨ��host��alarm�ڵ������
     */
    public final static long DEFAULT_SCAN_ALARM_PERIOD = 5000L;

    /**
     * Ĭ�ϵ�ɨ��host��stats�ڵ������
     */
    public final static long DEFAULT_SCAN_STATS_PERIOD = 5000L;

    /**
     * Ĭ�ϵ�ɨ��host��heartBeat�ڵ������
     */
    public final static long DEFAULT_SCAN_HEARTBEAT_PERIOD = 15000L;

    /**
     * ��ֵ�澯�������� ���뵥λ
     */
    public final static long DEFAULT_THRESHOLD_FROZEN_PERIOD = 300000;

    /**
     * �澯��ⶳ������ ���뵥λ
     */
    public final static long DEFAULT_ALARM_FROZEN_PERIOD = 300000;

    /**
     * running��������ⶳ������ ���뵥λ
     */
    public final static long DEFAULT_HEARTBEAT_FROZEN_PERIOD = 300000;

    /**
     * Ĭ�ϵ�ɨ��position�Ľڵ������
     */
    public final static long DEFAULT_SCAN_POSITIN_PERIOD = 15000L;

    /**
     * λ���ⶳ������ ���뵥λ
     */
    public final static long DEFAULT_POSITION_FROZEN_PERIOD = 300000;

    /**
     * λ����೤ʱ��û�б仯��澯
     */
    public final static long DEFAULT_POSITION_NOT_CHANGE_PERIOD = 1800000;

    /**
     * λ�������೤ʱ����澯
     */
    public final static long DEFAULT_LANTENCY_ALARM_THRESHOULD = 1800000;

    /**
     * ͳ��ģ�� Ĭ����ͳ�Ƽ���ʱ�䴰��
     */
    public final static long DEFAULT_STATS_PERIOD = 5000L;

    /**
     * ɨ��Plugin������
     */
    public final static long DEFAULT_SCAN_PLUGIN_PERIOD = 5000L;

    /**
     * ZK�ڵ�ָ���
     */
    public final static String ZK_PATH_SEP = "/";

    /**
     * �ļ�ϵͳ�ָ���
     */
    public final static String FILE_SEP = System.getProperty("file.separator");

    /**
     * ϵͳ���з�
     */
    public final static String LINE_SEP = System.getProperty("line.separator");

    /**
     * ��ǰ����ϵͳ����
     */
    public final static String OS_NAME = StringUtil.defaultIfBlank(System.getProperty("os.name"));

    /**
     * �ַ������� "-"
     */
    public final static String BAR_LINE = "-";

    /**
     * �ַ������� "_"
     */
    public final static String LOW_BAR_LINE = "_";

    /**
     * �ո��ַ������� " "
     */
    public final static String SPACE_SEP = " ";

    /**
     * ���ų��� ","
     */
    public final static String COMMA_STR = ",";

    /**
     * ð�ų��� ":"
     */
    public final static String COLON_STR = ":";

    /**
     * ���ų��� "#"
     */
    public final static String POUND_STR = "#";

    /**
     * ��ų��� "."
     */
    public final static String POINT_STR = ".";

    /**
     * ��Ԫ���ų���
     */
    public final static String DOLLAR_STR = "$";
    /**
     * ^���ų���
     */
    public final static String TIP_STR = "^";

    /**
     * ;���ų���
     */
    public final static String SEMICOLON_STR = ";";

    /**
     * @���ų���
     */
    public final static String AT_STR = "@";

    /**
     * Ĭ�ϵ����ڸ�ʽ
     */
    public final static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * ��ǰ�û���
     */
    public final static String USER_NAME = System.getProperty("user.name", "admin");

    /**
     * ������group����ʹ�����token��������Ϊ������������default group
     */
    public final static String DEFAULT_GROUP = "DEFAULT_GROUP";

    /**
     * ���û������java opt����ʹ��Ĭ�ϵ� opt
     */
    public final static String DEFAULT_JAVA_OPT = "DEFAULT_JAVA_OPT";

    /**
     * һ���ʱ����
     */
    public final static long DAY_TIME_SECONDS = 86400L;

    public final static int SECOND_PER_HOUR = 3600;

    /**
     * venus prop��KEY
     */
    public final static String DB_SYNC_IP_KEY = "replicator.global.master";
    public final static String DB_SYNC_PORT_KEY = "replicator.global.db.port";
    public final static String SWITCH_POLICY_KEY = "replicator.plugin.directRelay.switchPolicy";

    /**
     * ����������ʱ
     */
    public final static int RESTART_DELAY_TIME = 15000;
}
