package com.taobao.jingwei.monitor.core;

import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.config.ChildChangeListener;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.monitor.alert.AlertMsgManager;
import com.taobao.jingwei.monitor.conf.MonitorConfig;
import com.taobao.jingwei.monitor.lb.ConsisHashStrategy;
import com.taobao.jingwei.monitor.listener.MonitorChildListener;
import com.taobao.jingwei.monitor.listener.MonitorTaskScheduler;
import com.taobao.jingwei.monitor.util.GroupUtil;
import com.taobao.jingwei.monitor.util.MonitorConst;
import com.taobao.jingwei.monitor.util.MonitorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
* ɨ�����������ʱ״̬�����õļ�ز����Ƚϣ�������������WW��SM�澯
* @author shuohailhl
*
*/
public class MonitorCoreThread implements Runnable {
	private Logger log = LoggerFactory.getLogger(MonitorCoreThread.class);

	/** ZK��diamond���ù����� */
	private final ConfigManager configManager;

	/** �澯������ */
	private final AlertMsgManager alertMsgManager;

	/** ��������� */
	private final MonitorConfig monitorConfig;

	/** monitor�� */
	private final String monitorName;

	/** ������ */
	private final Object lock = new Object();

	/** key : taskName, value : MonitorTaskScheduler */
	private final Map<String, MonitorTaskScheduler> monitorTasks = new ConcurrentHashMap<String, MonitorTaskScheduler>();

	/** key : group name, value �� �������� : ���ɨ���� */
	private final Map<String, HashMap<String, MonitorTaskScheduler>> orignMonitorGroups = new ConcurrentHashMap<String, HashMap<String, MonitorTaskScheduler>>();

	/** ɨ��������ڵ� /jingwei/monitors/tasks/**task */
	private final ScheduledExecutorService taskScannerScheduler = Executors.newScheduledThreadPool(1,
			new TaskScannerThreadFactory());

	/** ���tickʱ�䣨3�룩������л������񣬵�����ص㣬���ֵ����ص��ɨ�����ڣ���ɨ��zk�ڵ㣬���û�е���ɨ�����ڣ��򽫼�ص��Ӧ�ļ�ʱ����ȥtick��3�룩 */
	private final ScheduledExecutorService scanForCheckScheduler = Executors.newScheduledThreadPool(1,
			new ScanForCheckerThreadfactory());

	/** ��ȡһ�������Ӧ�Ļ���״̬��ɨ�����������ɨ���  */
	private final ExecutorService executorService = Executors.newFixedThreadPool(32, new ScanForOneTaskThreadFactory());

	/** ɨ���� */
	private final MonitorTaskScanner monitorTaskScanner;

	/** �ֲ����� */
	/** �������ߣ�monitor�ĸ������仯��ʱ�����´������� */
	private volatile ConsisHashStrategy consisHashStrategy;

	private final MonitorChildListener monitorChildListener;

	public MonitorCoreThread(ConfigManager configManager, AlertMsgManager alertMsgManager, MonitorConfig monitorConfig) {
		this.configManager = configManager;
		this.alertMsgManager = alertMsgManager;
		this.monitorConfig = monitorConfig;
		this.monitorName = this.monitorConfig.getMonitorName();
		this.monitorTaskScanner = new MonitorTaskScanner(this);
		this.monitorChildListener = new MonitorChildListener(this);
	}

	@Override
	public void run() {
		//monitor������ڵ��Ƿ��г�ʼ��
		try {
			JingWeiUtil.initJingWeiRootPath(this.configManager);
		} catch (Exception e) {
			log.error("init jingWei root path error:", e);
			this.destoryAndExit();
		}
		String path = MonitorUtil.getMonitorMonitorsPath();
		this.configManager.addChildChangesListener(path, this.monitorChildListener);

		//��zkдrunning״̬, e.g /jingwei/monitors/monitors/**monitor
		this.publishSelf();

		// e.g /jingwei/monitors/monitors/**monitor,�ڵ���ʧ�����·���
		this.addRepublishListener();

		// ��ʱɨ�����õı仯
		this.taskScannerScheduler.scheduleAtFixedRate(monitorTaskScanner, (long) 10, (long) 10, TimeUnit.SECONDS);

		// ɨ��ڵ��ж��Ƿ�澯
		ScanForCheck scanForCheck = new ScanForCheck();
		this.scanForCheckScheduler.scheduleAtFixedRate(scanForCheck, 20000L, MonitorConst.DEFAULT_TICK_TIME,
				TimeUnit.MILLISECONDS);
		log.warn("[jingwei monitor] " + this.monitorName + " has succcessful started!");
	}

