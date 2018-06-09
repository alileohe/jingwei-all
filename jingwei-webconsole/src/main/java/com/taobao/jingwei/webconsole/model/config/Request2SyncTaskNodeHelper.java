package com.taobao.jingwei.webconsole.model.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;
import org.json.JSONException;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.applier.DataBaseApplierNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode.GroupingSetting;
import com.taobao.jingwei.common.node.type.DBType;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;

/**
 * @desc  从页面的request获取配置对象 
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 11, 2013 8:22:08 PM
 */

public class Request2SyncTaskNodeHelper {
	private static Log log = LogFactory.getLog(Request2SyncTaskNodeHelper.class);

	public static void updateCommonConfig(HttpServletRequest request, SyncTaskNode syncTaskNode) {

		// 多线程开关
		Boolean mutilThreadChecked = ConfigUtil.isChecked(request, "multiThreadCheckBox");
		if (mutilThreadChecked) {
			Boolean mutilThread = ConfigUtil.isChecked(request, "multiThread");
			syncTaskNode.setMultiThread(mutilThread);
		}

		// 队列容量
		Boolean queueCapacityChecked = ConfigUtil.isChecked(request, "queueCapacityCheckBox");
		if (queueCapacityChecked) {
			String str = request.getParameter("queueCapacity");
			if (StringUtil.isBlank(str)) {
				syncTaskNode.setQueueCapacity(1);
			} else {
				syncTaskNode.setQueueCapacity(Integer.valueOf(str));
			}
		}

		// 最大线程数量
		Boolean maxThreadCountChecked = ConfigUtil.isChecked(request, "maxThreadCountCheckBox");
		if (maxThreadCountChecked) {
			String str = request.getParameter("maxThreadCount");
			if (StringUtil.isBlank(str)) {
				syncTaskNode.setMaxThreadCount(1);
			} else {
				syncTaskNode.setMaxThreadCount(Integer.valueOf(str));
			}
		}

		// grouping setting
		Boolean groupingSettingsChecked = ConfigUtil.isChecked(request, "groupingSettingsCheckBox");
		if (groupingSettingsChecked) {
			List<GroupingSetting> groupingSettings = getGroupingSettings(request.getParameter("groupingSettings"));
			syncTaskNode.setGroupingSettings(groupingSettings);
		}

		// java opt
		Boolean javaOptChecked = ConfigUtil.isChecked(request, "javaOptCheckBox");
		if (javaOptChecked) {
			String javaOpt = request.getParameter("javaOpt");
			syncTaskNode.setJavaOpt(javaOpt);
		}

		// 是否使用上次位点
		Boolean useLastPositionChecked = ConfigUtil.isChecked(request, "useLastPositionCheckBox");
		if (useLastPositionChecked) {
			Boolean useLastPosition = ConfigUtil.isChecked(request, "useLastPosition");
			syncTaskNode.setUseLastPosition(useLastPosition);
		}

		// 统计周期
		Boolean statsPeriodChecked = ConfigUtil.isChecked(request, "statsPeriodCheckBox");
		if (statsPeriodChecked) {
			String str = request.getParameter("statsPeriod");
			if (StringUtil.isBlank(str)) {
				syncTaskNode.setStatsPeriod(10000);
			} else {
				syncTaskNode.setStatsPeriod(Integer.valueOf(str));
			}
		}

		// 事务统计周期
		Boolean summaryPeriodChecked = ConfigUtil.isChecked(request, "summaryPeriodCheckBox");
		if (summaryPeriodChecked) {
			String str = request.getParameter("summaryPeriod");
			if (StringUtil.isBlank(str)) {
				syncTaskNode.setSummaryPeriod(10000);
			} else {
				syncTaskNode.setSummaryPeriod(Integer.valueOf(str));
			}
		}

		// 位点提交周期
		Boolean comitLogPeriodChhecked = ConfigUtil.isChecked(request, "comitLogPeriodChheckBox");
		if (comitLogPeriodChhecked) {
			String str = request.getParameter("comitLogPeriod");
			if (StringUtil.isBlank(str)) {
				syncTaskNode.setComitLogPeriod(10000);
			} else {
				syncTaskNode.setComitLogPeriod(Integer.valueOf(str));
			}
		}

		//位点提交数量
		Boolean comitLogCountChecked = ConfigUtil.isChecked(request, "comitLogCountCheckBox");
		if (comitLogCountChecked) {
			String str = request.getParameter("comitLogCount");
			if (StringUtil.isBlank(str)) {
				syncTaskNode.setComitLogPeriod(1000);
			} else {
				syncTaskNode.setComitLogCount(Integer.valueOf(str));
			}
		}

		// 描述
		Boolean taskDescChecked = ConfigUtil.isChecked(request, "taskDescCheckBox");
		if (taskDescChecked) {
			syncTaskNode.setDesc(request.getParameter("taskDesc"));
		}
	}

