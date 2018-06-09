package com.taobao.jingwei.server.config;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.server.JingweiServerMain;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * @desc Server�������ļ�
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * 
 * @date 2011-11-16����3:52:14
 */

public class ServerConfig implements JingWeiConstants {
	/** ����tar.gz�ļ���Ŀ¼�� */
	public static final String TARGET_DIR_NAME = "target";

	/** ����task�ļ���Ŀ¼�� */
	public static final String WORK_DIR_NAME = "work";

	/** ����tar.gz�����ļ���Ŀ¼�� */
	public static final String BAK_DIR_NAME = "bak";

	/** service�˿� */
	public static final String DEFAULT_SERVICE_PORT = "9090";

	/** service�Ե�CONTEXT PATH */
	public static final String CTX_PATH = "jingwei-server-api";

	private static Log log = LogFactory.getLog(JingweiServerMain.class);

	private final String serverName;
	private String serverConfFile;
	private String taskBootFilePath;
	private String taskPluginDirPath;
	private int executorCapacity;
	private String serverBaseHome;
	private final String version = VERSION;
	private final String userName = USER_NAME;
	private String servicePort;

	public static String VERSION = "2.1.4-SNAPSHOT";
	public static String TASK_BOOT_FILE_NAME = "task";

	// E.g /home/admin/jingwei-server-2.0.0-SNAPSHOT/bin
	private static String TASK_BOOT_SUB_PATH = "bin";

	public static String PLUGIN_FILE_NAME = "plugin";

	private final static String SERVER_SECTION_NAME = "Server";
	private final static String SERVER_NAME_KEY = "Name";
	private final static String SERVER_BASE_HOME_KEY = "BaseHome";
	private final static String EXECUTOR_CAPACITY_KEY = "ExecutorCapacity";
	private final static String SERVICE_PORT_KEY = "servicePort";

	private final static int DEFAULT_EXECUTOR_COUNT = 0;

	private static ServerConfig instance;

	public static ServerConfig getInstance() {
		return instance;
	}

	public ServerConfig(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * ��ȡ������Ϣ
	 * 
	 * @param filePath �����ļ�·��
	 * @return <code>null</code> �޷���ȡ������
	 */
	public static ServerConfig getServerConfigFromFile(String filePath) {
		ServerConfig serverConfig = null;
		Map<String, String> confMap = JingWeiUtil.getIniValuesFromFile(filePath, SERVER_SECTION_NAME, null);
		log.warn("[jingwei server] load server config from : " + filePath);

		if (!confMap.isEmpty()) {
			String serverName = confMap.get(SERVER_NAME_KEY);
			serverName = StringUtil.isNotBlank(serverName) ? serverName : JingWeiUtil.getLocalHostName();

			if (StringUtil.isNotEmpty(serverName)) {
				serverConfig = new ServerConfig(serverName);
				log.warn("[jingwei server] server name : " + serverName);
			} else {
				log.error("[jingwei server] server name can not get !");
				return null;
			}

			// service�Ķ˿ں�
			String servicePort = confMap.get(SERVICE_PORT_KEY);

			if (StringUtil.isBlank(servicePort)) {
				serverConfig.setServicePort(DEFAULT_SERVICE_PORT);
			} else {
				serverConfig.setServicePort(servicePort);
			}

			serverConfig.setServerConfFile(filePath);

			String serverBaseHome = confMap.get(SERVER_BASE_HOME_KEY);

			serverConfig.setServerBaseHome(serverBaseHome);

			if (StringUtil.isBlank(serverBaseHome)) {
				log.error("[jingwei server] base home is blank!");
				return null;
			}

			serverConfig.setTaskBootFilePath(new StringBuilder(serverBaseHome).append(JingWeiConstants.FILE_SEP)
					.append(TASK_BOOT_SUB_PATH).toString());

			serverConfig.setTaskPluginDirPath(serverBaseHome + JingWeiConstants.FILE_SEP
					+ ServerConfig.PLUGIN_FILE_NAME);

			// group ִ��������
			String executorCapacityStr = confMap.get(EXECUTOR_CAPACITY_KEY);

			if (StringUtil.isBlank(executorCapacityStr)) {
				serverConfig.setExecutorCapacity(DEFAULT_EXECUTOR_COUNT);
			} else {
				serverConfig.setExecutorCapacity(Integer.valueOf(executorCapacityStr));
			}
		}

		instance = serverConfig;

		return serverConfig;
	}

	public String getServerConfFile() {
		return serverConfFile;
	}

	public void setServerConfFile(String serverConfFile) {
		this.serverConfFile = serverConfFile;
	}

	public String getServerName() {
		return serverName;
	}

	public String getTaskBootFilePath() {
		return taskBootFilePath;
	}

	public void setTaskBootFilePath(String taskBootFilePath) {
		this.taskBootFilePath = taskBootFilePath;
	}

	public String getTaskPluginDirPath() {
		return taskPluginDirPath;
	}

	public void setTaskPluginDirPath(String taskPluginDirPath) {
		this.taskPluginDirPath = taskPluginDirPath;
	}

	public int getExecutorCapacity() {
		return executorCapacity;
	}

	public void setExecutorCapacity(int executorCapacity) {
		this.executorCapacity = executorCapacity;
	}

	public String getVersion() {
		return version;
	}

	public String getUserName() {
		return userName;
	}

	public String getServerBaseHome() {
		return serverBaseHome;
	}

	public void setServerBaseHome(String serverBaseHome) {
		this.serverBaseHome = serverBaseHome;
	}

	public String getServicePort() {
		return servicePort;
	}

	public void setServicePort(String servicePort) {
		this.servicePort = servicePort;
	}

}
