package com.taobao.jingwei.server.plugin;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.server.ServerTaskNode;
import com.taobao.jingwei.common.node.server.ServerTaskNode.PluginTaskTargetStateEnum;
import com.taobao.jingwei.common.node.server.ServerTaskNode.PluginTaskWorkStateEnum;
import com.taobao.jingwei.common.node.server.ServerTaskNode.TaskTypeEnum;
import com.taobao.jingwei.server.config.TaskManifestConfig;
import com.taobao.jingwei.server.core.ServerCoreThread;
import com.taobao.jingwei.server.util.ServerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ����plugin�µ�targetĿ¼��workĿ¼����
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * 
 * @date 2011-12-6����3:51:14
 */
public class PluginScannerHelper {
	private static Log log = LogFactory.getLog(PluginScannerHelper.class);

	/** ����tar.gz�ļ���Ŀ¼�� */
	private static final String TARGET_DIR_NAME = "target";

	/** ����task�ļ���Ŀ¼�� */
	private static final String WORK_DIR_NAME = "work";

	/** ��������plugin �����Ŀ¼ */
	public static final String JINGWEI_PLUGIN_CONF_PATH = "conf";

	/** ��������plugin �����Ŀ¼ */
	public static final String JINGWEI_PLUGIN_LIB_PATH = "lib";

	/** ��������plugin ����������ļ� */
	private static final String JINGWEI_MF = "JINGWEI.MC";

	/** tar.gz�ļ������� */
	private final TarGzFileFilter tarGzFileFilter = new TarGzFileFilter();

	/** plugin task�ļ��й����� */
	private final PluginTaskFileFilter pluginTaskFileFilter = new PluginTaskFileFilter();

	/** ɨ���ļ������߳� */
	private final ServerCoreThread serverCoreThread;

	public PluginScannerHelper(ServerCoreThread serverCoreThread) {
		this.serverCoreThread = serverCoreThread;
	}

	/**
	 * ��ȡplugin��workĿ¼
	 * 
	 * @return ���������workĿ¼��e.g $JINGEWI_SERVER_TOOR$/plugin/work
	 */
	public File getPluginWorkdir() {
		StringBuilder sb = new StringBuilder();

		sb.append(this.serverCoreThread.getServerConfig().getTaskPluginDirPath()).append(JingWeiUtil.FILE_SEP);
		sb.append(WORK_DIR_NAME);

		return new File(sb.toString());
	}

	/**
	 * ��ȡplugin��targetĿ¼
	 * 
	 * @return ���������targetĿ¼��e.g $JINGEWI_SERVER_TOOR$/plugin/target
	 */
	private File getPluginTargetdir() {
		StringBuilder sb = new StringBuilder();

		sb.append(serverCoreThread.getServerConfig().getTaskPluginDirPath()).append(JingWeiUtil.FILE_SEP);
		sb.append(TARGET_DIR_NAME);

		return new File(sb.toString());
	}

	/**
	 * ���е�tar.gzѹ���ļ�
	 * 
	 * @return �������� targetĿ¼�µ��ļ�
	 */
	public File[] getPluginTargets() {
		if (getPluginTargetdir() == null) {
			return new File[0];
		}

		return getPluginTargetdir().listFiles(tarGzFileFilter);
	}

	/**
	 * ���е�tar.gzѹ���ļ����ļ���
	 * 
	 * @return �������� targetĿ¼�µ��ļ�
	 */
	public List<String> getPluginTargetFileNames() {
		File[] files = this.getPluginTargets();
		List<String> fileNames = new ArrayList<String>();

		for (File f : files) {
			fileNames.add(f.getName());
		}

		return fileNames;
	}

	/**
	 * ���е�work�ļ��У�ÿһ��Ŀ¼��Ӧһ��task; Ŀ¼����taskͬ��
	 * 
	 * @return �������� workĿ¼�µ��ļ�Ŀ¼
	 */
	public File[] getPluginWorkTaskDirs() {
		if (getPluginWorkdir() == null) {
			return new File[0];
		}

		return getPluginWorkdir().listFiles(pluginTaskFileFilter);
	}

	/**
	 * ���е�workĿ¼�µĶ�������Ŀ¼��
	 * 
	 * @return �������� workĿ¼�µ�Ŀ¼��
	 */
	public List<String> getPluginWorkFileNames() {
		File[] files = this.getPluginWorkTaskDirs();
		List<String> fileNames = new ArrayList<String>();

		for (File f : files) {
			fileNames.add(f.getName());
		}

		return fileNames;
	}

