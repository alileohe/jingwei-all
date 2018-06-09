package com.taobao.jingwei.core.loader;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.node.applier.EventFilterNode;
import com.taobao.jingwei.common.node.applier.EventFilterNode.ColumnFilterConditionNode;
import com.taobao.jingwei.common.node.applier.MultiMetaApplierNode;
import com.taobao.jingwei.common.node.applier.SubMetaApplierNode;
import com.taobao.jingwei.common.node.extractor.BinLogExtractorNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.core.internal.applier.MultiMetaApplier;
import com.taobao.jingwei.core.kernel.JingWeiCore;
import org.json.JSONException;

import java.io.InputStream;
import java.util.*;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 21, 2012 2:42:28 PM
 */

public class MultiMetaApplierLoader {

	private static final String IC_SUB_META_TEST_1 = "IC_SUB_META_TEST_1";

	private static final String IC_SCHEMA_REG = "^(icdb)[0-9]{1,2}$";
	private static final String IC_SCHEMA = "icdb0";

	private static final String AUCTION_TABLE_REG = "^(auction_auctions){1}_{1}[0-9]{4}$";
	private static final String SKU_TABLE_REG = "^(sku){1}_{1}[0-9]{4}$";

	private static final String AUCTION_TABLE = "auction_auctions";

	private static final String IC_SUB_META_TEST_2 = "IC_SUB_META_TEST_2";

	private static final String IC_FILTER_INCLUDE_COLUMN_1 = "title";
	private static final String IC_FILTER_INCLUDE_COLUMN_2 = "sync_version";
	private static final String IC_FILTER_INCLUDE_COLUMN_3 = "auction_status";

	private static final String IC_FILTER_EXCLUDE_COLUMN_1 = "title";
	private static final boolean IC_FILTER_INCLUDE_RULE = true;

	private static final String NAME = "MULTI_META_TEST";
	private static final int MAX_EVENT_SIZE = 4096;

	public static void main(String[] args) throws Exception {
		//׼��SyncTaskNode
		SyncTaskNode syncTaskNode = new SyncTaskNode();
		syncTaskNode.setName(NAME);
		syncTaskNode.setTaskInstanceCount(1);
		syncTaskNode.setUseLastPosition(false);
		syncTaskNode.setExtractorType(ExtractorType.BINLOG_EXTRACTOR);
		syncTaskNode.setApplierType(ApplierType.MULTI_META_APPLIER);
		//����replicator.properties
		InputStream ips = MultiMetaApplierLoader.class.getClassLoader().getResourceAsStream("replicator.properties");
		BinLogExtractorNode binLogExtractorNode = new BinLogExtractorNode();
		binLogExtractorNode.getConf().load(ips);
		syncTaskNode.setExtractorData(binLogExtractorNode.toJSONString());

		//��ʼ��ZK
		ZkConfig zkConfig = new ZkConfig("10.232.37.114:2181,10.232.37.112:2181,10.232.102.191:2181");
		zkConfig.setZkSessionTimeoutMs(15000);
		ZkConfigManager zkConfigManager = new ZkConfigManager();
		zkConfigManager.setZkConfig(zkConfig);
		zkConfigManager.init();

		MultiMetaApplierLoader loader = new MultiMetaApplierLoader();
		//MultiMetaApplier multiMetaApplier = loader.getMultiMetaApplier();

		JingWeiCore jingWeiCore = new JingWeiCore();

		//jingWeiCore.setApplier(loader.getMultiMetaApplierFromZk(zkConfigManager));
		jingWeiCore.setApplier(loader.getMultiMetaApplier());
		jingWeiCore.setConfigManager(zkConfigManager);
		jingWeiCore.setSyncTaskNode(syncTaskNode);

		//syncTaskNode.setApplierData(loader.getMultiMetaApplierNode().toJSONString());
		String path = JingWeiConstants.JINGWEI_TASK_ROOT_PATH + JingWeiConstants.ZK_PATH_SEP + NAME;
		zkConfigManager.publishOrUpdateData(path, syncTaskNode.toJSONString(), true);

		jingWeiCore.init();
		System.out.println("============jingWeiCore Start============");
	}

	private SubMetaApplierNode getSubMetaApplierNode(String topic, String shardColumn, String srcTableReg) {
		SubMetaApplierNode subMetaApplierNode = new SubMetaApplierNode();

		subMetaApplierNode.setSplitTxEvent(false);
		subMetaApplierNode.setMetaTopic(topic);
		subMetaApplierNode.setFailContinue(false);
		subMetaApplierNode.setName(NAME);
		subMetaApplierNode.setMaxEventSize(MAX_EVENT_SIZE);
		subMetaApplierNode.setShardColumn(shardColumn);

		subMetaApplierNode.setSrcSchemaReg(IC_SCHEMA_REG);
		subMetaApplierNode.setSrcTableReg(srcTableReg);

		return subMetaApplierNode;
	}