	/**
	 * ��zkдrunning״̬, e.g /jingwei/monitors/monitors/**monitor
	 */
	public void publishSelf() {

		List<String> tasks = new ArrayList<String>();
		tasks.addAll(monitorTasks.keySet());
		tasks.addAll(orignMonitorGroups.keySet());

		try {
			MonitorUtil.publishMonitorAppendTask(configManager, tasks, monitorName);
		} catch (Exception e) {
			log.error("addTask To Monitor error:", e);
			this.destoryAndExit();
		}

		log.warn("publish monitor data : " + Arrays.deepToString(tasks.toArray()) + ", monitor : " + monitorName);
	}

	/**
	 * ������ָ÷ǳ־ýڵ���ʧ�������·���, e.g /jingwei/monitors/monitors/**monitor
	 */
	private void addRepublishListener() {
		final String path = MonitorUtil.getMonitorSelfPath(monitorName);

		this.configManager.addChildChangesListener(path, new ChildChangeListener() {

			@Override
			public void handleChild(String parentPath, List<String> currentChilds) {
				if (currentChilds == null || currentChilds.isEmpty()) {
					log.warn("listener zk slef disapper : " + path);
					MonitorCoreThread.this.publishSelf();
				}
			}
		});
	}

	/**
	 * ���group���͵�ɨ����
	 * @param groupName groupName����taskName
	 * @param jsonStr e.g. /jingwei/monitors/tasks/**task�Ľڵ�����
	 */
	public void addGroupMonitor(String groupName, String jsonStr) {
		synchronized (this.lock) {

			if (consisHashStrategy == null) {
				return;
			}

			// һ���Լ��
			String consumerId = consisHashStrategy.findConsumerByPartition(groupName);

			if (!consumerId.equals(this.monitorName)) {
				if (log.isDebugEnabled()) {
					log.warn(" hash result not for this group  : " + groupName);
				}

				return;
			}

			log.warn("[jwm] detected add group to mem : " + groupName);
			if (!this.orignMonitorGroups.containsKey(groupName)) {
				HashMap<String, MonitorTaskScheduler> groupTasks = new HashMap<String, MonitorTaskScheduler>();
				this.orignMonitorGroups.put(groupName, groupTasks);
			}

			Set<String> tasks = GroupUtil.getGroupTasksFromZk(this.configManager, groupName);

			for (String taskName : tasks) {
				this.addGroupMonitorTask(groupName, taskName, jsonStr);
			}

			// �޸�����
			this.publishSelf();
		}
	}

	/**
	 * Ϊgroup���͵�������������и����������ɨ����
	 * @param groupName
	 * @param taskName
	 * @param jsonStr
	 */
	public void addGroupMonitorTask(String groupName, String taskName, String jsonStr) {
		synchronized (this.lock) {

			String monitorName = monitorConfig.getMonitorName();
			MonitorTaskScheduler monitorScheduler = new MonitorTaskScheduler(alertMsgManager, configManager,
					monitorName, taskName);

			// ���ַ�group����������
			monitorScheduler.setGroupName(groupName);

			// �޸�MonitorTaskNode
			monitorScheduler.handleData(null, jsonStr);

			this.orignMonitorGroups.get(groupName).put(taskName, monitorScheduler);
		}
	}

	/**
	* ���һ���������
	*
	* @param taskName
	*/
	public void addMonitorScheduler(String taskName, String jsonStr) {
		synchronized (this.lock) {

			if (consisHashStrategy == null) {
				return;
			}

			// һ���Լ��
			String consumerId = consisHashStrategy.findConsumerByPartition(taskName);

			if (!consumerId.equals(this.monitorName)) {
				if (log.isDebugEnabled()) {
					log.debug(" hash result not for this task : " + taskName);
				}
				return;
			}

			// �������������棬��ֹ��ͻ
			if (this.getMonitorTasks().containsKey(taskName)) {
				return;
			}

			MonitorTaskScheduler monitorScheduler = new MonitorTaskScheduler(alertMsgManager, configManager,
					monitorName, taskName);

			// �޸�MonitorTaskNode
			monitorScheduler.handleData(null, jsonStr);

			monitorTasks.put(taskName, monitorScheduler);
			log.warn("[jwm] cache zk data change listener for task : " + taskName);

			// �޸�����
			this.publishSelf();
		}
	}

	/**
	* 1 ֹͣɨ���߳� 2 ɾ����Ӧ�Ļ���
	*
	* @param taskName
	*/
	public void deleteMonitorScheduler(String taskName) {
		synchronized (this.lock) {
			MonitorTaskScheduler scheduler = monitorTasks.get(taskName);
			if (null != scheduler) {
				// ɾ�������ɨ����
				this.monitorTasks.remove(taskName);

				log.warn("[jwm] remove mem data change listener for task : " + taskName);

				// �޸�����
				this.publishSelf();
			}
		}
	}