	/**
	 * ��ȡtargetĿ¼���ļ���
	 * 
	 * @return ��ȡtargetĿ¼��������
	 */
	public Set<String> getTaskNamesFromTargetDir() {
		File[] files = this.getPluginTargets();

		return this.getTaskNamesFromTargetFiles(files);
	}

	/**
	 * ��ȡtargetĿ¼���ļ���
	 * 
	 * @return ��ȡtargetĿ¼��������
	 */
	public Set<String> getTaskNamesFromTargetFiles(File[] files) {
		Set<String> targetNames = new HashSet<String>();

		for (File file : files) {
			String taskName = this.getTaskNameByTargetFileName(file.getName());
			targetNames.add(taskName);
		}

		return targetNames;
	}

	/**
	 * ��ȡworkĿ¼��Ŀ¼��
	 * 
	 * @return ��ȡworkĿ¼��������
	 */
	public Set<String> getTaskNamesFromWorkDir() {
		File[] dirs = this.getPluginWorkTaskDirs();

		return this.getTaskNamesFromWorkDirs(dirs);
	}

	/**
	 * ��ȡworkĿ¼��Ŀ¼��
	 * 
	 * @return ��ȡworkĿ¼��������
	 */
	public Set<String> getTaskNamesFromWorkDirs(File[] dirs) {
		Set<String> workTaskNames = new HashSet<String>();

		for (File file : dirs) {
			workTaskNames.add(file.getName());
		}

		return workTaskNames;
	}

	/**
	 * ��ȡtask��MF�ļ�ȫ·��
	 * 
	 * @param taskName
	 * @return ���������MF�ļ��������������ȫ·����
	 */
	public String getMenifestFileFullName(String taskName) {
		StringBuilder sb = new StringBuilder();

		sb.append(getPluginWorkdir().toString()).append(JingWeiUtil.FILE_SEP);
		sb.append(taskName).append(JingWeiUtil.FILE_SEP);
		sb.append(JINGWEI_PLUGIN_CONF_PATH).append(JingWeiUtil.FILE_SEP);
		sb.append(JINGWEI_MF);

		return sb.toString();
	}

	/**
	 * ��ȡ���������������<br>
	 * 
	 * modified by leiwen.zh 2013-1-31, ����Ѱ��main-class�����ȼ�:<br>
	 * 1����work/**taskĿ¼��<br>
	 * 2��1�Ҳ���, ��work/**groupĿ¼��
	 * 
	 * @return task��Ӧ���������ȫ·��
	 */
	public String getMainClassByTaskName(String taskName, String groupName) {
		String taskFullPath = this.getMenifestFileFullName(taskName);
		String groupFullPath = this.getMenifestFileFullName(groupName);

		File taskDir = new File(taskFullPath);
		File groupDir = new File(groupFullPath);

		TaskManifestConfig taskManifestConfig = null;
		if (taskDir.exists()) {
			// ��work/**taskĿ¼��
			taskManifestConfig = TaskManifestConfig.getTaskManifestConfig(taskFullPath);
		} else if (groupDir.exists()) {
			// ��work/**groupĿ¼��
			taskManifestConfig = TaskManifestConfig.getTaskManifestConfig(groupFullPath);
		}

		return taskManifestConfig.getTaskMainClass();
	}

	/**
	 * tar.gz�ļ�������
	 * 
	 * @author shuohailhl
	 * 
	 */
	public static class TarGzFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			String name = pathname.getName().toLowerCase();

			if (pathname.isFile() && (name.endsWith(".tar.gz"))) {
				return true;
			}

