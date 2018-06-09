package com.taobao.jingwei.server;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ZkDataPublisher {

	private ConfigManager configManager;

	
	public ConfigManager getConfigManager() {
		return configManager;
	}

	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	@SuppressWarnings({ "resource", "deprecation" })
	public static void main(String[] args) throws IOException {
		ZkDataPublisher zkDataPublisher = new ZkDataPublisher();
		if (!zkDataPublisher.checkAndParserBootPram(args)) {
			return;
		}
		ConfigManager configManager = zkDataPublisher.getConfigManager();
		
		File file = new File("d:/zkData.txt");
		DataInputStream is = new DataInputStream(new FileInputStream(file));
		String data = is.readLine(); 
//		String dataPath = "/jingwei-v2/servers/TAOBAO-5DCEBC60/tasks/test-task4/operate";
//		String dataPath = "/jingwei-v2/servers/TAOBAO-5DCEBC60/tasks/DAILY-SNS-USER";
//		String dataPath = "/jingwei-v2/servers/TAOBAO-5DCEBC60/tasks/DAILY-SNS-USER/operate";
		String dataPath = "/jingwei-v2/tasks/DAILY-SNS-USER";
		try {
//			configManager.publishData(dataPath, data, true);
			configManager.publishOrUpdateData(dataPath, data, true);
//			configManager.delete("/jingwei-v2/servers/TAOBAO-5DCEBC60/tasks/test-task4");
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	public boolean checkAndParserBootPram(String[] args) {
		boolean checkRet = false;
		if (args.length < 1) {
			return checkRet;
		}
		String confPath = args[0];
		if (StringUtil.isBlank(confPath)) {
			return checkRet;
		}
		// ƴװ������server�������ļ�·��
		File configFile = new File(confPath);
		if (!configFile.exists()) {
			return checkRet;
		}
		

		// ��ʼ��ZK������
		ZkConfig zkConfig = ZkConfig.getZkConfigFromFile(confPath);
		if (null == zkConfig) {
			return checkRet;
		}
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();
		this.setConfigManager(zkConfigManager);

		return true;
	}

}
