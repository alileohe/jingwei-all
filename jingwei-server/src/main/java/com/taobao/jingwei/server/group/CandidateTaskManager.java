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
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @desc 扫描搜有的start op的任务，根据自身容量运行任务
 * @date May 24, 2012 5:11:10 PM
 */

public class CandidateTaskManager implements JingWeiConstants {
    private static Log log = LogFactory.getLog(CandidateTaskManager.class);

    public final BuildinTaskManager buildinTaskManager;

    private final CustomerTaskManager customerTaskManager;

    private final ServerCoreThread serverCoreThread;

    private final Object lock = new Object();

    /**
     * 定时发送running状态 每5秒
     */
    private static final int SCAN_SERVER_FREE_CAPACITY_INTERVAL = 5000;

    /**
     * 如果10次还没有获取执行机会则强只执行
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
     * 调用sh脚本， ps本机进程，检查type=task的进程个数，如果小于server的容量，则启动任务
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

                    // 读取任务实例数量，如果没有达到则启动任务
                    int requiredTaskInstanceCount = TaskUtil.getTaskInstanceCount(configManager,
                            candidateTarget.getTaskName());

                    // 已经加锁的个数，代表运行的task的实例个数
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
                            //当前机器已经运行了，就跳过该机器
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
     * 获取所有的可能运行的任务，group或者非group的任务，start状态
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
     * 获取所有可能要执行的任务，star状态的group和非group任务
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
                // 非组的任务都属于默认组
                CandidateTarget candidateTarget = new CandidateTarget(DEFAULT_GROUP, taskName);
                buildinCandidateTargets.add(candidateTarget);
            }
        }

        buildinCandidateTargets.removeAll(this.getGroupCandidateTargets(configManager, serverName));

        Collections.shuffle(buildinCandidateTargets);

        return buildinCandidateTargets;
    }

    /**
     * 返回所有group、task，满足 op是start的任务
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
