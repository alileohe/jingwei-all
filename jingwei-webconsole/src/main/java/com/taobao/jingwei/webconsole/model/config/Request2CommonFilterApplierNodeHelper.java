package com.taobao.jingwei.webconsole.model.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;
import org.json.JSONException;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.applier.ApplierFilterNode;
import com.taobao.jingwei.common.node.applier.EventFilterNode;
import com.taobao.jingwei.common.node.applier.EventFilterNode.ColumnFilterConditionNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;

public class Request2CommonFilterApplierNodeHelper {
	private static Log log = LogFactory.getLog(Request2CommonFilterApplierNodeHelper.class);

	public static void updateCommonFilterApplierConfig(HttpServletRequest request, SyncTaskNode syncTaskNode)
			throws JSONException, InvalidFileFormatException {
		ApplierFilterNode node = new ApplierFilterNode();

		Boolean commonFilterEnabled = ConfigUtil.isChecked(request, "commonFilterEnabled");

		if (commonFilterEnabled) {
			String commonFilterSrcSchemaReg = request.getParameter("commonFilterSrcSchemaReg");
			String commonFilterSrcTableReg = request.getParameter("commonFilterSrcTableReg");
			Boolean commonFilterInsert = ConfigUtil.isChecked(request, "commonFilterInsert");
			Boolean commonFilterUpdate = ConfigUtil.isChecked(request, "commonFilterUpdate");
			Boolean commonFilterDelete = ConfigUtil.isChecked(request, "commonFilterDelete");

			Boolean commonFilterChecked = ConfigUtil.isChecked(request, "commonFilterCheckBox");
			String commonFilterConfig = request.getParameter("commonFilterConfig");
			String commonFilterDynaCode = request.getParameter("commonFilterDynaCode");

			node.setSrcSchemaReg(commonFilterSrcSchemaReg);
			node.setSrcTableReg(commonFilterSrcTableReg);

			EventFilterNode eventFilterNode = new EventFilterNode();
			eventFilterNode.setIncludeDelete(commonFilterDelete);
			eventFilterNode.setIncludeUpdate(commonFilterUpdate);
			eventFilterNode.setIncludeInsert(commonFilterInsert);

			if (commonFilterChecked) {
				getEventFilterNode(eventFilterNode, commonFilterConfig, commonFilterDynaCode);
			}

			node.setEventFilterNode(eventFilterNode);
			syncTaskNode.setApplierFilterData(node.toJSONString());
		} else {
			syncTaskNode.setApplierFilterData(StringUtil.EMPTY_STRING);
		}

	}

	public static EventFilterNode getEventFilterNode(EventFilterNode eventFilterNode, String mutilColumnFilterConfig,
			String dynaCode) throws InvalidFileFormatException {

		if (StringUtil.isNotBlank(dynaCode)) {
			eventFilterNode.setSourceCode(dynaCode);
		} else {
			Map<String, HashMap<String, ColumnFilterConditionNode>> conditions = getColumnFilterCondition(mutilColumnFilterConfig);
			eventFilterNode.setConditions(conditions);
		}

		return eventFilterNode;
	}

	private static Map<String, HashMap<String, ColumnFilterConditionNode>> getColumnFilterCondition(String columnConfig)
			throws InvalidFileFormatException {

		Map<String, HashMap<String, ColumnFilterConditionNode>> conditions = new HashMap<String, HashMap<String, ColumnFilterConditionNode>>();

		InputStream is = ConfigUtil.string2InputStream(columnConfig);

		Ini ini = new Ini();
		try {
			ini.load(is);

			Section columnFilterConfig = ini.get("columnFilter");
			ColumnFilterConditionNode columnFilterConditionNode = new ColumnFilterConditionNode();

			// 使用排除还是包含
			Boolean useInclude = Boolean.valueOf(columnFilterConfig.get("useInclude"));

			columnFilterConditionNode.setUseIncludeRule(useInclude);

			// 字段列表
			String columns = columnFilterConfig.get("filterColumns");
			if (useInclude) {
				columnFilterConditionNode.setIncludeColumns(ConfigUtil.commaSepString2Set(columns));
			} else {
				columnFilterConditionNode.setExcludeColumns(ConfigUtil.commaSepString2Set(columns));
			}

			// 库名表达式
			String filterColumnSchemaReg = columnFilterConfig.get("filterColumnSchemaReg");

			// 逻辑表名
			String logicTable = columnFilterConfig.get("filterColumnLogicTableName");

			HashMap<String, ColumnFilterConditionNode> map = new HashMap<String, ColumnFilterConditionNode>();

			map.put(logicTable, columnFilterConditionNode);

			conditions.put(filterColumnSchemaReg, map);

		} catch (Exception e) {
			throw new InvalidFileFormatException("配置文件格式错误");
		}

		return conditions;
	}
}
