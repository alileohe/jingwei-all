package com.taobao.jingwei.webconsole.model.config.applier;

import com.taobao.jingwei.common.node.applier.AbstractApplierNode;
import com.taobao.jingwei.common.node.type.ApplierType;

/**
 * @desc 
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Dec 20, 2012 4:59:39 PM
 */

public class CustomerApplierConfig extends ApplierConfig {

	private static final long serialVersionUID = 1L;

	@Override
	public AbstractApplierNode getApplierNode() {
		return null;
	}

	@Override
	public ApplierType getApplierType() {
		return ApplierType.CUSTOM_APPLIER;
	}

}
