package com.taobao.jingwei.core.kernel;

/**
 *
 * <p>
 * description:独立的jingweiCore不会监听server节点的op
 * 
 * 整个生命周期都有使用者独立控制
 * <p>
 * 
 * @{# JingWeiCore.java Create on Nov 28, 2011 4:30:30 PM
 * 
 *     Copyright (c) 2011 by qihao.
 * 
 * @author <a href="mailto:qihao@taobao.com">qihao</a>
 * @version 1.0
 */
public class JingWeiCore extends AbstractJingWeiCore {

	public void beforeInitProcessor() {
		//NO-OP 普通的JingWeiCore无需实现这个接口
	}

	public void afterInitProcessor() {
		//NO-OP 普通的JingWeiCore无需实现这个接口
	}
}