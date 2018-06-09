package com.taobao.jingwei.webconsole.model.config.extractor;

import java.io.Serializable;

import com.taobao.jingwei.common.node.extractor.AbstractExtractorNode;
import com.taobao.jingwei.common.node.type.ExtractorType;

/**
 * @desc 
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Dec 20, 2012 1:37:52 PM
 */

public abstract class ExtractorConfig implements Serializable {
	private static final long serialVersionUID = -7730958778918313114L;
	protected String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected String props;

	public String getProps() {
		return props;
	}

	public void setProps(String props) {
		this.props = props;
	}

	public abstract AbstractExtractorNode getExtarctorNode();

	public abstract ExtractorType getExtractorType();

}