	/**
	 * ɾ�����е�ɨ������
	 */
	public void deleteAllMonitorTasks() {
		synchronized (lock) {
			for (String taskName : this.monitorTasks.keySet()) {
				this.deleteMonitorScheduler(taskName);
			}

			for (String groupName : this.orignMonitorGroups.keySet()) {
				this.deleteGroup(groupName);
			}
		}
	}

	/**
	 * ɾ����ֹͣgroup���͵ļ������
	 * @param groupName
	 */
	public void deleteGroup(String groupName) {
		synchronized (lock) {

			HashMap<String, MonitorTaskScheduler> groupSchedulers = this.getOrignMonitorGroups().get(groupName);

			if (null != groupSchedulers) {
				this.getOrignMonitorGroups().remove(groupName);
			}

			// �޸�����
			this.publishSelf();
		}
	}

	/**
	 * ɾ���������ڵ������ɨ����
	 * @param groupName
	 * @param taskName
	 */
	public void deleteGroupMonitorTask(String groupName, String taskName) {
		synchronized (this.lock) {
			HashMap<String, MonitorTaskScheduler> groupSchedulers = this.getOrignMonitorGroups().get(groupName);

			if (groupSchedulers != null) {
				groupSchedulers.remove(taskName);
				log.warn("[jingwei monitor] remove mem data change listener for task : " + taskName);

				if (groupSchedulers.isEmpty()) {
					this.getOrignMonitorGroups().remove(groupName);

				}
			}
		}
	}

	/**
	* �˳�ϵͳ���ر�ZK Client
	*/
	public void destoryAndExit() {
		JingWeiUtil.destroyZkAndExit(configManager, -1);
	}

	/**
	 * ��ȡ���е�ǰ��ɨ������
	 * @return
	 */
	public Set<String> getAllMonitorSchedulerTasks() {
		Set<String> set = new HashSet<String>();
		set.addAll(this.monitorTasks.keySet());
		set.addAll(this.orignMonitorGroups.keySet());

		return set;
	}

	/**
	 * ����ɢ�з�������
	 * @param currentChilds
	 */
	public void balance(List<String> currentChilds) {
		synchronized (this.getLock()) {
			// �������ߣ�monitor�ĸ������仯��ʱ�����´�������
			this.consisHashStrategy = new ConsisHashStrategy(currentChilds);

			Set<String> currentMonitorTasks = this.getMonitorTasks().keySet();
			Set<String> currentGroupMonitorTasks = this.getOrignMonitorGroups().keySet();

			for (String taskName : currentMonitorTasks) {
				String consumerId = consisHashStrategy.findConsumerByPartition(taskName);

				if (!consumerId.equals(this.monitorName)) {
					log.warn("task move from this monitor : " + taskName);
					this.deleteMonitorScheduler(taskName);
				}
			}

			for (String groupName : currentGroupMonitorTasks) {
				String consumerId = consisHashStrategy.findConsumerByPartition(groupName);

				if (!consumerId.equals(this.monitorName)) {
					log.warn(" group task move from this monitor : " + groupName);
					this.deleteGroup(groupName);
				}
			}
		}
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public AlertMsgManager getAlertMsgManager() {
		return alertMsgManager;
	}

	public Map<String, MonitorTaskScheduler> getMonitorTasks() {
		return monitorTasks;
	}

	public Object getLock() {
		return lock;
	}

	public MonitorConfig getMonitorConfig() {
		return monitorConfig;
	}

	public Map<String, HashMap<String, MonitorTaskScheduler>> getOrignMonitorGroups() {
		return orignMonitorGroups;
	}

	public String getMonitorName() {
		return monitorName;
	}

	private static class TaskScannerThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "monitor-task-scanner");
		}

	}

	private static class ScanForCheckerThreadfactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "scan-for-check-by-ticker");
		}
	}

	private static class ScanForOneTaskThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "scan-for-one-task");
		}
	}

	private class ScanForCheck extends Thread {
		public ScanForCheck() {
			this.setName("scan-for-check");
		}

		@Override
		public void run() {
			if (log.isInfoEnabled()) {
				log.info("[jw monitor] tranverse all tasks for check.");
			}

			synchronized (MonitorCoreThread.this.getLock()) {
				Collection<MonitorTaskScheduler> schedulers = monitorTasks.values();

				for (MonitorTaskScheduler scheduler : schedulers) {
					executorService.submit(new DefaultScanWorkerChain(scheduler));
				}

				Set<String> groups = orignMonitorGroups.keySet();

				for (String group : groups) {
					Map<String, MonitorTaskScheduler> map = orignMonitorGroups.get(group);
					Set<Map.Entry<String, MonitorTaskScheduler>> tasks = map.entrySet();
					for (Map.Entry<String, MonitorTaskScheduler> task : tasks) {
						executorService.submit(new DefaultScanWorkerChain(task.getValue()));
					}
				}
			}
		}
	}
}