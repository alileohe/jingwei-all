package com.taobao.jingwei.server.group;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.config.SessionStateListener;
import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.timer.HashedWheelTimer;
import com.taobao.jingwei.common.timer.Timeout;
import com.taobao.jingwei.common.timer.TimerTask;
import com.taobao.jingwei.server.listener.TaskInstanceLockListener;
import com.taobao.jingwei.server.util.GroupUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @desc ʵ���䱸�����߼��ĸ���core
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 6, 2012 10:24:26 AM
 */

public class CandidateTaskCoreLoader implements JingWeiConstants {
	private static Log log = LogFactory.getLog(CandidateTaskCoreLoader.class);

	private TaskInstanceLockListener groupTaskLockListener;

	private String mainClass;

	private String serverName;

	private String groupName;

	private String taskName;

	private String confFilePath;

	private String lockIndex;

	private ConfigManager configManager;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void start(String[] args) {
		try {
			Class clazz = Class.forName(this.getMainClass());

			Method mainNameMethod = clazz.getMethod("main", args.getClass());

			mainNameMethod.invoke(clazz, new Object[] { args });
		} catch (Exception e) {
			log.error("[jingwei server] class not find " + this.getMainClass(), e);
			JingWeiUtil.destroyZkAndExit(null, -1);
		}
	}

	//$MAIN_CLASS $TASK_NAME $SERVER_NAME $GROUP_NAME $BASE_DIR/conf/server.ini
	public static void main(String[] args) {
	    
	    log.warn("[jingwei-server] start custom task, args:" + args[0]);

		CandidateTaskCoreLoader loader = new CandidateTaskCoreLoader();

		if (!loader.checkAndParserBootPram(args[0])) {
			JingWeiUtil.destroyZkAndExit(null, -1);
		}

		// e.g./jingwei/tasks/**task/t-locks/lock1
		loader.publishTaskLock();

		//  ע����� e.g. jingwei/tasks/**task/t-locks/lock1,�ڵ���ʧ�������ݱ仯���˳�����
		loader.registerGroupTaskLockListener();

		// �ȴ�������������ʱ�����˳�
		loader.addSessionDisconnectionHandler();

		loader.start(args);
		
		log.warn("[jingwei-server] custom task started");
	}

	/**
	 * �ȴ�������������ʱ�����˳�
	 */
	private void addSessionDisconnectionHandler() {

		this.configManager.addSessionStateListener(new SessionStateListener() {
			private final HashedWheelTimer notifyWheelTimer = new HashedWheelTimer(Executors.defaultThreadFactory());
			private volatile Timeout timeout;

			@Override
			public void handleStateChanged(KeeperState state) throws Exception {
				if (KeeperState.Disconnected == state) {
					log.error(" Disconnected From Zk Add delay Notify Empty Task! ");
					timeout = notifyWheelTimer.newTimeout(new TimerTask() {
						@Override
						public void run(Timeout timeout) throws Exception {
							log.error("zk disconnect timeout exceed spec' timeout. ");
							JingWeiUtil.destroyZkAndExit(configManager, -1);
						}
					}, 20000, TimeUnit.MILLISECONDS);
				} else if (KeeperState.SyncConnected == state) {
					log.error(" SyncConnected From Zk Cancel delay Notify Empty Task! ");
					if (null != timeout) {
						if (!timeout.isCancelled() && !timeout.isExpired()) {
							timeout.cancel();
						}
					}

				}
			}

			@Override
			public void handleNewSession() throws Exception {
				log.error(" handleNewSession! ");
			}
		});
	}

	private void publishTaskLock() {
		try {
			GroupUtil.creatGroupTaskEephemeralLock(configManager, taskName, lockIndex, serverName);
		} catch (Exception e) {
			log.error(
					"[jingwei server]create node error:"
							+ GroupUtil.getGroupTaskEephemeralLockPath(configManager, taskName, lockIndex), e);
			JingWeiUtil.destroyZkAndExit(configManager, -1);
		}
	}

	/**
	 * e.g. /jingweip/tasks/**task/t-locks/lock1 
	 * �ڵ���ʧ���߽ڵ�����ݲ��Ǳ�sever����ʱ��ص��������˳�
	 */
	private void registerGroupTaskLockListener() {
		this.groupTaskLockListener = new TaskInstanceLockListener(this, taskName);

		String path = GroupUtil.getGroupTaskEephemeralLockPath(configManager, taskName, lockIndex);

		this.configManager.addDataListener(path, this.groupTaskLockListener);
	}

	/**
	 * �������
	 * 
	 * @param args   �����в���
	 * @return false �������� ,true ����
	 */
	public boolean checkAndParserBootPram(String argStr) {
		boolean checkRet = false;
		if (StringUtil.isBlank(argStr)) {
			log.error("[jingwei-server] please set right parameter!");
			return checkRet;
		}

		Map<String, String> args = JingWeiUtil.handleArgs(argStr);

		String mainClass = args.get("mainClass");
		if (StringUtil.isBlank(mainClass)) {
			log.error("[jingwei-server] please set main class !");
			return checkRet;
		}
		this.setMainClass(mainClass);

		String taskName = args.get("taskName");
		if (StringUtil.isBlank(taskName)) {
			log.error("[jingwei-server] please set task name !");
			return checkRet;
		}
		this.setTaskName(taskName);

		String serverName = args.get("serverName");
		if (StringUtil.isBlank(serverName)) {
			log.error("[jingwei-server] please set server name !");
			return checkRet;
		}
		this.setServerName(serverName);

		// ƴװ������server�������ļ�·��
		String confPath = args.get("confPath");
		if (StringUtil.isBlank(confPath)) {
			log.error("[jingwei-server] please set conf file path!");
			return checkRet;
		}
		this.setConfFilePath(confPath);

		// ��ʼ��ZK������
		ZkConfig zkConfig = ZkConfig.getZkConfigFromFile(confPath);
		if (null == zkConfig) {
			log.error("[jingwei-server] zk config error please check!");
			return checkRet;
		}

		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();
		this.setConfigManager(zkConfigManager);
		JingWeiUtil.setConfigManager(zkConfigManager);
		JingWeiUtil.setArgString(argStr);

		// group���͵��������ҪgroupName��lockIndex
		String groupName = args.get("groupName");
		// �����Ĭ��������û����
		if (groupName.equals(DEFAULT_GROUP)) {
			this.setGroupName(StringUtil.EMPTY_STRING);
		} else {
			this.setGroupName(groupName);
		}

		String lockIndex = args.get("lockIndex");
		this.setLockIndex(lockIndex);

		return true;
	}

	public String getLockIndex() {
		return lockIndex;
	}

	public void setLockIndex(String lockIndex) {
		this.lockIndex = lockIndex;
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getConfFilePath() {
		return confFilePath;
	}

	public void setConfFilePath(String confFilePath) {
		this.confFilePath = confFilePath;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}

	public void finish() {
		JingWeiUtil.destroyZkAndExit(null, -1);
	}

}
