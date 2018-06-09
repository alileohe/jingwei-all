package com.taobao.jingwei.common.config;

import org.I0Itec.zkclient.IZkChildListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public abstract class ChildChangeListener implements IZkChildListener {

	private static Log logger = LogFactory.getLog(ChildChangeListener.class);

	public abstract void handleChild(String parentPath, List<String> currentChilds);

	public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
		String listString = null == currentChilds || currentChilds.isEmpty() ? "[Empty]" : currentChilds.toString();
		logger.warn("[ChildChangeListener] " + parentPath + " childChange:" + listString);
		this.handleChild(parentPath, currentChilds);
	}
}