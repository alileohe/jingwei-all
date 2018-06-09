package com.taobao.jingwei.common.node;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**   
 * <p>description:<p> ����������zk�洢�ڵ�
 *
 * @{#} HeartbeatNode.java Create on Dec 12, 2011 7:28:55 PM   
 *   
 * Copyright (c) 2011 by qihao.
 *
 *@author <a href="mailto:qihao@taobao.com">qihao</a> 
 *@version 1.0   
 */
public class HeartbeatNode extends AbstractNode {

	private volatile String ownerDataIdOrPath;

	private volatile Date timestamp = new Date();

	private final static String TIMESTAMP_KEY = "timestamp";

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
		if (null != timestamp) {
			jsonObject.put(TIMESTAMP_KEY, JingWeiUtil.date2String(this.getTimestamp()));
		} else {
			jsonObject.put(TIMESTAMP_KEY, StringUtil.EMPTY_STRING);
		}

	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
		String tmsp = jsonObject.getString(TIMESTAMP_KEY);

		if (StringUtil.isNotBlank(tmsp)) {
			this.setTimestamp(JingWeiUtil.string2Date(tmsp));
		}
	}

	/**
	 * zk�洢·��:/jingwei-v2/tasks/��������/hosts/��������/heartBeat
	 * ����/jingwei-v2/tasks/��������/hosts/�������� ΪownerDataIdOrPath
	 */
	@Override
	public String getDataIdOrNodePath() {
		return ownerDataIdOrPath + ZK_PATH_SEP + JINGWEI_HOST_HEART_BEAT_NODE_NAME;
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	public String getOwnerDataIdOrPath() {
		return ownerDataIdOrPath;
	}

	public void setOwnerDataIdOrPath(String ownerDataIdOrPath) {
		this.ownerDataIdOrPath = ownerDataIdOrPath;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public static void main(String args[]) {
		String str = "2012-07-20 17:28:21.58.4";
		//JSONObject jsonObject = new JSONObject({"timestamp":"2012-07-20 17:28:21.584","desc":"","name":"DAILY-WLB-CODE"});
		Date o = JingWeiUtil.string2Date(str);
		System.out.println(o.toString());
	}
}
