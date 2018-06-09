package com.taobao.jingwei.server.core;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.config.ConfigDataListener;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.StatusNode.StatusEnum;
import com.taobao.jingwei.common.node.server.ServerNode;
import com.taobao.jingwei.server.config.ServerConfig;
import com.taobao.jingwei.server.group.CandidateTaskManager;
import com.taobao.jingwei.server.listener.TasksChildChangeListener;
import com.taobao.jingwei.server.service.HttpStaticFileServer;
import com.taobao.jingwei.server.util.BothTaskSet;
import com.taobao.jingwei.server.util.ServerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

/**
 * ��������
 * 
 * <ol>
 * <li>�鿴status״̬���Ƿ�Ϊrunning������� �������У��˳�
 * <li>�����ýڵ�������opΪstart��task
 * <li>ע��/jingwei/servers/**server/tasks�ڵ��Child������
 * <li>ע��SeeeionStateListener
 * <li>����running״̬��/jingwei/servers/**server/status
 * </ol>
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * @date 2011-11-16����3:41:36
 */
public class ServerCoreThread implements Runnable {
	private final Log log = LogFactory.getLog(this.getClass());

	/** ������������� */
	private final CustomerTaskManager customerTaskManager;

	/** ������������� */
	private final BuildinTaskManager buildinTaskManager;

	/** ���Ӽ������������ */
	private final TasksChildChangeListener tasksChildChangeListener;

	/** ���ù����� */
	private final ConfigManager configManager;

	/** Server������ */
	private final ServerConfig serverConfig;

	/** server��״̬�ڵ� */
	private volatile StatusNode statusNode = new StatusNode();

	/** ɨ��ִ��start������ */
	private final CandidateTaskManager candidateTaskManager;

	public ServerCoreThread(ConfigManager configManager, ServerConfig serverConfig) {
		this.configManager = configManager;
		this.serverConfig = serverConfig;
		this.customerTaskManager = new CustomerTaskManager(this);
		this.buildinTaskManager = new BuildinTaskManager(this);
		this.tasksChildChangeListener = new TasksChildChangeListener(this);
		this.candidateTaskManager = new CandidateTaskManager(this);
	}

	@Override
	public void run() {
		// ��1��running
		this.statusNode
				.setOwnerDataIdOrPath(ServerNode.getDataIdOrPathFromServerName(this.serverConfig.getServerName()));
		this.publishRunning();
		this.addRepublishRunningListener();

		// ����ִ����������server�汾���û�����zk
		this.publishServerConfig2Zk();

		// ��2����ʼ������ ���������, �ýڵ��µ�task
		BothTaskSet bothTaskSet = ServerUtil.loadTaskFromZkForTaskManager(this.serverConfig, this.configManager);

		this.buildinTaskManager.init();
		log.warn("[jingwei-server] add buildin task already configed at zookeeper : " + bothTaskSet.getBuildinTasks());
		this.buildinTaskManager.prepare(bothTaskSet.getBuildinTasks());

		this.customerTaskManager.init();
		this.customerTaskManager.preparePlugin(bothTaskSet.getCustomerTasks());
		log.warn("[jingwei-server] add customer task already configed at zookeeper : " + bothTaskSet.getCustomerTasks());

		// ��ʱɨ��plugin��target��workĿ¼
		this.customerTaskManager.beginScanPlugin();

		// ��3��ע��/jingwei/servers/**server/tasks�ڵ��Child������
		String path = ServerUtil.getServerTasksNodePath(this.serverConfig.getServerName());
		this.configManager.addChildChangesListener(path, this.getTasksChildChangeListener());

		// (4) ��ʼ�����������
		this.candidateTaskManager.init();

		// (5) ����jingwei����
		new HttpStaticFileServer(ServerConfig.getInstance().getServicePort()).start();

		// �����ɹ���¼��־
		log.warn("[jingwei-server] " + this.serverConfig.getServerName() + " successful started !");
	}

	/**
	 * ����executor������zk
	 */
	private void publishServerConfig2Zk() {
		String path = ServerNode.getDataIdOrPathFromServerName(this.serverConfig.getServerName());
		String data = this.configManager.getData(path);

		ServerNode serverNode = new ServerNode();
		if (data != null) {
			try {
				serverNode.jsonStringToNodeSelf(data);

			} catch (JSONException e) {
				log.error(e);
				this.destoryAndExit();
			}
		}
		ServerConfig serverConfig = this.getServerConfig();
		serverNode.setExecutorCount(serverConfig.getExecutorCapacity());
		serverNode.setVersion(serverConfig.getVersion());
		serverNode.setUserName(serverConfig.getUserName());
		try {
			this.configManager.publishOrUpdateData(path, serverNode.toJSONString(), true);
		} catch (Exception e) {
			log.error(e);
			this.destoryAndExit();
		}
	}

	public void addRepublishRunningListener() {

		String path = this.getStatusPath();

		this.getConfigManager().addDataListener(path, new ConfigDataListener() {
			@Override
			public void handleData(String dataIdOrPath, String data) {
				if (StringUtil.isBlank(data)) {
					log.warn("[jingwei server] server running is blank");
					ServerCoreThread.this.publishRunning();
				}
			}
		});
	}

	/**
	 * �˳�ϵͳ���ر�ZK Client
	 */
	public void destoryAndExit() {
		log.warn("[jingwei-server] server  exits !");
		JingWeiUtil.destroyZkAndExit(configManager, -1);
	}

	/**
	 * ��zkдrunning״̬, e.g /jingwei/servers/**server/status
	 */
	public void publishRunning() {
		String statusNodePath = this.getStatusPath();
		StatusNode statusNode = new StatusNode();
		statusNode.setStatusEnum(StatusEnum.RUNNING);
		statusNode.setName(JingWeiUtil.JINGWEI_STATUS_NODE_NAME);
		try {
			this.configManager
					.publishOrUpdateData(statusNodePath, statusNode.toJSONString(), statusNode.isPersistent());
		} catch (JSONException e) {
			log.error("[jingwei server] get zk node data from json string error! node path is: " + statusNodePath);
			this.destoryAndExit();
		} catch (Exception e) {
			log.error("[jingwei server] putlish zk node data error! node path is: " + statusNodePath);
			this.destoryAndExit();
		}
	}

	private String getStatusPath() {
		String serverNodePath = ServerNode.getDataIdOrPathFromServerName(this.serverConfig.getServerName());
		String statusNodePath = serverNodePath + JingWeiUtil.ZK_PATH_SEP + JingWeiUtil.JINGWEI_STATUS_NODE_NAME;
		return statusNodePath;
	}

	public TasksChildChangeListener getTasksChildChangeListener() {
		return tasksChildChangeListener;
	}

	public CustomerTaskManager getCustomerTaskManager() {
		return customerTaskManager;
	}

	public BuildinTaskManager getBuildinTaskManager() {
		return buildinTaskManager;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public ServerConfig getServerConfig() {
		return serverConfig;
	}

	public CandidateTaskManager getCandidateTaskManager() {
		return candidateTaskManager;
	}
}