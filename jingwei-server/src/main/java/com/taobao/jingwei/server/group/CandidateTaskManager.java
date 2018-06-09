package com.taobao.jingwei.server.group;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.TaskUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.server.core.BuildinTaskManager;
import com.taobao.jingwei.server.core.CustomerTaskManager;
import com.taobao.jingwei.server.core.ServerCoreThread;
import com.taobao.jingwei.server.util.GroupUtil;
import com.taobao.util.RandomUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @desc ɨ�����е�start op�����񣬸�������������������
 * @date May 24, 2012 5:11:10 PM
 */

public class CandidateTaskManager implements JingWeiConstants {
    private static Log log = LogFactory.getLog(CandidateTaskManager.class);

    public final BuildinTaskManager buildinTaskManager;

    private final CustomerTaskManager customerTaskManager;

    private final ServerCoreThread serverCoreThread;

    private final Object lock = new Object();

    /**
     * ��ʱ����running״̬ ÿ5��
     */
    private static final int SCAN_SERVER_FREE_CAPACITY_INTERVAL = 5000;

    /**
     * ���10�λ�û�л�ȡִ�л�����ǿִֻ��
     */
    private static final int MAX_TRY_COUNT = 8;

    private int tryCount = 0;

    public CandidateTaskManager(ServerCoreThread serverCoreThread) {
        this.serverCoreThread = serverCoreThread;
        this.buildinTaskManager = serverCoreThread.getBuildinTaskManager();
        this.customerTaskManager = serverCoreThread.getCustomerTaskManager();
    }

    public void init() {
        Thread runningTaskInstaceScanner = new Thread(new RunningTaskInstaceScanner());
        runningTaskInstaceScanner.start();
    }

    /**
     * ����sh�ű��� ps�������̣����type=task�Ľ��̸��������С��server������������������
     *
     * @author shuohailhl
     */
    class RunningTaskInstaceScanner implements Runnable {

