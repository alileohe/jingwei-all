package com.taobao.jingwei.common.node.server;

import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.node.AbstractNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Class ServerNode
 * 
 * @author qihao <dqylyln@gmail.com>
 * @since 11-11-16
 */
public class ServerNode extends AbstractNode {

	/** ������ */
	private volatile List<String> groups = Collections.emptyList();

	/** server�Ͽӵ����� */
	private int executorCount = 0;

	/** server�İ汾 */
	private String version = "";

	/** ��ǰ�û��� */
	private String userName = "";

	private static final String EXECUTOR_COUNT = "executorCount";
	private static final String VERSION = "version";
	private static final String USER_NAME = "userName";

	public static String getDataIdOrPathFromServerName(String serverName) {
		StringBuilder sb = new StringBuilder();
		sb.append(JINGWEI_SERVER_ROOT_PATH).append(JingWeiUtil.ZK_PATH_SEP);
		sb.append(serverName);
		return sb.toString();
	}

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject)
			throws JSONException {
		jsonObject.put("groups", this.groups != null ? new JSONArray(
				this.groups) : new JSONArray());

		jsonObject.put(EXECUTOR_COUNT, this.getExecutorCount());

		jsonObject.put(VERSION, this.getVersion());

		jsonObject.put(USER_NAME, this.getUserName());
	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject)
			throws JSONException {
		this.setGroups(JingWeiUtil.<String> jsonArray2List(jsonObject
				.getJSONArray("groups")));

		if (jsonObject.has(EXECUTOR_COUNT)) {
			this.setExecutorCount(jsonObject.getInt(EXECUTOR_COUNT));
		}

		if (jsonObject.has(VERSION)) {
			this.setVersion(jsonObject.getString(VERSION));
		}

		if (jsonObject.has(USER_NAME)) {
			this.setUserName(jsonObject.getString(USER_NAME));
		}
	}

	@Override
	public String getDataIdOrNodePath() {
		return new StringBuilder(JINGWEI_SERVER_ROOT_PATH).append(ZK_PATH_SEP)
				.append(this.getName()).toString();
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public int getExecutorCount() {
		return executorCount;
	}

	public void setExecutorCount(int executorCount) {
		this.executorCount = executorCount;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
