package com.taobao.jingwei.common.node.monitor;

import com.taobao.jingwei.common.node.AbstractNode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @desc ��Ӧe.g. /jingwei/monitors/monitors ·�����洢ȫ��ww�澯��sm�澯����
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Aug 18, 2012 7:42:40 AM
 */

public class MonitorParentNode extends AbstractNode {
	/** �����澯���� */
	private boolean isWwToggle;

	/** ���Ÿ澯���� */
	private boolean isSmToggle;

	private final String IS_WW_TOGGLE = "isWwToggle";
	private final String IS_SM_TOGGLE = "isSmToggle";

	public static String getPath() {
		return new StringBuilder(JINGWEI_MONITOR_ROOT_PATH).append(ZK_PATH_SEP)
				.append(JINGWEI_MONITOR_MONITORS_NODE_NAME).toString();
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public String getDataIdOrNodePath() {
		return MonitorParentNode.getPath();
	}

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
		jsonObject.put(IS_SM_TOGGLE, this.isSmToggle());
		jsonObject.put(IS_WW_TOGGLE, this.isWwToggle());
	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
		this.setSmToggle(jsonObject.getBoolean(IS_SM_TOGGLE));
		this.setWwToggle(jsonObject.getBoolean(IS_WW_TOGGLE));
	}

	public boolean isWwToggle() {
		return isWwToggle;
	}

	public void setWwToggle(boolean isWwToggle) {
		this.isWwToggle = isWwToggle;
	}

	public boolean isSmToggle() {
		return isSmToggle;
	}

	public void setSmToggle(boolean isSmToggle) {
		this.isSmToggle = isSmToggle;
	}

}
