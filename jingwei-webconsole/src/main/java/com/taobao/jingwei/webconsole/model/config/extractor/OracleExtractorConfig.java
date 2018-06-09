package com.taobao.jingwei.webconsole.model.config.extractor;

import com.taobao.jingwei.common.node.extractor.AbstractExtractorNode;
import com.taobao.jingwei.common.node.extractor.OracleExtractorNode;
import com.taobao.jingwei.common.node.type.ExtractorType;

/**
 * @desc 
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Dec 19, 2012 6:06:45 PM
 */

public class OracleExtractorConfig extends ExtractorConfig {

	private static final long serialVersionUID = -7556847335057026778L;

	@Override
	public AbstractExtractorNode getExtarctorNode() {
		OracleExtractorNode node = new OracleExtractorNode();
		node.setExtractorData(this.getProps());
		return node;
	}

	@Override
	public ExtractorType getExtractorType() {

		return ExtractorType.ORACLE_EXTRACTOR;
	}
}
