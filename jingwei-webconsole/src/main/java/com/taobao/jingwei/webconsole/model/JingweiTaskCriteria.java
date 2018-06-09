package com.taobao.jingwei.webconsole.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.common.lang.StringUtil;

public class JingweiTaskCriteria implements Serializable {
	private static final long serialVersionUID = 669798605933734740L;

	public static final String SESSION_NAME = "jingweiTaskCriteria";
	public static final String SESSION_JSON = "jingweiTaskCriteria_JSON";

	private String taskId = "";
	private String hostName = "";
	private String runStatus = "";
	private Integer extractorType;
	private Integer applierType;

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getRunStatus() {
		return runStatus;
	}

	public void setRunStatus(String runStatus) {
		this.runStatus = runStatus;
	}

	public Integer getExtractorType() {
		return extractorType;
	}

	public void setExtractorType(Integer extractorType) {
		this.extractorType = extractorType;
	}

	public Integer getApplierType() {
		return applierType;
	}

	public void setApplierType(Integer applierType) {
		this.applierType = applierType;
	}

	public void jsonStringToself(String jsonString) throws JSONException {
		if (StringUtil.isBlank(jsonString)) {
			throw new JSONException("constructor JSONObject is empty!");
		}
		JSONObject obj = new JSONObject(jsonString);
		this.setTaskId(obj.getString("taskId"));
		this.setHostName(obj.getString("hostName"));
		this.setRunStatus(obj.getString("runStatus"));
		this.setExtractorType(obj.getString("extractorType") == null ? null
				: obj.getInt("extractorType"));
		this.setApplierType(obj.getString("applierType") == null ? null : obj
				.getInt("applierType"));
	}

	public String toJSONString() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("taskId", StringUtil.defaultIfBlank(this.taskId));
		obj.put("hostName", StringUtil.defaultIfBlank(this.hostName));
		obj.put("runStatus", StringUtil.defaultIfBlank(this.runStatus));
		obj.put("extractorType", this.extractorType == null ? ""
				: this.extractorType);
		obj.put("applierType", this.applierType == null ? "" : this.applierType);
		return obj.toString();
	}

}
