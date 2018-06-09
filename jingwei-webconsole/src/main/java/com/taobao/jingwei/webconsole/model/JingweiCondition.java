/**
 * 
 */
package com.taobao.jingwei.webconsole.model;

import java.io.Serializable;

import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;

/**
 * 精卫查询条件
 * 
 * @author qingren 2011-9-6
 */
public class JingweiCondition implements Serializable ,JingweiWebConsoleConstance{
	private static final long serialVersionUID = 4621670276698615268L;

	private String column;
	private int pageSize = JINGWEI_DEFAULT_ITEM_CNT;
	private int currentPage = 1;
	private int totalPage = 1;
	private int recordCnt = 0;
	private boolean asc = true;

	private String agentName;
	private String taskName;
	private String appName;
	private String schemaName;
	private String logicTableName;
	private String taskType;

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getLogicTableName() {
		return logicTableName;
	}

	public void setLogicTableName(String logicTableName) {
		this.logicTableName = logicTableName;
	}

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public boolean isAsc() {
		return asc;
	}

	public void setAsc(boolean asc) {
		this.asc = asc;
	}

	public int getRecordCnt() {
		return recordCnt;
	}

	public void setRecordCnt(int recordCnt) {
		this.recordCnt = recordCnt;
	}

}
