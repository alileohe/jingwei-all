package com.taobao.jingwei.webconsole.model.config.applier;

import java.io.Serializable;

import org.json.JSONException;

import com.taobao.jingwei.common.node.applier.AbstractApplierNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.webconsole.model.config.exception.BatchConfigException;

/**
 * @desc
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Dec 20, 2012 5:14:08 PM
 */

abstract public class ApplierConfig implements Serializable {

	private static final long serialVersionUID = 1376529354303379942L;
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

	abstract public AbstractApplierNode getApplierNode() throws JSONException, BatchConfigException;

	abstract public ApplierType getApplierType();

}