	private SubMetaApplierNode getSubMetaApplierNodeForJingweiTestDb(String topic, String shardColumn,
			String srcTableReg) {
		SubMetaApplierNode subMetaApplierNode = new SubMetaApplierNode();

		subMetaApplierNode.setSplitTxEvent(false);
		subMetaApplierNode.setMetaTopic(topic);
		subMetaApplierNode.setFailContinue(false);
		subMetaApplierNode.setName(NAME);
		subMetaApplierNode.setMaxEventSize(MAX_EVENT_SIZE);
		subMetaApplierNode.setShardColumn(shardColumn);

		subMetaApplierNode.setSrcSchemaReg("^jingwei_test_1$");
		subMetaApplierNode.setSrcTableReg(srcTableReg);

		return subMetaApplierNode;
	}
	//qihao
	@SuppressWarnings("unused")
	private MultiMetaApplier getMultiMetaApplierFromZk(ZkConfigManager zkConfigManager) throws JSONException {

		String path = JingWeiConstants.JINGWEI_TASK_ROOT_PATH + JingWeiConstants.ZK_PATH_SEP + "yuanfeng";
		String syncTaskNodeString = zkConfigManager.getData(path);

		SyncTaskNode syncTaskNode = new SyncTaskNode();

		syncTaskNode.jsonStringToNodeSelf(syncTaskNodeString);

		String applierData = syncTaskNode.getApplierData();

		MultiMetaApplierNode multiMetaApplierNode = new com.taobao.jingwei.common.node.applier.MultiMetaApplierNode();
		multiMetaApplierNode.jsonStringToNodeSelf(applierData);

		MultiMetaApplier multiMetaApplier = new MultiMetaApplier(multiMetaApplierNode);

		return multiMetaApplier;
	}

	private MultiMetaApplier getMultiMetaApplier() throws JSONException {

		MultiMetaApplier multiMetaApplier = new MultiMetaApplier(this.getMultiMetaApplierNode());

		return multiMetaApplier;
	}

	private MultiMetaApplierNode getMultiMetaApplierNode() throws JSONException {
		MultiMetaApplierNode muitiMetaApplierNode = new MultiMetaApplierNode();

		// auction
		SubMetaApplierNode acutionNode = this.getSubMetaApplierNode(IC_SUB_META_TEST_1, "title", AUCTION_TABLE_REG);

		// filter
		EventFilterNode eventFilterNode = getEventFilterNode(IC_SCHEMA, AUCTION_TABLE, IC_FILTER_INCLUDE_RULE);

		acutionNode.setEventFilterData(eventFilterNode.toJSONString());

		// sku
		SubMetaApplierNode skuNode = this.getSubMetaApplierNode(IC_SUB_META_TEST_2, "type", SKU_TABLE_REG);

		// jingwei_test_1
		SubMetaApplierNode jingweiTestNode = this.getSubMetaApplierNodeForJingweiTestDb(IC_SUB_META_TEST_1,
				"GIFT_RECEIVER_ID", "^(jingwei_test_00){1}$");

		List<SubMetaApplierNode> subMetaApplierNodes = new ArrayList<SubMetaApplierNode>();

		//subMetaApplierNodes.add(acutionNode);
		subMetaApplierNodes.add(skuNode);
		subMetaApplierNodes.add(jingweiTestNode);

		muitiMetaApplierNode.setSubMetaApplierNodes(subMetaApplierNodes);

		return muitiMetaApplierNode;
	}

	private EventFilterNode getEventFilterNode(String schema, String table, boolean useIncludeRule) {
		EventFilterNode eventFilterNode = new EventFilterNode();

		Map<String, HashMap<String, ColumnFilterConditionNode>> conditions = new HashMap<String, HashMap<String, ColumnFilterConditionNode>>();

		HashMap<String, ColumnFilterConditionNode> tableConditcions = new HashMap<String, EventFilterNode.ColumnFilterConditionNode>();

		tableConditcions.put(table, this.getColumnFilterConditionNode(table, useIncludeRule));

		conditions.put(schema, tableConditcions);

		eventFilterNode.setConditions(conditions);

		return eventFilterNode;
	}

	private ColumnFilterConditionNode getColumnFilterConditionNode(String table, boolean useIncludeRule) {
		ColumnFilterConditionNode columnFilterConditionNode = new ColumnFilterConditionNode();

		columnFilterConditionNode.setUseIncludeRule(useIncludeRule);
		columnFilterConditionNode.setIncludeColumns(this.getIncludeColumnSet(table));
		columnFilterConditionNode.setExcludeColumns(this.getExcludeColumnSet(table));

		return columnFilterConditionNode;
	}

	private Set<String> getIncludeColumnSet(String table) {
		Set<String> set = new HashSet<String>();
		set.add(IC_FILTER_INCLUDE_COLUMN_1);
		set.add(IC_FILTER_INCLUDE_COLUMN_2);
		set.add(IC_FILTER_INCLUDE_COLUMN_3);
		return set;
	}

	private Set<String> getExcludeColumnSet(String table) {
		Set<String> set = new HashSet<String>();
		set.add(IC_FILTER_EXCLUDE_COLUMN_1);
		return set;
	}
}