	public static void updateDatabaseApplierConfig(HttpServletRequest request, SyncTaskNode syncTaskNode)
			throws JSONException {
		//DatabaseApplierConfig config = new DatabaseApplierConfig();
		DataBaseApplierNode node = new DataBaseApplierNode();
		node.jsonStringToNodeSelf(syncTaskNode.getApplierData());

		// matrix
		Boolean matrixChecked = ConfigUtil.isChecked(request, "matrixCheckBox");
		if (matrixChecked) {
			String matrix = request.getParameter("matrix");
			node.setMatrixName(matrix);
		}

		// 规则名
		Boolean tddlRuleChecked = ConfigUtil.isChecked(request, "tddlRuleCheckBox");
		if (tddlRuleChecked) {
			String tddlRule = request.getParameter("tddlRule");
			node.setRuleName(tddlRule);
		}

		// 目标数据库类型
		Boolean applierDbTypeChecked = ConfigUtil.isChecked(request, "applierDbTypeCheckBox");
		if (applierDbTypeChecked) {
			String dbType = request.getParameter("applierDbType");
			node.setDbType(DBType.valueOf(dbType));
		}

		// 是否覆盖
		Boolean useReplaceChecked = ConfigUtil.isChecked(request, "useReplaceCheckBox");
		if (useReplaceChecked) {
			Boolean useReplace = ConfigUtil.isChecked(request, "useReplace");
			node.setReplace(useReplace);
		}

		// 失败继续
		Boolean failContinueChecked = ConfigUtil.isChecked(request, "failContinueCheckBox");
		if (failContinueChecked) {
			Boolean failContinue = ConfigUtil.isChecked(request, "failContinue");
			node.setFailContinue(failContinue);
		}

		// table map
		Boolean tableMapsChecked = ConfigUtil.isChecked(request, "tableMapsCheckBox");
		if (tableMapsChecked) {
			String tableMap = request.getParameter("tableMaps");
			// 会修改node的内容
			fillTableMap(tableMap, node);
		}

		syncTaskNode.setApplierData(node.toJSONString());
	}

	/**
	 * 从页面配置获取common config
	 * @param request
	 * @return
	 */
	public static CommonConfig getCommonConfig(HttpServletRequest request) {
		CommonConfig commonConfig = new CommonConfig();
		// 多线程开关
		Boolean mutilThreadChecked = ConfigUtil.isChecked(request, "mutilThreadCheckBox");
		if (mutilThreadChecked) {
			Boolean mutilThread = ConfigUtil.isChecked(request, "multiThread");
			commonConfig.setMultiThread(mutilThread);
		}

		// grouping setting
		Boolean groupingSettingsChecked = ConfigUtil.isChecked(request, "groupingSettingsCheckBox");
		if (groupingSettingsChecked) {
			List<GroupingConfig> groupingConfig = getGroupingConfig(request.getParameter("groupingSettings"));
			commonConfig.setGroupingSettings(groupingConfig);
		}

		// java opt
		Boolean javaOptChecked = ConfigUtil.isChecked(request, "javaOptCheckBox");
		if (javaOptChecked) {
			String javaOpt = request.getParameter("javaOpt");
			commonConfig.setJavaOpt(javaOpt);
		}

		// 是否使用上次位点
		Boolean useLastPositionChecked = ConfigUtil.isChecked(request, "useLastPositionCheckBox");
		if (useLastPositionChecked) {
			Boolean useLastPosition = ConfigUtil.isChecked(request, "useLastPosition");
			commonConfig.setUseLastPosition(useLastPosition);
		}

		// 统计周期
		Boolean statsPeriodChecked = ConfigUtil.isChecked(request, "statsPeriodCheckBox");
		if (statsPeriodChecked) {
			String str = request.getParameter("statsPeriod");
			if (StringUtil.isBlank(str)) {
				commonConfig.setStatsPeriod(10000);
			} else {
				commonConfig.setStatsPeriod(Integer.valueOf(str));
			}
		}

		// 事务统计周期
		Boolean summaryPeriodChecked = ConfigUtil.isChecked(request, "summaryPeriodCheckBox");
		if (summaryPeriodChecked) {
			String str = request.getParameter("summaryPeriod");
			if (StringUtil.isBlank(str)) {
				commonConfig.setSummaryPeriod(10000);
			} else {
				commonConfig.setSummaryPeriod(Integer.valueOf(str));
			}
		}

		// 位点提交周期
		Boolean comitLogPeriodChhecked = ConfigUtil.isChecked(request, "comitLogPeriodChheckBox");
		if (comitLogPeriodChhecked) {
			String str = request.getParameter("comitLogPeriod");
			if (StringUtil.isBlank(str)) {
				commonConfig.setComitLogPeriod(10000);
			} else {
				commonConfig.setComitLogPeriod(Integer.valueOf(str));
			}
		}

		//位点提交数量
		Boolean comitLogCountChecked = ConfigUtil.isChecked(request, "comitLogCountCheckBox");
		if (comitLogCountChecked) {
			String str = request.getParameter("comitLogCount");
			if (StringUtil.isBlank(str)) {
				commonConfig.setComitLogPeriod(1000);
			} else {
				commonConfig.setComitLogPeriod(Integer.valueOf(str));
			}
		}

		// 描述
		Boolean taskDescChecked = ConfigUtil.isChecked(request, "taskDescCheckBox");
		if (taskDescChecked) {
			commonConfig.setDescription(request.getParameter("taskDesc"));
		}

		return commonConfig;
	}

