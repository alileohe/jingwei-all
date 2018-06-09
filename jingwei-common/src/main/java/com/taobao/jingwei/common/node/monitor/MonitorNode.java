package com.taobao.jingwei.common.node.monitor;

import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.node.AbstractNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * ��Ӧ/jingwei/monitors/**monitor
 *
 * @author shuohailhl
 */
public class MonitorNode extends AbstractNode {

	/** ���ű����� */
	private volatile List<String> tasks = Collections.emptyList();

	/**
	 * e.g. /jingwei/monitors/monitors/**monitor
	 * @param monitorName
	 * @return
	 */
	public static String getDataIdOrNodePath(String monitorName) {
		StringBuilder sb = new StringBuilder(JINGWEI_MONITOR_ROOT_PATH);
		sb.append(ZK_PATH_SEP).append(JINGWEI_MONITOR_MONITORS_NODE_NAME);
		sb.append(ZK_PATH_SEP).append(monitorName);

		return sb.toString();
	}

	@Override
	public boolean isPersistent() {
		return false;
	}

	@Override
	public String getDataIdOrNodePath() {
		return MonitorNode.getDataIdOrNodePath(this.getName());
	}

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
		jsonObject.put("tasks", this.tasks != null ? new JSONArray(this.tasks) : new JSONArray());
	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
		this.setTasks(JingWeiUtil.<String> jsonArray2List(jsonObject.getJSONArray("tasks")));
	}

	public List<String> getTasks() {
		return tasks;
	}

	public void setTasks(List<String> tasks) {
		this.tasks = tasks;
	}
}
