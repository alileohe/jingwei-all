package com.taobao.jingwei.webconsole.model.config.extractor;

import com.taobao.jingwei.common.node.extractor.AbstractExtractorNode;
import com.taobao.jingwei.common.node.extractor.BinLogExtractorNode;
import com.taobao.jingwei.common.node.type.ExtractorType;

/**
 * @desc 
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Dec 20, 2012 1:21:17 PM
 */

public class MysqlExtractorConfig extends ExtractorConfig {

	private static final long serialVersionUID = 1145203286414268373L;

	private volatile boolean autoSwitch;

	private volatile String switchPolicy;

	private volatile String groupName;

	public boolean isAutoSwitch() {
		return autoSwitch;
	}

	public void setAutoSwitch(boolean autoSwitch) {
		this.autoSwitch = autoSwitch;
	}

	public String getSwitchPolicy() {
		return switchPolicy;
	}

	public void setSwitchPolicy(String switchPolicy) {
		this.switchPolicy = switchPolicy;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public BinLogExtractorNode getMysqlExtractorNode(MysqlExtractorConfig mysqlExtractorConfig) {
		String confs = mysqlExtractorConfig.getProps();
		BinLogExtractorNode node = new BinLogExtractorNode(confs);

		node.setAutoSwitch(mysqlExtractorConfig.isAutoSwitch());
		node.setGroupName(mysqlExtractorConfig.getGroupName());
		node.setSwitchPolicy(mysqlExtractorConfig.getSwitchPolicy());

		return node;
	}

	public AbstractExtractorNode getExtarctorNode() {
		String confs = this.getProps();
		BinLogExtractorNode node = new BinLogExtractorNode(confs);

		node.setAutoSwitch(this.isAutoSwitch());
		node.setGroupName(this.getGroupName());
		node.setSwitchPolicy(this.getSwitchPolicy());

		return node;
	}

	@Override
	public ExtractorType getExtractorType() {

		return ExtractorType.BINLOG_EXTRACTOR;
	}

}