        @Override
        public void run() {
            while (true) {

                CandidateTaskManager.this.tryStart();
                try {
                    Thread.sleep(SCAN_SERVER_FREE_CAPACITY_INTERVAL);
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
        }
    }

    private void tryStart() {
        synchronized (lock) {
            int configCapacity = this.getServerCoreThread().getServerConfig().getExecutorCapacity();

            int usedCapacityCount = JingWeiUtil.getJingWeiTaskCount();

            int freeCapacity = configCapacity - usedCapacityCount;

            log.warn("[jingwei server] server config capacity : " + configCapacity + " used : " + usedCapacityCount);
            if (freeCapacity > 0) {
                CandidateTaskManager.this.tryStartTask(freeCapacity, configCapacity, usedCapacityCount);
            }
        }
    }

    private void tryStartTask(int freeCapacity, int configCapacity, int usedCapacityCount) {
        ConfigManager configManager = this.serverCoreThread.getConfigManager();
        String serverName = this.getServerCoreThread().getServerConfig().getServerName();

        List<CandidateTarget> candidateTargets = this.getAllCandidateTarget(configManager, serverName);

        int used = 0;
        for (CandidateTarget candidateTarget : candidateTargets) {

            if (this.openLock(configCapacity, usedCapacityCount, used)) {
                if (used < freeCapacity) {
                    String taskName = candidateTarget.getTaskName();

                    // ��ȡ����ʵ�����������û�дﵽ����������
                    int requiredTaskInstanceCount = TaskUtil.getTaskInstanceCount(configManager,
                            candidateTarget.getTaskName());

                    // �Ѿ������ĸ������������е�task��ʵ������
                    Map<String, String> lockTasks = TaskUtil.getTaskLocksCount(configManager, taskName);
                    log.warn("[jingwei server]" + candidateTarget.getTaskName() + " required  : "
                            + requiredTaskInstanceCount + ", aleady running : " + lockTasks.size());
                    if (requiredTaskInstanceCount == lockTasks.size()) {
                        continue;
                    }

                    Set<String> waitLockNames = GroupUtil.waitLockNames(requiredTaskInstanceCount, lockTasks.keySet());

                    if (lockTasks.values().contains(serverName)) {
                        log.warn("[jingwei server] server TaskLock ignore Task: "
                                + candidateTarget.getTaskName());
                        continue;
                    }
                    for (String lockName : waitLockNames) {
                        List<String> runningTasks = JingWeiUtil.getJingWeiTaskList();
                        log.warn("[jingwei server]  already running Task process List ===" + runningTasks.toString());
                        if (runningTasks.contains(taskName)) {
                            log.warn("[jingwei server] server running process ignore Task:" + taskName);
                            //��ǰ�����Ѿ������ˣ��������û���
                            break;
                        }
                        candidateTarget.setLockIndex(GroupUtil.getIndexFromLockName(lockName));
                        if (candidateTarget.tryStart(this)) {
                            used++;
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean openLock(int configCapacity, int usedCapacityCount, int used) {
        if ((RandomUtil.getInt(0, configCapacity) + 1) > (used + usedCapacityCount)) {
            this.tryCount = 0;
            return true;
        } else {
            if (this.tryCount == MAX_TRY_COUNT) {
                this.tryCount = 0;
                return true;
            } else {
                this.tryCount++;
                return false;
            }
        }
    }

    // jingwei/groups/**group/tasks/**task/locks/lock1
    public BuildinTaskManager getBuildinTaskManager() {
        return buildinTaskManager;
    }

    public CustomerTaskManager getCustomerTaskManager() {
        return customerTaskManager;
    }

    public ServerCoreThread getServerCoreThread() {
        return serverCoreThread;
    }

    /**
     * ��ȡ���еĿ������е�����group���߷�group������start״̬
     *
     * @param configManager
     * @param serverName
     * @return
     */
    private List<CandidateTarget> getAllCandidateTarget(ConfigManager configManager, String serverName) {
        List<CandidateTarget> candidateTargets = new ArrayList<CandidateTarget>();

        candidateTargets.addAll(this.getGroupCandidateTargets(configManager, serverName));
        candidateTargets.addAll(this.getNoGroupCandidateTargets(configManager, serverName));

        return candidateTargets;
    }

    /**
     * ��ȡ���п���Ҫִ�е�����star״̬��group�ͷ�group����
     *
     * @return
     */
    private List<CandidateTarget> getNoGroupCandidateTargets(ConfigManager configManager, String serverName) {
        List<CandidateTarget> buildinCandidateTargets = new ArrayList<CandidateTarget>();

        Set<String> all = new HashSet<String>();

        Set<String> buildinTasks = buildinTaskManager.getTasks();
        Set<String> customerTasks = customerTaskManager.getTasks();

        all.addAll(customerTasks);
        all.addAll(buildinTasks);

        for (String taskName : all) {

            boolean isStartOp = TaskUtil.isServerTaskStartOp(configManager, serverName, taskName);
            if (isStartOp) {
                // �������������Ĭ����
                CandidateTarget candidateTarget = new CandidateTarget(DEFAULT_GROUP, taskName);
                buildinCandidateTargets.add(candidateTarget);
            }
        }

        buildinCandidateTargets.removeAll(this.getGroupCandidateTargets(configManager, serverName));

        Collections.shuffle(buildinCandidateTargets);

        return buildinCandidateTargets;
    }

    /**
     * ��������group��task������ op��start������
     *
     * @return
     */
    private List<CandidateTarget> getGroupCandidateTargets(ConfigManager configManager, String serverName) {

        List<CandidateTarget> candidateTargets = GroupUtil.getCandidateTargets(configManager, serverName);

        Collections.shuffle(candidateTargets);

        return candidateTargets;
    }

    public Object getLock() {
        return lock;
    }

}
