package com.taobao.jingwei.core.loader;

import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.node.applier.AndorCommandApplierNode;
import com.taobao.jingwei.common.node.extractor.MetaExtractorNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.core.kernel.JingWeiCore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndorCommandApplierLoader {
	private static String TASK_NAME = "andorCommandApplierTest";

	private static String ZK_HOSTS = "10.232.37.114:2181,10.232.37.112:2181,10.232.102.191:2181";
	private static String metaTopic = "andor-applye-test"; 
	private static String metaGroup = "andor-command-load-tesddtdd1";
	private static String appName = "ANDOR_MANUL_TEST_APP";
	private static Map<String, Map<String, List<String>>> cascadeIndexNameMap;

	public static void main(String[] args) throws Exception {
		
		cascadeIndexNameMap = new HashMap<String, Map<String,List<String>>>();
		
		Map<String, List<String>> indexMap_MULTY_TABLE_SINGLE_DB = new HashMap<String, List<String>>();
		indexMap_MULTY_TABLE_SINGLE_DB.put("MULTY_TABLE_SINGLE_DB._SCHOOL", Arrays.asList(new String[] {"SCHOOL","ID"}));
		indexMap_MULTY_TABLE_SINGLE_DB.put("MULTY_TABLE_SINGLE_DB._UID", Arrays.asList(new String[] {"UID","ID"}));
		indexMap_MULTY_TABLE_SINGLE_DB.put("MULTY_TABLE_SINGLE_DB._ID", Arrays.asList(new String[] {"SCHOOL","ID"}));
		cascadeIndexNameMap.put("MULTY_TABLE_SINGLE_DB", indexMap_MULTY_TABLE_SINGLE_DB);
		
		Map<String, List<String>> indexMap_MULTY_TABLE_SINGLE_DB_UID = new HashMap<String, List<String>>();
		indexMap_MULTY_TABLE_SINGLE_DB_UID.put("MULTY_TABLE_SINGLE_DB_UID._SCHOOL", Arrays.asList(new String[] {"SCHOOL","ID"}));
		indexMap_MULTY_TABLE_SINGLE_DB_UID.put("MULTY_TABLE_SINGLE_DB_UID._UID", Arrays.asList(new String[] {"UID","ID"}));
		indexMap_MULTY_TABLE_SINGLE_DB_UID.put("MULTY_TABLE_SINGLE_DB_UID._ID", Arrays.asList(new String[] {"SCHOOL","ID"}));
		cascadeIndexNameMap.put("MULTY_TABLE_SINGLE_DB_UID", indexMap_MULTY_TABLE_SINGLE_DB_UID);
		
		
		SyncTaskNode syncTaskNode = new SyncTaskNode();
		syncTaskNode.setName(TASK_NAME);
		syncTaskNode.setTaskInstanceCount(1);
		syncTaskNode.setExtractorType(ExtractorType.META_EXTRACTOR);
		syncTaskNode.setApplierType(ApplierType.ANDOR_COMMAND_APPLIER);
		syncTaskNode.setUseLastPosition(false);

		
		MetaExtractorNode metaExtraNode = new MetaExtractorNode();
		metaExtraNode.setMetaTopic(metaTopic);
		metaExtraNode.setMetaGroup(metaGroup);
		metaExtraNode.setMaxMessageSize(1024);
		syncTaskNode.setExtractorData(metaExtraNode.toJSONString());
		
		
		AndorCommandApplierNode andorCommNode = new AndorCommandApplierNode();
		andorCommNode.setAppName(appName);
		andorCommNode.setCascadeIndexNameMap(cascadeIndexNameMap);
		syncTaskNode.setApplierData(andorCommNode.toJSONString());
		

		ZkConfig zkConfig = new ZkConfig(ZK_HOSTS);
		zkConfig.setZkSessionTimeoutMs(5000);
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();
		//��SyncTaskNode������zk��
		//zkConfigManager.publishOrUpdatePersistentData(syncTaskNode.getDataIdOrNodePath(), syncTaskNode.toJSONString());

		//����jingweCore
		JingWeiCore jingWeiCore = new JingWeiCore();
		jingWeiCore.setConfigManager(zkConfigManager);
		jingWeiCore.setSyncTaskNode(syncTaskNode);
		jingWeiCore.init();
		System.out.println("============jingWeiCore Start============");
	}
}