			return false;
		}
	}

	/**
	 * ���������ļ������� workĿ¼�µĶ������� Ŀ¼�������ļ���
	 * 
	 * @author shuohailhl
	 * 
	 */
	private static class PluginTaskFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	}

	/**
	 * ��ѹ��tar.gz�ļ�
	 * 
	 * @param fileName Ҫ��ѹ����ȫ·���ļ���
	 */
	public void unTarGz(String fileName) {

		log.warn("[jingwei server] un tar.gz file : " + fileName);

		StringBuilder shellBuilder = new StringBuilder();
		shellBuilder.append(" tar -C ");
		shellBuilder.append(this.getPluginWorkdir().getAbsolutePath() + JingWeiUtil.FILE_SEP);
		shellBuilder.append(" -zxf ");
		shellBuilder.append(fileName);

		ServerUtil.callShell(shellBuilder.toString());
	}

	/**
	 * ����targetѹ�����ļ�����ȡ������
	 * 
	 * @param target�ļ���
	 * @return ��Ӧ��task��
	 */
	public String getTaskNameByTargetFileName(String targetName) {
		return new StringBuilder(targetName).substring(0, targetName.indexOf("tar.gz") - 1).toString();
	}

	/**
	 * ����targetѹ�����ļ�����ȡ������
	 * 
	 * @param target�ļ���
	 * @return ��Ӧ��task��
	 */
	public String getTaskNameByWorkDirName(String workDirName) {
		return workDirName;
	}

	/**
	 * ��zk����Ӧtask����target��work�е�״̬
	 * 
	 * @param taskName ������
	 * @param targetState ������targetĿ¼��״̬ {@link PluginTaskTargetStateEnum}
	 * @param orkState ������workĿ¼��״̬ {@link PluginTaskWorkStateEnum}
	 */
	public void addOrUpdateCustomerServerTaskNode(String taskName, PluginTaskTargetStateEnum targetState,
			PluginTaskWorkStateEnum workState) {
		ConfigManager configManager = this.serverCoreThread.getConfigManager();
		String serverName = this.serverCoreThread.getServerConfig().getServerName();
		String taskPath = ServerTaskNode.getDataIdOrNodePathByServerTaskName(serverName, taskName);

		ServerTaskNode serverTaskNode = new ServerTaskNode();
		serverTaskNode.setName(taskName);
		serverTaskNode.setTaskName(taskName);
		serverTaskNode.setServerName(this.serverCoreThread.getServerConfig().getServerName());
		serverTaskNode.setTaskType(TaskTypeEnum.CUSTOMER);
		serverTaskNode.setPluginTaskTargetStateEnum(targetState);
		serverTaskNode.setPluginTaskWorkStateEnum(workState);

		try {

			configManager.publishOrUpdateData(taskPath, serverTaskNode.toJSONString(), serverTaskNode.isPersistent());

		} catch (JSONException e) {
			log.error("[jingwei server] get zk node data error! node data is: " + serverTaskNode);
		} catch (Exception e) {
			log.error("[jingwei server] publish zk node data error! node path is: " + taskPath);
		}

		log.warn("[jingwei server] add or update customer task at zk task name : " + taskName + "; target state is : "
				+ targetState.toString() + "; work state is : " + workState);

	}

	/**
	 * �ж�task�Ƿ���targetĿ¼��
	 * 
	 * @param taskName ����task���ж�
	 * @return true target�ļ���null ��ʾû��
	 */
	public File taskInTarget(String taskName) {
		File[] targetFiles = this.getPluginTargets();

		for (File targetFile : targetFiles) {
			if (this.getTaskNameByTargetFileName(targetFile.getName()).equals(taskName)) {
				return targetFile;
			}
		}

		return null;
	}

	/**
	 * �ж�task�Ƿ���workĿ¼��
	 * 
	 * @param taskName ����task���ж�
	 * @return true ��ʾworkĿ¼��null ��ʾû��
	 */
	public File taskInWork(String taskName) {
		File[] workDirs = this.getPluginWorkTaskDirs();

		for (File workDir : workDirs) {
			if (this.getTaskNameByWorkDirName(workDir.getName()).equals(taskName)) {
				return workDir;
			}
		}

		return null;
	}

	/**
	 * ���workĿ¼
	 */
	public void clearWorkDir() {

		log.warn("[jingwei server] clear plugin work dir!");

		StringBuilder shellBuilder = new StringBuilder();
		shellBuilder.append(" rm -rf ");
		shellBuilder.append(this.getPluginWorkdir().getAbsolutePath());
		shellBuilder.append(JingWeiConstants.FILE_SEP).append("*");

		ServerUtil.callShell(shellBuilder.toString());
	}

	/**
	 * ���tat.gz�ļ���������
	 * 
	 * @param gzFile
	 * @return <code>false</code>�ļ������ڻ��߲���������ִ�д���
	 * @throws IOException
	 */
	public boolean isIntegrity(File gzFile) throws IOException {
		if (!gzFile.exists()) {
			return false;
		}

		String script = " gzip -tv " + gzFile.getAbsolutePath();

		Process proc = Runtime.getRuntime().exec(script);
		int exitValue = 0;
		try {
			exitValue = proc.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}

		if (exitValue == 0) {
			return true;
		}

		return false;
	}

	public ServerCoreThread getServerCoreThread() {
		return serverCoreThread;
	}
}
