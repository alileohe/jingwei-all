package com.taobao.jingwei.webconsole.model.config.extractor;

import com.taobao.jingwei.common.node.extractor.AbstractExtractorNode;
import com.taobao.jingwei.common.node.type.ExtractorType;

/**
 * @desc 
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 8, 2013 1:59:10 PM
 */

public class CustomerExtractorConfig extends ExtractorConfig {

	private static final long serialVersionUID = 6096336559362859893L;

	@Override
	public AbstractExtractorNode getExtarctorNode() {
		return null;
	}

	@Override
	public ExtractorType getExtractorType() {
		return ExtractorType.CUSTOM_EXTRACTOR;
	}

}