	/**
	 * CommonConfig使用
	 * @param grouping
	 * @return
	 */
	private static List<GroupingConfig> getGroupingConfig(String grouping) {

		List<GroupingConfig> groupingConfigs = new ArrayList<GroupingConfig>();
		InputStream is = ConfigUtil.string2InputStream(grouping);
		Config cfg = new Config();

		cfg.setMultiSection(true);
		Ini ini = new Ini();
		ini.setConfig(cfg);
		try {
			ini.load(is);

			List<Section> groupings = ini.getAll("grouping");

			for (Section section : groupings) {
				GroupingConfig config = new GroupingConfig();

				String schemaReg = section.get("schemaReg");
				String tableReg = section.get("tableReg");
				String fields = section.get("fields");

				config.setSchemaReg(schemaReg);
				config.setTableReg(tableReg);
				config.setFields(fields);

				groupingConfigs.add(config);
			}
		} catch (InvalidFileFormatException e) {
			log.error("", e);
		} catch (IOException e) {
			log.error("", e);
		}

		return groupingConfigs;
	}

	private static List<GroupingSetting> getGroupingSettings(String grouping) {

		List<GroupingSetting> groupingSettings = new ArrayList<GroupingSetting>();
		
		if (StringUtil.isBlank(grouping)) {
			return groupingSettings;
		}
		InputStream is = ConfigUtil.string2InputStream(grouping);
		Config cfg = new Config();

		cfg.setMultiSection(true);
		Ini ini = new Ini();
		ini.setConfig(cfg);
		try {
			ini.load(is);

			List<Section> groupings = ini.getAll("grouping");

			for (Section section : groupings) {
				GroupingSetting config = new GroupingSetting();

				String schemaReg = section.get("schemaReg");
				String tableReg = section.get("tableReg");
				String fields = section.get("fields");

				config.setSchemaReg(schemaReg);
				config.setTableReg(tableReg);
				config.setFields(fields);

				groupingSettings.add(config);
			}
		} catch (InvalidFileFormatException e) {
			log.error("", e);
		} catch (IOException e) {
			log.error("", e);
		}

		return groupingSettings;
	}

	public static void fillTableMap(String tableMapStr, DataBaseApplierNode node) {
		InputStream is = ConfigUtil.string2InputStream(tableMapStr);
		Config cfg = new Config();

		cfg.setMultiSection(true);
		Ini ini = new Ini();
		ini.setConfig(cfg);

		// 表名映射
		Map<String/*sourceLogicTable*/, String/*targetLogicTable*/> logicTableNames = new HashMap<String, String>();
		/**
		 * 列过滤，虽然叫ignoreColumns但是本身含义只是列过滤的名称
		 * 具体过滤方式是包含还是排除取决于对应filterFlags的MAP的BOOLEAN选项
		 */
		Map<String/* sourceLogicTable */, Set<String>/* columnName */> ignoreColumns = new HashMap<String, Set<String>>();
		Map<String/* sourceLogicTable */, Boolean/* include:true;eclude:false */> filterFlags = new HashMap<String, Boolean>();

		Map<String/*sourceLogicTable*/, Map<String/*SorceColumnName*/, String/*TargeColumnName*/>> columnMapping = new HashMap<String, Map<String, String>>();

		try {
			ini.load(is);

			List<Section> tableMaps = ini.getAll("TableMap");

			for (Section section : tableMaps) {

				String sourceTable = section.get("sourceTable");
				String targetTable = section.get("targetTable");
				String columnMap = section.get("columnMap");
				String filterColumns = section.get("filterColumns");
				Boolean useInclude = Boolean.valueOf(section.get("useInclude"));

				// 表名映射
				logicTableNames.put(sourceTable, targetTable);

				// 选择列集合
				ignoreColumns.put(sourceTable, ConfigUtil.commaSepString2Set(filterColumns));

				// 字段映射
				columnMapping.put(sourceTable, ConfigUtil.getTableMap(columnMap));

				// 包含还是排除
				filterFlags.put(sourceTable, useInclude);
			}

			node.setColumnMapping(columnMapping);

			node.setFilterFlags(filterFlags);

			node.setIgnoreColumns(ignoreColumns);

			node.setLogicTableNames(logicTableNames);
		} catch (InvalidFileFormatException e) {
			log.error("", e);
		} catch (IOException e) {
			log.error("", e);
		}
	}
}
