package com.taobao.jingwei.server.group;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.TaskUtil;
import com.taobao.jingwei.common.config.ConfigDataListener;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.server.util.GroupUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * @desc ִ��Ŀ����� e.g. /jingwei/groups/**group/tasks/**task/op ���ߵ�groupNameΪ��ʱ����/jingwei/servers/**server/tasks/**task/op
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 3, 2012 12:58:28 PM
 */
public final class CandidateTarget implements JingWeiConstants {

	private static Log log = LogFactory.getLog(CandidateTarget.class);

	/** ʮ������Ƿ�running�����û��running���˳� */
	private static final long WAIT_RUNNING_TIME = 10000L;

	/** ���� */
	private final String groupName;

	/** ������ */
	private final String taskName;

	private volatile int lockIndex;

	/** ͬ������ */
	private final SynchronousQueue<Boolean> syncQueue = new SynchronousQueue<Boolean>();

	/** /jingwei/tasks/**task/hosts/**host/status */
	private volatile Boolean started = Boolean.FALSE;

	private Object lock = new Object();

	public CandidateTarget(String groupName, String taskName) {
		this.groupName = groupName;
		this.taskName = taskName;
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (!(obj instanceof CandidateTarget)) {
			return false;
		}

		CandidateTarget other = (CandidateTarget) obj;

		if (this.groupName.equals(other.getGroupName()) && this.taskName.equals(other.getTaskName())) {
			return true;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.groupName.hashCode() ^ this.taskName.hashCode();
	}

	public String getGroupName() {
		return groupName;
	}

	public String getTaskName() {
		return taskName;
	}

	/**
	 * ���Դ����ǳ־ýڵ�ķ�ʽ�������ɹ���������У�����ʧ���򷵻�
	 * 
	 * @return
	 */
	private void start(final CandidateTaskManager groupTaskManager) {

		new Thread() {

			public void run() {
				this.setName(getGroupName() + "-" + getTaskName() + "-invoker");

				boolean buildinType = false;
				try {
					buildinType = TaskUtil.isBuildinTaskType(groupTaskManager.getServerCoreThread().getConfigManager(),
							CandidateTarget.this.taskName);
				} catch (Exception e) {
					log.error(e);
					return;
				}

				if (buildinType) {

					groupTaskManager.getBuildinTaskManager().startTask(taskName, String.valueOf(lockIndex), groupName);
					log.warn("[jingwei server] start customer task : " + CandidateTarget.this.getTaskName()
							+ " lock index : " + lockIndex);
				} else {
					groupTaskManager.getCustomerTaskManager().startTask(taskName, String.valueOf(lockIndex), groupName);
					log.warn("[jingwei server] start customer task : " + CandidateTarget.this.getTaskName()
							+ " lock index : " + lockIndex);
				}
			};
		}.start();

	}

	public boolean tryStart(CandidateTaskManager candidateTaskManager) {

		log.warn("[jingwei server] try to start task : " + taskName + ",index=" + lockIndex);
		ConfigManager configManager = candidateTaskManager.getServerCoreThread().getConfigManager();

		String serverName = candidateTaskManager.getServerCoreThread().getServerConfig().getServerName();
		// �����ǳ־ýڵ� /jingwei/groups/**group/tasks/**task/locks/lock

		boolean success = false;
		try {
			success = GroupUtil.creatTaskServerEephemeralLock(configManager, this.taskName, String.valueOf(lockIndex),
					serverName);
		} catch (Exception e) {
			log.error("[jingwei server] create ephemeral lock error : " + this.getGroupName() + "-" + taskName, e);
			return false;
		}

		if (!success) {
			log.warn("[jingwei server] node exist for " + this.getGroupName() + "-" + taskName + "-lock" + lockIndex);
			return false;
		}

		String printMsg = this.getTaskName() + ", " + this.getGroupName();

		// �������status��Ϊrunning�����core�ɹ�����
		ConfigDataListener configDataListener = this.addStatusListener(this, configManager, serverName);

		// ��������
		this.start(candidateTaskManager);

		Boolean hasPublishedRunning = null;

		long waitRunningTime = ((ZkConfigManager) configManager).getZkConfig().getZkSessionTimeoutMs() - 5;

		// ����ɹ�����running��/jingwei/tasks/**task/hosts/**host/status
		try {
			hasPublishedRunning = syncQueue.poll(waitRunningTime, TimeUnit.MILLISECONDS);

		} catch (InterruptedException e) {
			log.error(e);
		} finally {
			log.warn(" afert " + waitRunningTime + " check running : " + hasPublishedRunning + "\t" + printMsg);

			try {
				GroupUtil.deleteTaskServerEphemetalLock(configManager, taskName, String.valueOf(lockIndex));
				log.warn("[jingewi server] delete group task server lcok :"
						+ GroupUtil.getTaskServerEephemeralLockPath(configManager, taskName, String.valueOf(lockIndex)));
			} catch (Exception e) {
				log.error(e);
			}

			this.deleteStatusListener(configDataListener, configManager, serverName);
			log.warn("delete data listener staus " + taskName + ", " + serverName);
		}

		if (hasPublishedRunning != null && hasPublishedRunning) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * /jingwei/tasks/**task/hosts/**host/status
	 * 
	 * @param target ִ�ж���
	 */
	private ConfigDataListener addStatusListener(final CandidateTarget target, ConfigManager configManager,
			String serverName) {
		// path
		String path = getStatusListenerPath(target.getTaskName(), serverName);

		ConfigDataListener configDataListener = new ConfigDataListener() {

			@Override
			public void handleData(String dataIdOrPath, String data) {
				if (StringUtil.isBlank(data)) {
					return;
				}

				if (!started) {
					synchronized (lock) {
						if (!started) {
							started = Boolean.TRUE;
							String printMsg = target.getTaskName() + ", " + target.getGroupName();

							log.warn("listened running status : " + printMsg);

							syncQueue.add(true);
						}
					}
				}
			}
		};

		configManager.addDataListener(path.toString(), configDataListener);

		return configDataListener;
	}

	private void deleteStatusListener(ConfigDataListener configDataListener, ConfigManager configManager,
			String serverName) {
		String path = this.getStatusListenerPath(taskName, serverName);

		configManager.removeDataListener(path, configDataListener);
	}

	/**
	 * /jingwei/tasks/**task/hosts/**host/status
	 * 
	 * @param taskName
	 * @param serverName
	 * @return
	 */
	private String getStatusListenerPath(String taskName, String serverName) {
		StringBuilder path = new StringBuilder(JINGWEI_TASK_ROOT_PATH);
		path.append(ZK_PATH_SEP).append(taskName);
		path.append(ZK_PATH_SEP).append(JINGWEI_TASK_HOST_NODE);
		path.append(ZK_PATH_SEP).append(serverName);
		path.append(ZK_PATH_SEP).append(JINGWEI_STATUS_NODE_NAME);

		return path.toString();
	}

	public int getLockIndex() {
		return lockIndex;
	}

	public void setLockIndex(int lockIndex) {
		this.lockIndex = lockIndex;
	}

}
