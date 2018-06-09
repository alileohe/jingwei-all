package com.taobao.jingwei.common.node.applier;

import com.taobao.jingwei.common.node.AbstractNode;

/**
 * Class AbstractApplierNode
 *
 * @author qihao <dqylyln@gmail.com>
 * @since 11-11-15
 */
public abstract class AbstractApplierNode extends AbstractNode {

	private String applierData;

	private boolean replace;

	private boolean failContinue = true;

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public String getDataIdOrNodePath() {
		//NO-OP: Applier无需实现该方法
		return null;
	}

	public String getApplierData() {
		return applierData;
	}

	public void setApplierData(String applierData) {
		this.applierData = applierData;
	}

	public boolean isReplace() {
		return replace;
	}

	public void setReplace(boolean replace) {
		this.replace = replace;
	}

	public boolean isFailContinue() {
		return failContinue;
	}

	public void setFailContinue(boolean failContinue) {
		this.failContinue = failContinue;
	}
}
