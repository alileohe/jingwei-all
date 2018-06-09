package com.taobao.jingwei.webconsole.model.config.applier;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.taobao.jingwei.common.node.applier.AbstractApplierNode;
import com.taobao.jingwei.common.node.applier.MultiMetaApplierNode;
import com.taobao.jingwei.common.node.applier.SubMetaApplierNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.webconsole.model.config.exception.BatchConfigException;

/**
 * @desc
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Dec 20, 2012 8:04:26 PM
 */

public class MultiMetaApplierConfig extends ApplierConfig {

	private static final long serialVersionUID = 2812307534742247606L;
	private List<MetaApplierConfig> metaApplierConfigs = new ArrayList<MetaApplierConfig>();

	public MultiMetaApplierConfig() {

	}

	public MultiMetaApplierConfig(MultiMetaApplierNode node) {

		List<SubMetaApplierNode> subMetaApplierNodes = node.getSubMetaApplierNodes();
		for (SubMetaApplierNode subMetaApplierNode : subMetaApplierNodes) {
			MetaApplierConfig metaApplierConfig = new MetaApplierConfig(subMetaApplierNode);
			this.metaApplierConfigs.add(metaApplierConfig);
		}
	}

	public List<MetaApplierConfig> getMetaApplierConfigs() {
		return metaApplierConfigs;
	}

	public void setMetaApplierConfigs(List<MetaApplierConfig> metaApplierConfigs) {
		this.metaApplierConfigs = metaApplierConfigs;
	}

	@Override
	public AbstractApplierNode getApplierNode() throws JSONException, BatchConfigException {

		MultiMetaApplierNode node = new MultiMetaApplierNode();

		List<SubMetaApplierNode> subMetaApplierNodes = new ArrayList<SubMetaApplierNode>();

		for (MetaApplierConfig metaApplierConfig : metaApplierConfigs) {
			SubMetaApplierNode subMetaApplierNode = metaApplierConfig.getSubMetaApplierNode();
			subMetaApplierNodes.add(subMetaApplierNode);
		}

		node.setSubMetaApplierNodes(subMetaApplierNodes);
		return node;
	}

	@Override
	public ApplierType getApplierType() {
		return ApplierType.MULTI_META_APPLIER;
	}
}
