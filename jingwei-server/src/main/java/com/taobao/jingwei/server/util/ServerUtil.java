package com.taobao.jingwei.server.util;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.OperateNode;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.common.node.server.ServerNode;
import com.taobao.jingwei.common.node.server.ServerTaskNode;
import com.taobao.jingwei.common.node.server.ServerTaskNode.TaskTypeEnum;
import com.taobao.jingwei.core.kernel.AbstractJingWeiCore;
import com.taobao.jingwei.core.kernel.JingWeiCore;
import com.taobao.jingwei.server.config.ServerConfig;
import com.taobao.jingwei.server.core.ServerTaskCore;
import com.taobao.jingwei.server.service.ServiceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * @desc ������
 * @author <a href="mailto:shuohai.lhl@taobao.com">˷�� shuohailhl</a>
 * @date 2011-12-6����2:29:55
 */
public class ServerUtil implements JingWeiConstants {
	private static Log log = LogFactory.getLog(ServerUtil.class);

	private static Timer timer = new Timer();

	private ServerUtil() {
	}

	/**
	 * ����shell�ű�
	 * 
	 * @param shellString shell �ַ���
	 */
	public static void callShell(String shellString) {
		// ���øýű�
		log.warn("[jingwei server] call shell : " + shellString);

		Process process = null;
		try {
			process = Runtime.getRuntime().exec(shellString);
			final InputStream is1 = process.getInputStream();
			new Thread(new Runnable() {
				public void run() {
					BufferedReader br = new BufferedReader(new InputStreamReader(is1));
					try {
						while (br.readLine() != null)
							;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start(); // �����������߳������process.getInputStream()�Ļ�����

			InputStream is2 = process.getErrorStream();
			BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
			StringBuilder buf = new StringBuilder(); // ������������
			String line = null;
			while ((line = br2.readLine()) != null)
				buf.append(line); // ѭ���ȴ�ffmpeg���̽���
			// System.out.println("������Ϊ��" + buf);
		} catch (Throwable e) {
			log.error("[jingwei-server] callShell Error Shell is : " + shellString);
		} finally {
			if (null != process)
				process.destroy();
		}

	}

	/**
	 * ������������server�ϻ�ȡ��Ӧ��
	 * 
	 * @param serverName
	 * @param taskName
	 * @return operate�ڵ��·��, e.g jingwei/servers/**server/tasks/**task/operate
	 */
	public static String getOpNodePathByServerTaskName(String serverName, String taskName) {
		StringBuilder sb = new StringBuilder();
		sb.append(ServerTaskNode.getDataIdOrNodePathByServerTaskName(serverName, taskName)).append(
				JingWeiUtil.ZK_PATH_SEP);
		sb.append(JingWeiUtil.JINGWEI_OPERATE_NODE_NAME);
		return sb.toString();
	}

	/**
	 * ����server������������ȡ��Ӧop������
	 * 
	 * @param configManager zk���ù�����
	 * @param serverName server��
	 * @param taskName ������
	 * @return OperateEnum task��Ӧ��operate����, <code>null</code> ��ʾ��ȡʧ��
	 */
	public static OperateEnum getOperateByServerTaskName(ConfigManager configManager, String serverName, String taskName) {
		OperateNode opNode = new OperateNode();
		String opPath = getOpNodePathByServerTaskName(serverName, taskName);
		String opNodeJsonString = configManager.getData(opPath);
		try {
			opNode.jsonStringToNodeSelf(opNodeJsonString);
		} catch (JSONException e) {
			log.error("get task operate type error! path : " + opPath, e);
			return null;
		}
		return opNode.getOperateEnum();
	}

	/**
	 * �ж�����ڵ��Ƿ����
	 * 
	 * @param configManager
	 * @param taskName
	 * @return <code>true</code>��ʾ������� <code>false</code>��ʾ���������
	 */
	public static boolean taskExist(ConfigManager configManager, String taskName) {
		String path = JingWeiConstants.JINGWEI_TASK_ROOT_PATH + FILE_SEP + taskName;

		return configManager.exists(path);
	}

	/**
	 * ����server������������ȡ����ڵ������
	 * 
	 * @param configManager ���������
	 * @param serverName server��
	 * @param taskName ������
	 * @return ָ����ServerTaskNode, null ��ʾ��ȡʧ��
	 */
	public static ServerTaskNode getServerTaskNodeByServerTaskNameFromZk(ConfigManager configManager,
			String serverName, String taskName) {
		ServerTaskNode serverTaskNode = new ServerTaskNode();
		String taskPath = ServerTaskNode.getDataIdOrNodePathByServerTaskName(serverName, taskName);
		String taskNodeJsonString = configManager.getData(taskPath);

		try {
			serverTaskNode.jsonStringToNodeSelf(taskNodeJsonString);
		} catch (JSONException e) {
			log.error("get task node data from json string error! path : " + taskPath, e);
			return null;
		}

		return serverTaskNode;
	}

	/**
	 * ����task�Ľű�
	 * 
	 * @param taskBootFileFullName task�������ű�
	 * @param mainClass task��������
	 * @param TaskTypeEnum ��������
	 * @param serverName server name
	 */
	public static void startJingweiTask(String taskBootFileFullName, String mainClass, String taskName,
			TaskTypeEnum taskTypeEnum, String serverName, String lockIndex, String groupName, String workDir,
			String javaOpt) {
		/**
		 * ƴװ�����ű��ַ��� ��ʽ�� windows: call path/fileName.bat linux: sh path/fileName.sh
		 */
		StringBuilder shellBuilder = new StringBuilder(JingWeiUtil.isWinOs() ? "call " : "sh ");
		// ƴװ·������
		shellBuilder.append(taskBootFileFullName);
		shellBuilder.append(".").append(JingWeiUtil.isWinOs() ? "bat " : "sh ");

		shellBuilder.append(ServerUtil.getParams(mainClass, taskName, taskTypeEnum, serverName, lockIndex, groupName,
				workDir, javaOpt));

		ServerUtil.callShell(shellBuilder.toString());
	}

	public static String getParams(String mainClass, String taskName, TaskTypeEnum taskTypeEnum, String serverName,
			String lockIndex, String groupName, String workDir, String javaOpt) {
		StringBuilder shellBuilder = new StringBuilder();

		shellBuilder.append(JingWeiConstants.SPACE_SEP);
		shellBuilder.append(mainClass);

		shellBuilder.append(JingWeiConstants.SPACE_SEP);
		shellBuilder.append(taskName);

		shellBuilder.append(JingWeiConstants.SPACE_SEP);
		shellBuilder.append(taskTypeEnum.toString());

		shellBuilder.append(JingWeiConstants.SPACE_SEP);
		shellBuilder.append(serverName);

		shellBuilder.append(JingWeiConstants.SPACE_SEP);
		shellBuilder.append(lockIndex);

		shellBuilder.append(JingWeiConstants.SPACE_SEP);
		shellBuilder.append(groupName);

		shellBuilder.append(JingWeiConstants.SPACE_SEP);
		shellBuilder.append(workDir);

		shellBuilder.append(JingWeiConstants.SPACE_SEP);
		shellBuilder.append(javaOpt);

		return shellBuilder.toString();
	}

	/**
	 * ��ȡtask�������ű�
	 * 
	 * @param serverConfig
	 * @return ����task�Ľű���λ��$JINGWEI_SERVER_ROOT$/bin
	 */
	public static String getTaskBootFileFullPath(ServerConfig serverConfig) {
		StringBuilder sb = new StringBuilder();

		sb.append(serverConfig.getTaskBootFilePath());
		sb.append(JingWeiUtil.ZK_PATH_SEP);
		sb.append(ServerConfig.TASK_BOOT_FILE_NAME);

		return sb.toString();
	}

	/**
	 * ��ȡtasks�ڵ�·��
	 * 
	 * @param servrName server��
	 * @return tasks�ڵ�·��, e.g /jingwei/servrs/**server/tasks
	 */
	public static String getServerTasksNodePath(String servrName) {
		StringBuilder sb = new StringBuilder(ServerNode.getDataIdOrPathFromServerName(servrName))
				.append(JingWeiUtil.ZK_PATH_SEP);
		sb.append(JingWeiUtil.JINGWEI_SERVER_TASKS_NAME);
		return sb.toString();
	}

	/**
	 * ��zk��Ӧ�ڵ�дoperate��stop
	 * 
	 * @param taskName ������
	 * @throws JSONException node�ڵ�ת��json�ַ����쳣
	 * @throws Exception дzk�ڵ��쳣
	 */
	public static void publishStopTaskOperate(ConfigManager configManager, String serverName, String taskName)
			throws JSONException, Exception {
		OperateNode opNode = new OperateNode();
		opNode.setName(JingWeiUtil.JINGWEI_OPERATE_NODE_NAME);
		opNode.setOperateEnum(OperateEnum.NODE_STOP);
		String parentNodeId = ServerTaskNode.getDataIdOrNodePathByServerTaskName(serverName, taskName);
		opNode.setOwnerDataIdOrPath(parentNodeId);
		String opDataIdOrPath = opNode.getDataIdOrNodePath();
		configManager.publishOrUpdateData(opDataIdOrPath, opNode.toJSONString(), opNode.isPersistent());
	}

	/**
	 * ��zk�����õ�task��ӵ�buildin��������customer������
	 * 
	 * @param serverConfig server������Ϣ
	 * @param configManager zk���ù�����
	 * @return buildin��customer�������͵�task
	 */
	public static BothTaskSet loadTaskFromZkForTaskManager(ServerConfig serverConfig, ConfigManager configManager) {
		StringBuilder sb = new StringBuilder(ServerNode.getDataIdOrPathFromServerName(serverConfig.getServerName()))
				.append(JingWeiUtil.ZK_PATH_SEP);
		sb.append(JingWeiUtil.JINGWEI_SERVER_TASKS_NAME);
		Map<String, String> tasks = configManager.getChildDatas(sb.toString(), null);
		BothTaskSet bothTaskSet = new BothTaskSet();
		for (Map.Entry<String, String> entry : tasks.entrySet()) {
			String taskName = entry.getKey();
			ServerTaskNode serverTaskNode = new ServerTaskNode();

			try {
				serverTaskNode.jsonStringToNodeSelf(entry.getValue());
			} catch (JSONException e) {
				log.error(
						"[jingwei server]get server task node data from json string error! path : "
								+ sb.append(JingWeiUtil.ZK_PATH_SEP).append(taskName), e);
				continue;
			}

			if (TaskTypeEnum.CUSTOMER == serverTaskNode.getTaskType()) {

				bothTaskSet.addCustomerTask(serverTaskNode.getTaskName());

			} else if (TaskTypeEnum.BUILDIN == serverTaskNode.getTaskType()) {

				bothTaskSet.addBuildinTask(serverTaskNode.getTaskName());

			}
		}

		log.warn("[jingwei-server] get tasks from zk , buildin tasks : " + bothTaskSet.getBuildinTasks()
				+ "; customer tasks : " + bothTaskSet.getCustomerTasks());

		return bothTaskSet;
	}

	/**
	 * ��ȡһ����������taskִ�ж���
	 * 
	 * @param groupName
	 * @return <code>List</code>ִ�ж�����б�
	 */
	public static Set<String> getTasks(ConfigManager configManager, String groupName) {
		// path
		StringBuilder sb = new StringBuilder(JingWeiConstants.JINGWEI_GROUP_ROOT_PATH);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(groupName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(JingWeiConstants.JINGWEI_GROUP_TASKS_NAME);

		Set<String> tasks = configManager.getChildDatas(sb.toString(), null).keySet();

		return tasks;
	}

	public static List<File> listSpecifFile(String path, final String suffix) {

		List<File> files = new ArrayList<File>();

		LinkedList<File> list = new LinkedList<File>();
		File dir = new File(path);
		list.add(dir);

		File tmp;
		while (!list.isEmpty()) {
			tmp = (File) list.removeFirst();

			File[] file = tmp.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.getName().endsWith(suffix);
				}
			});

			if (file == null) {
				continue;
			}
			for (int i = 0; i < file.length; i++) {
				if (file[i].isDirectory())
					list.add(file[i]);
				else
					System.out.println(file[i].getAbsolutePath());
				files.add(file[i]);
			}
		}

		return files;
	}

	/**
	 * ��ȡ����tar.gz�İ�
	 * 
	 * @param path
	 * @param suffix
	 * @return
	 */
	public static List<String> listFileNames(String path, String ext) {
		List<String> tarNames = new ArrayList<String>();
		List<File> tars = listSpecifFile(path, ext);
		for (File f : tars) {
			tarNames.add(f.getName());
		}

		return tarNames;
	}

	/**
	 * �ǵݹ�ɾ���ļ���
	 * 
	 * @param path ����·��
	 * @return <code>true</code> ��ʾ�ɹ���<code>false</code>��ʾʧ��
	 */
	public static boolean deleteDirNoRecursion(String path) {
		List<String> deleteList = new ArrayList<String>();
		deleteList.add(path);
		while (deleteList.size() > 0) {
			int i = deleteList.size() - 1;
			String currentPath = deleteList.get(i);
			File[] files = (new File(currentPath)).listFiles();
			if (files.length == 0) {
				(new File(currentPath)).delete();
				deleteList.remove(i);
			} else {
				for (int k = 0; k < files.length; k++) {
					if (files[k].isDirectory()) {
						deleteList.add(files[k].getPath());
					} else {
						boolean success = files[k].delete();
						if (!success) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	/**
	 * ��ȡserver�����е�tar��
	 * 
	 * @return ���û���򷵻ؿյ�list
	 */
	public static List<String> getTarNamesAtServer() {
		// e.g. $JINGWEI_SERVER_HOME/plugin/target
		String path = ServiceUtil.getTargetDirRealPath();

		// ��ȡ���е�tar
		List<String> list = ServerUtil.listFileNames(path, ".tar.gz");

		return list;
	}

	/**
	 * ��ȡserver�����е�bak��tar��
	 * 
	 * @return ���û���򷵻ؿյ�list
	 */
	public static List<String> getBakTarNamesAtServer() {
		// e.g. $JINGWEI_SERVER_HOME/plugin/bak
		String path = ServiceUtil.getBakDirRealPath();

		// ��ȡ���е�tar
		List<String> list = ServerUtil.listFileNames(path, StringUtil.EMPTY_STRING);

		return list;
	}

	/**
	 * ɾ��$JINGWEI_SERVER_HOME/plugin/targetĿ¼�µ�tar�ļ�
	 * 
	 * @param tarName tar����
	 * @return <code>true</code> ��ʾ�ɹ���<code>false</code>��ʾʧ��
	 */
	public static boolean removeTargetTar(String tarName) {

		String realPath = ServerUtil.getTargetTarRealPath(tarName);

		File f = new File(realPath);

		log.warn("delete file : " + realPath);
		return f.delete();
	}

	/**
	 * ��ȡ����·�� $JINGWEI_SERVER_HOME/plugin/targetĿ¼�µ�tar�ļ�
	 * 
	 * @param tarName
	 * @return
	 */
	public static String getTargetTarRealPath(String tarName) {

		String path = ServiceUtil.getTargetDirRealPath();

		String realPath = new StringBuilder(path).append(FILE_SEP).append(tarName).toString();
		return realPath;
	}

	/**
	 * ��ȡ����·�� $JINGWEI_SERVER_HOME/plugin/bakĿ¼�µ�tar�ļ�
	 * 
	 * @param tarName
	 * @return
	 */
	public static String getBakTarRealPath(String tarName) {

		String path = ServiceUtil.getBakDirRealPath();

		String realPath = new StringBuilder(path).append(FILE_SEP).append(tarName).toString();
		return realPath;
	}

	/**
	 * ��tar.tar.gz��targetĿ¼������bakĿ¼
	 * 
	 * @param tarName
	 * @return <code>true</code> ��ʾ�ɹ���<code>false</code>��ʾʧ��
	 */
	public static boolean copyTarget2Bak(String tarName) {

		String targetTarRealPath = ServerUtil.getTargetTarRealPath(tarName);
		String bakTarRealPath = ServerUtil.getBakTarRealPath(tarName);
		log.warn("start backup tar : " + tarName);

		File sourceFile = new File(targetTarRealPath);
		File targetFile = new File(bakTarRealPath);

		try {
			ServerUtil.copyForChannel(sourceFile, targetFile);
		} catch (IOException e) {
			log.error("copy file failed " + tarName);
			return false;
		}

		return true;
	}

	/**
	 * ɾ��$JINGWEI_SERVER_HOME/plugin/bakĿ¼�µ�tar�ļ�
	 * 
	 * @param tarName tar����
	 * @return <code>true</code> ��ʾ�ɹ���<code>false</code>��ʾʧ��
	 */
	public static boolean removeBakTar(String tarName) {

		String realPath = ServerUtil.getBakTarRealPath(tarName);
		log.warn("delete file : " + realPath);

		File f = new File(realPath);
		return f.delete();
	}

	/**
	 * ɾ��$JINGWEI_SERVER_HOME/plugin/workĿ¼�µ�tar�ļ� *
	 * 
	 * @param tarName tar����
	 * 
	 * @return <code>true</code> ��ʾ�ɹ���<code>false</code>��ʾʧ��
	 */
	public static boolean removeWork(String taskWorkDirPath) {
		String path = ServiceUtil.getWorkDirRealPath();

		String realPath = new StringBuilder(path).append(FILE_SEP).append(taskWorkDirPath).toString();

		return ServerUtil.deleteDirNoRecursion(realPath);
	}

	/**
	 * ʵ���ļ�����
	 * 
	 * @param sourceFile
	 * @param targetFile
	 * @return
	 * @throws Exception
	 */
	public static long copyForChannel(File sourceFile, File targetFile) throws IOException {
		long time = new Date().getTime();
		int length = 2097152;
		FileInputStream in = new FileInputStream(sourceFile);
		FileOutputStream out = new FileOutputStream(targetFile);
		FileChannel inFileChannel = in.getChannel();
		FileChannel outFileChannel = out.getChannel();
		ByteBuffer b = null;
		while (true) {
			if (inFileChannel.position() == inFileChannel.size()) {
				inFileChannel.close();
				outFileChannel.close();
				return new Date().getTime() - time;
			}
			if ((inFileChannel.size() - inFileChannel.position()) < length) {
				length = (int) (inFileChannel.size() - inFileChannel.position());
			} else {
				length = 2097152;
			}

			b = ByteBuffer.allocateDirect(length);
			inFileChannel.read(b);
			b.flip();
			outFileChannel.write(b);
			outFileChannel.force(false);
		}
	}

	/**
	 * 
	 * @return
	 */
	public static AbstractJingWeiCore getJngweiCore() {
		String taskName = JingWeiUtil.getJingweiTaskName();
		AbstractJingWeiCore core = null;
		if (StringUtil.isBlank(taskName)) {
			core = new JingWeiCore();
		} else {
			core = new ServerTaskCore();
		}

		return core;
	}

	public static void main(String[] args) {
		File source = new File("d:/mysql-5.0.56.tar.gz");
		File target = new File("d:/bak.tar.gz");
		// try {
		// long time = ServerUtil.copyForChannel(source, target);
		// System.out.println(time);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		listSpecifFile("D:/", StringUtil.EMPTY_STRING);
	}
}