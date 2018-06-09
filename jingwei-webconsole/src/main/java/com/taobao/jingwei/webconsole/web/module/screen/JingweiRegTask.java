package com.taobao.jingwei.webconsole.web.module.screen;

import com.alibaba.citrus.turbine.Context;
import com.taobao.jingwei.common.node.applier.AndorCommandApplierNode;
import com.taobao.jingwei.common.node.applier.MetaApplierNode;
import com.taobao.jingwei.common.node.applier.SubMetaApplierNode;
import com.taobao.jingwei.common.node.extractor.MetaExtractorNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.common.JingweiTypeHelper;
import com.taobao.jingwei.webconsole.model.JingweiBinlogExtractor;

public class JingweiRegTask {
	public void execute(Context context) {
		SyncTaskNode taskNode = new SyncTaskNode();
		context.put("taskNode", taskNode);

		MetaApplierNode metaApplierNode = new MetaApplierNode();
		context.put("metaApplierNode", metaApplierNode);

		MetaExtractorNode metaExtractorNode = new MetaExtractorNode();
		context.put("metaExtractorNode", metaExtractorNode);

		SubMetaApplierNode multiMetaApplierNode = new SubMetaApplierNode();
		context.put("multiMetaApplierNode", multiMetaApplierNode);

		AndorCommandApplierNode andorCommandApplierNode = new AndorCommandApplierNode();
		context.put("andorCommandApplierNode", andorCommandApplierNode);

		context.put("binData", JingweiBinlogExtractor.getTemplate());
		context.put("newBinData", JingweiBinlogExtractor.getNewTemplate());
		context.put("switchPolicyType", JingweiTypeHelper.getDBSyncSwitchPolicyType());

		context.put("hosts", JingweiZkConfigManager.getKeys());
		context.put("extractorType", JingweiTypeHelper.getExtractorType());
		context.put("applierType", JingweiTypeHelper.getApplierType());
		context.put("dbType", JingweiTypeHelper.getDbType());
		context.put("compressionType", JingweiTypeHelper.getCompressionType());

		context.put("batchCreateTypes", JingweiTypeHelper.getBatchCreateType());
	}
}
