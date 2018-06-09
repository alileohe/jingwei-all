package com.taobao.jingwei.common;

import com.alibaba.common.lang.StringUtil;

/**
 * <p/>
 * description:精卫常量部分，子类可以实现该接口，或者直接静态方式使用
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
     * 精卫在ZK存储的根路径
     */
    public final static String JINGWEI_ROOT_PATH = "/jingwei-v2";

    /**
     * 任务在zk的存储根路径
     */
    public final static String JINGWEI_TASK_ROOT_PATH = JINGWEI_ROOT_PATH + "/tasks";

    /**
     * server在zk的存储路径
     */
    public final static String JINGWEI_SERVER_ROOT_PATH = JINGWEI_ROOT_PATH + "/servers";

    /**
     * group在zk的存储路径
     */
    public final static String JINGWEI_GROUP_ROOT_PATH = JINGWEI_ROOT_PATH + "/groups";

    /**
     * server节点下tasks的节点名
     */
    public final static String JINGWEI_SERVER_TASKS_NAME = "tasks";

    /**
     * monitor节点下tasks的节点名
     */
    public final static String JINGWEI_MONITOR_TASKS_NAME = "tasks";

    /**
     * monitor节点下groups的节点名
     */
    public final static String JINGWEI_MONITOR_GROUPS_NAME = "groups";

    /**
     * group节点下tasks的节点名
     */
    public final static String JINGWEI_GROUP_TASKS_NAME = "tasks";

    /**
     * group节点下task的lock节点名
     */
    public final static String JINGWEI_GROUP_TASK_LOCK_NAME = "lock";

    /**
     * server节点下executors的节点名
     */
    public final static String JINGWEI_EXECUTORS_NAME = "executors";

    /**
     * server节点下groups的节点名
     */
    public final static String JINGWEI_GROUPS_NAME = "groups";

    /**
     * monitor在zk的存储路径
     */
    public final static String JINGWEI_MONITOR_ROOT_PATH = JINGWEI_ROOT_PATH + "/monitors";

    /**
     * e.g. /jingwei/monitors/monitors 子节点保存非持久数据，节点存在代表running状态，
     */
    public final static String JINGWEI_MONITOR_MONITORS_NODE_NAME = "monitors";

    /**
     * e.g. /jingwei/monitors/tasks/**task/host
     * 或/jingwei/monitors/groups/**group/host运行该告警配置的主机名
     */
    public final static String JINGWEI_MONITOR_TASK_HOST_NAME = "host";

    /**
     * 精卫节点状态的名称，一般是与JINGWEI_TASK_ROOT_PATH或者JINGWEI_AGENT_ROOT_PATH
     * 组合是用，行程/jingwei/tasks/xxtask/status或者/jingwei/agents/xxagent/status
     */
    public final static String JINGWEI_STATUS_NODE_NAME = "status";

    /**
     * 精卫节点操作名称一般是与JINGWEI_TASK_ROOT_PATH或者JINGWEI_AGENT_ROOT_PATH
     * 组合是用，行程/jingwei
     * /tasks/hosts/xxtask/operate或者/jingwei/agents/xxagent/operate
     */
    public final static String JINGWEI_OPERATE_NODE_NAME = "operate";

    /**
     * 统计数据的节点，与JINGWEI_TASK_ROOT_PATH组合使用，例如/jingwei/tasks/**task/**host/stats
     * 其中**task表示task的名字，**host运行**task的主机，一台机器上只运行一个同名的task
     */
    public final static String JINGWEI_STATS_NODE_NAME = "stats";

    /**
     * /jingwei/tasks/**task/**host/alarm,该节点对应任务运行期间的一些非统计异常数据
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
     * 默认的扫描host的alarm节点的周期
     */
    public final static long DEFAULT_SCAN_ALARM_PERIOD = 5000L;

    /**
     * 默认的扫描host的stats节点的周期
     */
    public final static long DEFAULT_SCAN_STATS_PERIOD = 5000L;

    /**
     * 默认的扫描host的heartBeat节点的周期
     */
    public final static long DEFAULT_SCAN_HEARTBEAT_PERIOD = 15000L;

    /**
     * 阈值告警冻结周期 毫秒单位
     */
    public final static long DEFAULT_THRESHOLD_FROZEN_PERIOD = 300000;

    /**
     * 告警检测冻结周期 毫秒单位
     */
    public final static long DEFAULT_ALARM_FROZEN_PERIOD = 300000;

    /**
     * running和心跳检测冻结周期 毫秒单位
     */
    public final static long DEFAULT_HEARTBEAT_FROZEN_PERIOD = 300000;

    /**
     * 默认的扫描position的节点的周期
     */
    public final static long DEFAULT_SCAN_POSITIN_PERIOD = 15000L;

    /**
     * 位点检测冻结周期 毫秒单位
     */
    public final static long DEFAULT_POSITION_FROZEN_PERIOD = 300000;

    /**
     * 位点检测多长时间没有变化则告警
     */
    public final static long DEFAULT_POSITION_NOT_CHANGE_PERIOD = 1800000;

    /**
     * 位点检测落后多长时间则告警
     */
    public final static long DEFAULT_LANTENCY_ALARM_THRESHOULD = 1800000;

    /**
     * 统计模块 默认向统计计数时间窗口
     */
    public final static long DEFAULT_STATS_PERIOD = 5000L;

    /**
     * 扫描Plugin的周期
     */
    public final static long DEFAULT_SCAN_PLUGIN_PERIOD = 5000L;

    /**
     * ZK节点分隔符
     */
    public final static String ZK_PATH_SEP = "/";

    /**
     * 文件系统分隔符
     */
    public final static String FILE_SEP = System.getProperty("file.separator");

    /**
     * 系统换行符
     */
    public final static String LINE_SEP = System.getProperty("line.separator");

    /**
     * 当前操作系统名称
     */
    public final static String OS_NAME = StringUtil.defaultIfBlank(System.getProperty("os.name"));

    /**
     * 字符串常量 "-"
     */
    public final static String BAR_LINE = "-";

    /**
     * 字符串常量 "_"
     */
    public final static String LOW_BAR_LINE = "_";

    /**
     * 空格字符串常量 " "
     */
    public final static String SPACE_SEP = " ";

    /**
     * 逗号常量 ","
     */
    public final static String COMMA_STR = ",";

    /**
     * 冒号常量 ":"
     */
    public final static String COLON_STR = ":";

    /**
     * 井号常量 "#"
     */
    public final static String POUND_STR = "#";

    /**
     * 点号常量 "."
     */
    public final static String POINT_STR = ".";

    /**
     * 美元符号常量
     */
    public final static String DOLLAR_STR = "$";
    /**
     * ^符号常量
     */
    public final static String TIP_STR = "^";

    /**
     * ;符号常量
     */
    public final static String SEMICOLON_STR = ";";

    /**
     * @符号常量
     */
    public final static String AT_STR = "@";

    /**
     * 默认的日期格式
     */
    public final static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 当前用户名
     */
    public final static String USER_NAME = System.getProperty("user.name", "admin");

    /**
     * 新增的group不能使用这个token，可以认为非组任务都属于default group
     */
    public final static String DEFAULT_GROUP = "DEFAULT_GROUP";

    /**
     * 如果没有配置java opt，则使用默认的 opt
     */
    public final static String DEFAULT_JAVA_OPT = "DEFAULT_JAVA_OPT";

    /**
     * 一天的时间秒
     */
    public final static long DAY_TIME_SECONDS = 86400L;

    public final static int SECOND_PER_HOUR = 3600;

    /**
     * venus prop的KEY
     */
    public final static String DB_SYNC_IP_KEY = "replicator.global.master";
    public final static String DB_SYNC_PORT_KEY = "replicator.global.db.port";
    public final static String SWITCH_POLICY_KEY = "replicator.plugin.directRelay.switchPolicy";

    /**
     * 故障重启延时
     */
    public final static int RESTART_DELAY_TIME = 15000;
}
