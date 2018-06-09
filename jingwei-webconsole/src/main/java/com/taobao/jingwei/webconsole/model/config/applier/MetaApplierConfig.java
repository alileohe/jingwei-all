package com.taobao.jingwei.webconsole.model.config.applier;

import org.json.JSONException;

import com.taobao.jingwei.common.node.applier.AbstractApplierNode;
import com.taobao.jingwei.common.node.applier.EventFilterNode;
import com.taobao.jingwei.common.node.applier.SubMetaApplierNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.webconsole.model.config.CommonFilterConfig;
import com.taobao.jingwei.webconsole.model.config.exception.BatchConfigException;

/**
 * @desc 其实对应的是 SubMetaApplierNode的配置
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Dec 20, 2012 8:04:26 PM
 */
public class MetaApplierConfig extends ApplierConfig {
	private static final long serialVersionUID = 6547100933269192360L;

	private String metaTopic;
	private String shardColumn;
	private boolean splitTxEvent;
	private int maxEventSize = 6144;
	private long sendTimeOut = 3000L;
	private String compressionType = "NONE";
	private boolean enableColumnFilter;

	private CommonFilterConfig commonFilterConfig;

	//
	public SubMetaApplierNode getSubMetaApplierNode() throws JSONException, BatchConfigException {
		SubMetaApplierNode subMetaApplierNode = new SubMetaApplierNode();

		subMetaApplierNode.setMetaTopic(metaTopic);
		subMetaApplierNode.setShardColumn(shardColumn);
		subMetaApplierNode.setSplitTxEvent(splitTxEvent);
		subMetaApplierNode.setMaxEventSize(maxEventSize);
		subMetaApplierNode.setSendTimeOut(sendTimeOut);
		subMetaApplierNode.setCompressionType(compressionType);
		subMetaApplierNode.setEnableColumnfilter(enableColumnFilter);

		subMetaApplierNode.setSrcSchemaReg(commonFilterConfig.getSrcSchemaReg());
		subMetaApplierNode.setSrcTableReg(commonFilterConfig.getSrcTableReg());

		EventFilterNode eventFilterNode = commonFilterConfig.getEventFilterNode();
		subMetaApplierNode.setEventFilterData(eventFilterNode.toJSONString());

		return subMetaApplierNode;
	}

	public MetaApplierConfig() {

	}

	/**
	 * 从node到config的转换
	 * 
	 * @param subMetaApplierNode
	 */
	public MetaApplierConfig(SubMetaApplierNode subMetaApplierNode) {
		this.setMetaTopic(subMetaApplierNode.getMetaTopic());
		this.setShardColumn(subMetaApplierNode.getShardColumn());
		this.setSplitTxEvent(subMetaApplierNode.isSplitTxEvent());
		this.setMaxEventSize(subMetaApplierNode.getMaxEventSize());
		this.setSendTimeOut(subMetaApplierNode.getSendTimeOut());
		this.setCompressionType(subMetaApplierNode.getCompressionType());
		this.setEnableColumnFilter(subMetaApplierNode.isEnableColumnfilter());

		String eventFilterData = subMetaApplierNode.getEventFilterData();
		EventFilterNode eventNode = new EventFilterNode(eventFilterData);

		this.commonFilterConfig = new CommonFilterConfig(eventNode, subMetaApplierNode.getSrcSchemaReg(),
				subMetaApplierNode.getSrcTableReg());
	}

	public String getMetaTopic() {
		return metaTopic;
	}

	public void setMetaTopic(String metaTopic) {
		this.metaTopic = metaTopic;
	}

	public String getShardColumn() {
		return shardColumn;
	}

	public void setShardColumn(String shardColumn) {
		this.shardColumn = shardColumn;
	}

	public boolean isSplitTxEvent() {
		return splitTxEvent;
	}

	public void setSplitTxEvent(boolean splitTxEvent) {
		this.splitTxEvent = splitTxEvent;
	}

	public int getMaxEventSize() {
		return maxEventSize;
	}

	public void setMaxEventSize(int maxEventSize) {
		this.maxEventSize = maxEventSize;
	}

	public long getSendTimeOut() {
		return sendTimeOut;
	}

	public void setSendTimeOut(long sendTimeOut) {
		this.sendTimeOut = sendTimeOut;
	}

	public String getCompressionType() {
		return compressionType;
	}

	public void setCompressionType(String compressionType) {
		this.compressionType = compressionType;
	}

	public boolean isEnableColumnFilter() {
		return enableColumnFilter;
	}

	public void setEnableColumnFilter(boolean enableColumnFilter) {
		this.enableColumnFilter = enableColumnFilter;
	}

	public CommonFilterConfig getCommonFilterConfig() {
		return commonFilterConfig;
	}

	public void setCommonFilterConfig(CommonFilterConfig commonFilterConfig) {
		this.commonFilterConfig = commonFilterConfig;
	}

	@Override
	public AbstractApplierNode getApplierNode() throws JSONException, BatchConfigException {

		SubMetaApplierNode node = this.getSubMetaApplierNode();
		return node;
	}

	@Override
	public ApplierType getApplierType() {
		return ApplierType.META_APPLIER;
	}

}
