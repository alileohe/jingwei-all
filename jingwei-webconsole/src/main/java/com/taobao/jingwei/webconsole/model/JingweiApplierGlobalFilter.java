package com.taobao.jingwei.webconsole.model;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.applier.ApplierFilterNode;
import com.taobao.jingwei.common.node.applier.EventFilterNode;

public class JingweiApplierGlobalFilter {
	private boolean enableApplierGlobalFilter;
	private String appGlobalDbRegex;
	private String appGlobalTabRegex;
	private String appGlobalFilterString;
	private boolean appGlobalInsert;
	private boolean appGlobalUpdate;
	private boolean appGlobalDelete;
	private boolean appGlobalColumnFilterAdvMode;
	private String appGlobalSourceCode;

	public String toJSONString() throws JSONException {
		ApplierFilterNode node = new ApplierFilterNode();
		if (StringUtil.isBlank(this.getAppGlobalDbRegex())) {
			throw new NullPointerException("Applier全局过滤：库名表达式不能为空");
		}
		if (StringUtil.isBlank(this.getAppGlobalTabRegex())) {
			throw new NullPointerException("Applier全局过滤：表名表达式不能为空");
		}
		node.setSrcSchemaReg(this.getAppGlobalDbRegex());
		node.setSrcTableReg(this.getAppGlobalTabRegex());

		EventFilterNode event = new EventFilterNode();
		event.setIncludeInsert(this.isAppGlobalInsert());
		event.setIncludeUpdate(this.isAppGlobalUpdate());
		event.setIncludeDelete(this.isAppGlobalDelete());
		if (this.isAppGlobalColumnFilterAdvMode()) {
			event.setSourceCode(this.getAppGlobalSourceCode());
		} else {
			if (StringUtil.isNotBlank(this.getAppGlobalFilterString())) {
				Map<String, HashMap<String, EventFilterNode.ColumnFilterConditionNode>> condition = JingweiModelHelper
						.columnFilterStringToJson(this
								.getAppGlobalFilterString());
				event.setConditions(condition);
			}
		}

		node.setEventFilterNode(event);
		return node.toJSONString();
	}

	public boolean isEnableApplierGlobalFilter() {
		return enableApplierGlobalFilter;
	}

	public void setEnableApplierGlobalFilter(boolean enableApplierGlobalFilter) {
		this.enableApplierGlobalFilter = enableApplierGlobalFilter;
	}

	public String getAppGlobalDbRegex() {
		return appGlobalDbRegex;
	}

	public void setAppGlobalDbRegex(String appGlobalDbRegex) {
		this.appGlobalDbRegex = appGlobalDbRegex;
	}

	public String getAppGlobalTabRegex() {
		return appGlobalTabRegex;
	}

	public void setAppGlobalTabRegex(String appGlobalTabRegex) {
		this.appGlobalTabRegex = appGlobalTabRegex;
	}

	public String getAppGlobalFilterString() {
		return appGlobalFilterString;
	}

	public void setAppGlobalFilterString(String appGlobalFilterString) {
		this.appGlobalFilterString = appGlobalFilterString;
	}

	public boolean isAppGlobalInsert() {
		return appGlobalInsert;
	}

	public void setAppGlobalInsert(boolean appGlobalInsert) {
		this.appGlobalInsert = appGlobalInsert;
	}

	public boolean isAppGlobalUpdate() {
		return appGlobalUpdate;
	}

	public void setAppGlobalUpdate(boolean appGlobalUpdate) {
		this.appGlobalUpdate = appGlobalUpdate;
	}

	public boolean isAppGlobalDelete() {
		return appGlobalDelete;
	}

	public void setAppGlobalDelete(boolean appGlobalDelete) {
		this.appGlobalDelete = appGlobalDelete;
	}

	public boolean isAppGlobalColumnFilterAdvMode() {
		return appGlobalColumnFilterAdvMode;
	}

	public void setAppGlobalColumnFilterAdvMode(
			boolean appGlobalColumnFilterAdvMode) {
		this.appGlobalColumnFilterAdvMode = appGlobalColumnFilterAdvMode;
	}

	public String getAppGlobalSourceCode() {
		return appGlobalSourceCode;
	}

	public void setAppGlobalSourceCode(String appGlobalSourceCode) {
		this.appGlobalSourceCode = appGlobalSourceCode;
	}
}
