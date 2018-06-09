package com.taobao.jingwei.core.kernel;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.TaskUtil;
import com.taobao.jingwei.common.log.LogType;
import com.taobao.jingwei.common.node.HeartbeatNode;
import com.taobao.jingwei.common.node.PositionNode;
import com.taobao.jingwei.common.node.StatusNode.StatusEnum;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.common.stats.TransactionStats;
import com.taobao.tddl.dbsync.DbsyncException;
import com.taobao.tddl.dbsync.monitor.MonitorImpl;
import com.taobao.tddl.dbsync.pipeline.Pipeline;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * <p/>
 * description:
 * <p/>
 * �������ļ����
 *
 * @author <a href="mailto:qihao@taobao.com">qihao</a>
 * @version 1.0
 * @{# JingWeiMonitor.java Create on Dec 8, 2011 1:23:13 PM
 * <p/>
 * Copyright (c) 2011 by qihao.
 */
public class JingWeiHook extends MonitorImpl implements JingWeiConstants {

    private static final Log logger = LogFactory.getLog(JingWeiHook.class);

    private Log positionLogger;

    private AbstractJingWeiCore jingWeiCore;

    private final PositionNode positionNode = new PositionNode();

    private final HeartbeatNode heartbeatNode = new HeartbeatNode();

    /**
     * ��ʱ�ύλ��
     */
    private final ScheduledExecutorService positionCommitScheduler = Executors
            .newSingleThreadScheduledExecutor(new CommitSchedulerThreadFactory());

    /**
     * ��ʱ�ύλ��
     */
    private PositionCommitter positionCommitter;

    /**
     * ÿ��Сʱ�ύһ��λ��
     */
    private final int COMMIT_PISITION_INTERVAL = SECOND_PER_HOUR / 2;

    /**
     * @param jingWeiCore
     */
    public JingWeiHook(AbstractJingWeiCore jingWeiCore) {
        this.jingWeiCore = jingWeiCore;
    }

    /*
     * ����TX�������ݻص�
     *
     * @see
     * com.taobao.tddl.dbsync.monitor.MonitorImpl#updateSummary(java.lang.String
     * , com.taobao.tddl.dbsync.monitor.MonitorImpl.Summary)
     */
    protected void updateSummary(String name, Summary summary) {
        super.updateSummary(name, summary);
        TransactionStats transactionStats = jingWeiCore.statsUnit.getTxStats();
        transactionStats.setTxCount(summary.getTx());
        transactionStats.setTxTps(summary.getTxPs());
        transactionStats.setMillisMaxLatency(summary.getMaxLatency());
        transactionStats.setMillisMinLatency(summary.getMinLatency());
        transactionStats.setMillisAvgLatency(summary.getLatency());
    }

    /*
     * ��ʱ��������cmonitLog
     *
     * @see
     * com.taobao.tddl.dbsync.monitor.MonitorImpl#updatePosition(java.lang.String
     * , java.lang.String, java.sql.Timestamp)
     */
    protected void updatePosition(String name, String position, Timestamp t) {
        super.updatePosition(name, position, t);
        if (StringUtil.isNotBlank(position)) {
            positionNode.setName(name);
            positionNode.setPosition(position);
            ExtractorType extractorType = this.jingWeiCore.syncTaskNode.getExtractorType();
            //use position second Time
            Long millis = JingWeiUtil.getPositionMillis(position, extractorType);
            String time = StringUtil.EMPTY_STRING;
            if (millis > 0) {
                Date pTime = new Date(millis);
                time = JingWeiUtil.date2String(pTime);
                positionNode.setTimestamp(pTime);
            }
            //logger commit Position
            StringBuffer positionBuffer = new StringBuffer("[comitPosition]").append(JingWeiUtil.date2String(new Date()));
            positionBuffer.append(" name: ").append(positionNode.getName()).append(" position: ").append(position);
            positionBuffer.append(" Timestamp: ").append(time);
            positionLogger.warn(positionBuffer.toString());
            //update zk commit Position
            try {
                // д��zk
                jingWeiCore.configManager.publishOrUpdateData(positionNode.getDataIdOrNodePath(),
                        positionNode.toJSONString(), positionNode.isPersistent());
            } catch (Exception e) {
                // ������θ���λ��ʧ�ܣ����¼��־���ڴ��´θ��µ�����
                // ���һֱʧ�ܵĻ��������Բ��ñ���LOG�м�¼��λ����лָ�
                logger.error("Update Position Error!", e);
            }
        } else {
            logger.warn("updatePosition Empty position! ");
        }
    }

    /*
     * ��ʼ�������ϴ�λ��
     *
     * @see
     * com.taobao.tddl.dbsync.monitor.MonitorImpl#loadPosition(java.lang.String)
     */
    protected String loadPosition(String name) throws DbsyncException {
        // ���޻�ȡϵͳ����
        String position = super.loadPosition(name);
        String ownPath = this.jingWeiCore.syncTaskNode.getDataIdOrNodePath();
        positionNode.setOwnerDataIdOrPath(ownPath);
        positionNode.setName(name);
        if (null == position && this.jingWeiCore.syncTaskNode.isUseLastPosition()) {
            // �����ȡ����ϵͳ���ԣ���ȥZK����
            String positionStr = this.jingWeiCore.configManager.getData(positionNode.getDataIdOrNodePath());
            if (StringUtil.isNotBlank(positionStr)) {
                try {
                    positionNode.jsonStringToNodeSelf(positionStr);
                    return positionNode.getPosition();
                } catch (JSONException e) {
                    throw new DbsyncException("LoadPosition Error!", e);
                }
            }
        }
        return position;
    }

    /*
     * �����ɹ��ص�
     *
     * @see
     * com.taobao.tddl.dbsync.monitor.MonitorImpl#handleLaunch(java.lang.String,
     * com.taobao.tddl.dbsync.pipeline.Pipeline)
     */
    public void handleLaunch(String name, Pipeline pipeline) throws DbsyncException {
        super.handleLaunch(name, pipeline);
        String taskName = jingWeiCore.syncTaskNode.getName();
        this.positionLogger = com.taobao.jingwei.common.log.LogFactory.getLog(LogType.COMMIT, taskName);
        // ����״̬Ϊrunning
        this.jingWeiCore.changeTaskStatus(StatusEnum.RUNNING);
        String serverName = StringUtil.isNotBlank(jingWeiCore.getServerName()) ? jingWeiCore.getServerName()
                : AbstractJingWeiCore.HOST_NAME;
        logger.warn("[New Master Running Succeed] HostName: " + serverName + " TaskName: " + taskName);

        this.positionCommitter = new PositionCommitter(taskName);
        // ������ʱ��
        int secondOfDate = JingWeiUtil.getSecondOfDate();

        int delay = COMMIT_PISITION_INTERVAL - secondOfDate % (COMMIT_PISITION_INTERVAL);

        this.positionCommitScheduler.scheduleWithFixedDelay(positionCommitter, delay, COMMIT_PISITION_INTERVAL,
                TimeUnit.SECONDS);
    }

    public void handleHeartbeat(String name, Timestamp t) {
        super.handleHeartbeat(name, t);
        // ƴװ��������
        heartbeatNode.setOwnerDataIdOrPath(this.jingWeiCore.getTaskHostRootPath());
        heartbeatNode.setName(name);
        heartbeatNode.setTimestamp(t);
        try {
            // ����ZK������
            this.jingWeiCore.configManager.publishOrUpdateData(heartbeatNode.getDataIdOrNodePath(),
                    heartbeatNode.toJSONString(), heartbeatNode.isPersistent());
        } catch (Exception e) {
            // ��������ʧ�ܣ���¼��־�������������ֹͣ���º�ᱻmonitor��֪���ұ���
            // ��������ֻ��¼��־
            logger.error("Update Heartbeat Error!", e);
        }
    }

    public synchronized void handleException(String name, String msg, Exception ex) {
        logger.error("[monitor-exception] " + name + ": message = " + msg + ", error = " + ex.getMessage()
                + ", class = " + ex.getClass().getName(), ex);
        this.jingWeiCore.sendToAlarm(name + "-" + msg, ex);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.taobao.tddl.dbsync.pipeline.PipelineStatusListener#handleShutdown(String)
     */
    public void handleShutdown(String name) {
        super.handleShutdown(name);
        logger.warn("jingwei-hook handle shutdown " + name);
        try {
            if (this.jingWeiCore.syncTaskNode.isSingleTask()) {
                this.jingWeiCore.removeCurrentTaskLocks();
                this.jingWeiCore.changeTaskStatus(StatusEnum.STANDBY);
                /*
                 * labe:A
				 * 
				 * ����ǰ�ȴ��£���ֹDIAMOND����������ȶ�ȡ�����ļ�
				 * dbsync�������쵼�����������������ã��Ӷ�ʼ���޷�
				 * ��ȡ�����µ����á�
				 */
                Thread.sleep(JingWeiConstants.RESTART_DELAY_TIME);
                // ����lock,��dbsync�����쳣�Լ��رյ�����Ҫ����Lock��Ȼ��ԶSTANDBY
                this.jingWeiCore.registerLocks(Boolean.FALSE);
            } else {
                /*
                 *����Ƿǵ�������,����ȴ֮������dbsync
				 */
                Thread.sleep(JingWeiConstants.RESTART_DELAY_TIME);
                logger.warn("jingwei-hook is going to start dbsync " + name);
                this.jingWeiCore.startDbsync();
            }
        } catch (Exception e) {
            logger.error("jingWeiHook shutdown registerLocks To StandBy Error", e);
        }
    }

    protected void updateGrouping(String name, int[] enqueues) {
        if (logger.isWarnEnabled()) {
            StringBuilder builder = new StringBuilder("[monitor-grouping] ");
            builder.append(jingWeiCore.getTaskName());
            for (int group = 0; group < enqueues.length; group++) {
                builder.append((group > 0) ? ", " : ": ");
                builder.append(group).append(':');
                builder.append(enqueues[group]);
            }
            logger.warn(builder.toString());
        }
    }

    private static class CommitSchedulerThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "commit-position-scehduler");
        }
    }

    /**
     * ÿ���2Сʱ��ʱ����λ�㵽zk
     *
     * @author shuohailhl
     */
    private class PositionCommitter implements Runnable {
        private final String taskName;

        public PositionCommitter(String taskName) {
            this.taskName = taskName;
        }

        @Override
        public void run() {
            PositionNode positionNode = JingWeiHook.this.positionNode;

            if (null == positionNode.getPosition()) {
                positionLogger.warn("opsition node timestamp or position is null!");
                return;
            }
            int index = JingWeiUtil.getSecondOfDate() / COMMIT_PISITION_INTERVAL;
            String path = TaskUtil.getSpecTaskComitNodePath(this.taskName, index);
            try {
                // д��zk
                jingWeiCore.configManager.publishOrUpdateData(path, positionNode.toJSONString(),
                        positionNode.isPersistent());
                Date date = positionNode.getTimestamp();
                String time = JingWeiUtil.date2String(date);
                StringBuffer sb = new StringBuffer("[comit specified schecule position]");
                sb.append(" name: ").append(positionNode.getName()).append(" position: ")
                        .append(positionNode.getPosition());
                sb.append(" Timestamp: ").append(time);

                logger.warn(sb.toString());
            } catch (Exception e) {
                logger.error("Update Position Error " + index, e);
            }
        }
    }
}