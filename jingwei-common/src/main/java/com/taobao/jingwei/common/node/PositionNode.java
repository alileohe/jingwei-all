package com.taobao.jingwei.common.node;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**   
 * <p>description:<p> ������λ��ļ�¼��ZK�ڵ�
 *
 * @{#} ComitPositionNode.java Create on Dec 9, 2011 10:30:24 AM   
 *   
 * Copyright (c) 2011 by qihao.
 *
 *@author <a href="mailto:qihao@taobao.com">qihao</a> 
 *@version 1.0   
 */
public class PositionNode extends AbstractNode {

	private volatile String ownerDataIdOrPath;

	private volatile String position;

	private volatile Date timestamp;

	/**
	 * json�洢����key����
	 */
	private final static String POSITION_KEY = "position";
	private final static String TIMESTAMP_KEY = "timestamp";

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
		jsonObject.put(POSITION_KEY, StringUtil.defaultIfBlank(this.getPosition()));
		if (null != timestamp) {
			jsonObject.put(TIMESTAMP_KEY, JingWeiUtil.date2String(timestamp));
		} else {
			jsonObject.put(TIMESTAMP_KEY, StringUtil.EMPTY_STRING);
		}
	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
		this.setPosition(StringUtil.defaultIfBlank(jsonObject.getString(POSITION_KEY), null));
		String timestamp = jsonObject.getString(TIMESTAMP_KEY);
		if (StringUtil.isNotBlank(timestamp)) {
			this.setTimestamp(JingWeiUtil.string2Date(timestamp));
		}
	}

	/** 
	 * zk�洢·��Ϊ���磺
	 * /jingwei-v2/tasks/��������/lastComit   
	 * ����/jingwei-v2/tasks/�������ƣ�ΪownerDataIdOrPath
	 */
	public String getDataIdOrNodePath() {
		return ownerDataIdOrPath + ZK_PATH_SEP + JINGWEI_TASK_POSITION_NODE_NAME;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getOwnerDataIdOrPath() {
		return ownerDataIdOrPath;
	}

	public void setOwnerDataIdOrPath(String ownerDataIdOrPath) {
		this.ownerDataIdOrPath = ownerDataIdOrPath;
	}

	@Override
	public boolean isPersistent() {
		return true;
	}
}
