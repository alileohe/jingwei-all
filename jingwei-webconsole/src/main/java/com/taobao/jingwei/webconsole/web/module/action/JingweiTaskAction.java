package com.taobao.jingwei.webconsole.web.module.action;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.turbine.dataresolver.Params;
import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.node.applier.DataBaseApplierNode;
import com.taobao.jingwei.common.node.extractor.BinLogExtractorNode;
import com.taobao.jingwei.common.node.extractor.DrcExtractorNode;
import com.taobao.jingwei.common.node.extractor.OracleExtractorNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.DBType;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.biz.manager.JadeEnvMapManager;
import com.taobao.jingwei.webconsole.common.JingweiTypeHelper;
import com.taobao.jingwei.webconsole.model.*;
import com.taobao.jingwei.webconsole.model.config.exception.BatchConfigException;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;
import com.taobao.jingwei.webconsole.util.position.GetAtomInfoFromTddlGroupFailedException;
import com.taobao.jingwei.webconsole.util.position.MasterPositionHelper;
import com.taobao.jingwei.webconsole.util.position.MasterPositionHelper.PositionInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class JingweiTaskAction {
	private static Log log = LogFactory.getLog(JingweiTaskAction.class);
	@Autowired
	private JingweiTaskAO jwTaskAO;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	@Autowired
	private MasterPositionHelper positionHelper;

	public void doRegTask(Context context, Navigator navigator, @FormGroup(name = "jingweiTask") SyncTaskNode node,
			@Param(name = "extractorType") int extractorType, @Param(name = "applierType") int applierType,
			@Params JingweiApplierDBType applierDb, @Params JingweiApplierMetaType metaApplierNode,
			@Params GroupingEntry groupingEntrys, @Params JingweiExtractorMetaType metaExtractorNode,
			@Params JingweiBinlogExtractor binlog, @Params JingweiPropExtractor prop,
			@Params JingweiApplierGlobalFilter appGloFilter, @Params JingweiApplierMultiMetaType multiMetaApplierNode,
			@Params JingweiApplierAndorCommandType applierAndor, @Param(name = "host") String host) {
		JingWeiResult result = null;
		DataBaseApplierNode dban = null;
		try {
			// multi grouping setting
			if (null != groupingEntrys) {
				node.setGroupingSettings(groupingEntrys.getGroupingSetting());
			}

			node.setExtractorType(ExtractorType.getEnumByType(extractorType));
			node.setApplierType(ApplierType.getEnumByType(applierType));
			if (ExtractorType.BINLOG_EXTRACTOR.getType() == extractorType) {
				BinLogExtractorNode binExtractor = new BinLogExtractorNode(binlog.getExtractorData());
				if (binlog.isBinlogAutoSwitch()) {
					binExtractor.setAutoSwitch(binlog.isBinlogAutoSwitch());
					binExtractor.setGroupName(binlog.getBinlogGroupName());
					binExtractor.setSwitchPolicy(binlog.getBinlogSwitchPolicy());
				}
				node.setExtractorData(binExtractor.toJSONString());
			} else if (ExtractorType.META_EXTRACTOR.getType() == extractorType) {
				if (StringUtil.isBlank(metaExtractorNode.getExtMetaTopic())) {
					throw new NullPointerException("必须填写Extractor Meta主题");
				}
				if (StringUtil.isBlank(metaExtractorNode.getExtMetaGroup())) {
					throw new NullPointerException("必须填写Extractor Meta分组");
				}
				node.setExtractorData(metaExtractorNode.getMetaExtractorNode().toJSONString());
			} else if (ExtractorType.ORACLE_EXTRACTOR.getType() == extractorType) {
				OracleExtractorNode oracleNode = new OracleExtractorNode(JingWeiUtil.getPropFromString(prop
						.getContent()));
				node.setExtractorData(oracleNode.toJSONString());
			} else if (ExtractorType.DRC_EXTRACTOR.getType() == extractorType) {
				// 跟ORACLE公用同一个类型
				DrcExtractorNode drcNode = new DrcExtractorNode(JingWeiUtil.getPropFromString(prop.getContent()));
				node.setExtractorData(drcNode.toJSONString());
			}

			if (ApplierType.DATABASE_APPLIER.getType() == applierType) {
				dban = new DataBaseApplierNode();
				// required
				if (StringUtil.isBlank(applierDb.getMatrixName())) {
					throw new NullPointerException("必须填写Matrix名");
				}
				dban.setMatrixName(applierDb.getMatrixName());
				dban.setRuleName(applierDb.getRuleName());

				if (StringUtil.isNotBlank(applierDb.getDb())) {
					String[] schemas = StringUtil.split(applierDb.getDb(), ",");
					dban.setSchemaNames(Arrays.asList(schemas));
				} else {
					dban.setSchemaNames(Collections.<String> emptyList());
				}

				// applierDb.getTableMapping() = orgTable1 -> desTable|orgTable2
				// -> desTable
				if (applierDb.getTableMapping() != null) {
					String[] mappings = StringUtil.split(applierDb.getTableMapping(), "|");
					Map<String, String> tableMap = new HashMap<String, String>(mappings.length);
					for (int i = 0; i < mappings.length; i++) {
						if (StringUtil.isBlank(mappings[i])) {
							continue;
						}
						String[] org_des = StringUtil.split(mappings[i], " -> ");
						if (org_des.length != 2) {
							continue;
						}
						tableMap.put(org_des[0], org_des[1]);
					}
					dban.setLogicTableNames(tableMap);
				}

				// applierDb.getColumnMapping() = orgTable1.orgColumn1 ->
				// desColumn;orgTable1.orgColumn2 ->
				// desColumn|orgTable2.orgColumn1 ->
				// desColumn;orgTable2.orgColumn2 -> desColumn
				if (applierDb.getColumnMapping() != null) {
					String[] mappingss = StringUtil.split(applierDb.getColumnMapping(), "|");
					Map<String, Map<String, String>> columnMap = new HashMap<String, Map<String, String>>();
					for (String temp : mappingss) {
						String[] mappings = StringUtil.split(temp, ";");
						for (String mapping : mappings) {
							if (StringUtil.isBlank(mapping)) {
								continue;
							}
							String[] org_des = StringUtil.split(mapping, " -> ");
							if (org_des.length != 2) {
								continue;
							}
							String[] t_c = StringUtil.split(org_des[0], ".");
							Map<String, String> cMap = columnMap.get(t_c[0]);
							if (cMap == null) {
								cMap = new HashMap<String, String>();
								columnMap.put(t_c[0], cMap);
							}
							cMap.put(t_c[1], org_des[1]);

						}
					}
					dban.setColumnMapping(columnMap);
				}

				// applierDb.getIgnoreTableList() =
				// table1.column1;table1.column2#ture|table2.column1;table2.column2#false
				if (applierDb.getIgnoreTableList() != null) {
					String[] mappingss = StringUtil.split(applierDb.getIgnoreTableList(), "|");
					Map<String, Set<String>> ignoreMap = new HashMap<String, Set<String>>();
					Map<String, Boolean> filterFlags = new HashMap<String, Boolean>();
					for (String temp : mappingss) {
						String[] filters = StringUtils.split(temp, "#");
						if (filters.length != 2) {
							continue;
						}
						String[] mappings = StringUtil.split(filters[0], ";");
						for (String mapping : mappings) {
							if (StringUtil.isBlank(mapping)) {
								continue;
							}
							String[] tcs = StringUtil.split(mapping, ".");
							Set<String> cs = ignoreMap.get(tcs[0]);
							if (cs == null) {
								cs = new HashSet<String>();
								ignoreMap.put(tcs[0], cs);
							}
							Boolean bool = filterFlags.get(tcs[0]);
							if (bool == null) {
								filterFlags.put(tcs[0], "true".equals(filters[1]));
							}
							cs.add(tcs[1]);
						}
					}

					dban.setIgnoreColumns(ignoreMap);
					dban.setFilterFlags(filterFlags);
				}
				dban.setFailContinue(applierDb.isFailContinue());
				dban.setReplace(applierDb.isReplace());

				// non-required
				dban.setDbType(DBType.getEnumByType(applierDb.getDbType()));

				node.setApplierData(dban.toJSONString());
			} else if (ApplierType.META_APPLIER.getType() == applierType) {
				if (StringUtil.isBlank(metaApplierNode.getAppMetaTopic())) {
					throw new NullPointerException("必须填写Applier Meta主题");
				}
				// 提出这3个属性至全局
				// if (metaApplierNode.isMultiThread()) {
				// if (metaApplierNode.getQueueCapacity() < 0) {
				// throw new NullPointerException("队列容量必须大于0");
				// }
				// if (metaApplierNode.getMaxThreadCount() < 0) {
				// throw new NullPointerException("最大线程数必须大于0");
				// }
				// }
				node.setApplierData(metaApplierNode.getMetaApplierNode().toJSONString());
			} else if (ApplierType.MULTI_META_APPLIER.getType() == applierType) {
				node.setApplierData(multiMetaApplierNode.getMultiMetaApplierNode().toJSONString());
			} else if (ApplierType.ANDOR_COMMAND_APPLIER.getType() == applierType) {
				node.setApplierData(applierAndor.getAndorCommandApplierNode().toJSONString());

			}
			if (appGloFilter.isEnableApplierGlobalFilter()) {
				node.setApplierFilterData(appGloFilter.toJSONString());
			}

			// 如果设置批量标记，则批量创建任务
			String useBatchOnOff = request.getParameter("useBatch");
			Boolean useBatch = "on".equalsIgnoreCase(useBatchOnOff) ? true : false;
			if (useBatch) {
				// 批量创建任务
				result = this.createBatchTask(node, host);
			} else {
				result = jwTaskAO.addTaskInfo(node, host);
			}

		} catch (JSONException e) {
			result = new JingWeiResult();
			result.setSuccess(false);
			result.setErrorCode(JingWeiResult.JINGWEI_ERROR_PARSE_EXTRACTOR_DATA);
		} catch (NullPointerException e) {
			result = new JingWeiResult();
			result.setSuccess(false);
			result.setErrorCode(JingWeiResult.JINGWEI_ERROR_EXCEPTION);
			result.setReplaceInfo(new String[] { e.getMessage() });
		} catch (Exception e) {
			result = new JingWeiResult();
			result.setSuccess(false);
			result.setErrorCode(JingWeiResult.JINGWEI_ERROR_EXCEPTION);
			result.setReplaceInfo(new String[] { e.getMessage() });
		}
		if (result.isSuccess()) {
			navigator.redirectTo("jingweiModule").withTarget("jingweiTasks.vm").withParameter("host", host);
		} else {
			context.put("messages", result.getMessage());
			context.put("taskNode", node);
			context.put("host", host);
			context.put("sExtractorType", extractorType);
			context.put("sApplierType", applierType);
			context.put("dban", dban);
			if (dban != null) {
				context.put("schemas", JingweiTypeHelper.listToString(dban.getSchemaNames()));
			}
		}
	}

	public void doUpdateTask(Context context, Navigator navigator, @FormGroup(name = "jingweiTask") SyncTaskNode node,
			@Param(name = "extractorType") int extractorType, @Param(name = "applierType") int applierType,
			@Param(name = "replace") String replace, @Param(name = "failContinue") String failContinue,
			@Param(name = "appSplitTxEvent") String appSplitTxEvent, @Params JingweiApplierGlobalFilter appGloFilter,
			@Param(name = "updatePosition") String updatePosition, @Param(name = "position") String position,
			@Params JingweiApplierDBType applierDb, @Params JingweiApplierMetaType metaApplierNode,
			@Params GroupingEntry groupingEntrys, @Params JingweiExtractorMetaType metaExtractorNode,
			@Params JingweiBinlogExtractor binlog, @Params JingweiPropExtractor prop,
			@Params JingweiApplierMultiMetaType multiMetaApplierNode,
			@Params JingweiApplierAndorCommandType applierAndor, @Param(name = "host") String host) {
		JingWeiResult result = null;
		DataBaseApplierNode dban = null;
		try {

			// multi grouping setting
			if (null != groupingEntrys) {
				node.setGroupingSettings(groupingEntrys.getGroupingSetting());
			}

			node.setExtractorType(ExtractorType.getEnumByType(extractorType));
			node.setApplierType(ApplierType.getEnumByType(applierType));
			if (ExtractorType.BINLOG_EXTRACTOR.getType() == extractorType) {
				BinLogExtractorNode binExtractor = new BinLogExtractorNode(binlog.getExtractorData());
				if (binlog.isBinlogAutoSwitch()) {
					binExtractor.setAutoSwitch(binlog.isBinlogAutoSwitch());
					binExtractor.setGroupName(binlog.getBinlogGroupName());
					binExtractor.setSwitchPolicy(binlog.getBinlogSwitchPolicy());
				}
				node.setExtractorData(binExtractor.toJSONString());
			} else if (ExtractorType.META_EXTRACTOR.getType() == extractorType) {
				if (StringUtil.isBlank(metaExtractorNode.getExtMetaTopic())) {
					throw new NullPointerException("必须填写Extractor Meta主题");
				}
				if (StringUtil.isBlank(metaExtractorNode.getExtMetaGroup())) {
					throw new NullPointerException("必须填写Extractor Meta分组");
				}
				node.setExtractorData(metaExtractorNode.getMetaExtractorNode().toJSONString());
			} else if (ExtractorType.ORACLE_EXTRACTOR.getType() == extractorType) {
				OracleExtractorNode oracleNode = new OracleExtractorNode(JingWeiUtil.getPropFromString(prop
						.getContent()));
				node.setExtractorData(oracleNode.toJSONString());
			} else if (ExtractorType.DRC_EXTRACTOR.getType() == extractorType) {
				DrcExtractorNode drc = new DrcExtractorNode(JingWeiUtil.getPropFromString(prop.getContent()));
				node.setExtractorData(drc.toJSONString());
			}

			if (ApplierType.DATABASE_APPLIER.getType() == applierType) {
				dban = new DataBaseApplierNode();
				// required
				if (StringUtil.isBlank(applierDb.getMatrixName())) {
					throw new NullPointerException("必须填写Matrix名");
				}
				dban.setMatrixName(applierDb.getMatrixName());
				dban.setRuleName(applierDb.getRuleName());

				if (StringUtil.isNotBlank(applierDb.getDb())) {
					String[] schemas = StringUtil.split(applierDb.getDb(), ",");
					dban.setSchemaNames(Arrays.asList(schemas));
				} else {
					dban.setSchemaNames(Collections.<String> emptyList());
				}
				// applierDb.getTableMapping() = orgTable1 -> desTable|orgTable2
				// -> desTable
				if (applierDb.getTableMapping() != null) {
					String[] mappings = StringUtil.split(applierDb.getTableMapping(), "|");
					Map<String, String> tableMap = new HashMap<String, String>(mappings.length);
					for (int i = 0; i < mappings.length; i++) {
						if (StringUtil.isBlank(mappings[i])) {
							continue;
						}
						String[] org_des = StringUtil.split(mappings[i], " -> ");
						if (org_des.length != 2) {
							continue;
						}
						tableMap.put(org_des[0], org_des[1]);
					}
					dban.setLogicTableNames(tableMap);
				}

				// applierDb.getColumnMapping() = orgTable1.orgColumn1 ->
				// desColumn;orgTable1.orgColumn2 ->
				// desColumn|orgTable2.orgColumn1 ->
				// desColumn;orgTable2.orgColumn2 -> desColumn
				if (applierDb.getColumnMapping() != null) {
					String[] mappingss = StringUtil.split(applierDb.getColumnMapping(), "|");
					Map<String, Map<String, String>> columnMap = new HashMap<String, Map<String, String>>();
					for (String temp : mappingss) {
						String[] mappings = StringUtil.split(temp, ";");
						for (String mapping : mappings) {
							if (StringUtil.isBlank(mapping)) {
								continue;
							}
							String[] org_des = StringUtil.split(mapping, " -> ");
							if (org_des.length != 2) {
								continue;
							}
							String[] t_c = StringUtil.split(org_des[0], ".");
							Map<String, String> cMap = columnMap.get(t_c[0]);
							if (cMap == null) {
								cMap = new HashMap<String, String>();
								columnMap.put(t_c[0], cMap);
							}
							cMap.put(t_c[1], org_des[1]);

						}
					}
					dban.setColumnMapping(columnMap);
				}

				// applierDb.getIgnoreTableList() =
				// table1.column1;table1.column2#true|table2.column1;table2.column2#false
				if (applierDb.getIgnoreTableList() != null) {
					String[] mappingss = StringUtil.split(applierDb.getIgnoreTableList(), "|");
					Map<String, Set<String>> ignoreMap = new HashMap<String, Set<String>>();
					Map<String, Boolean> filterFlags = new HashMap<String, Boolean>();
					for (String temp : mappingss) {
						String[] filters = StringUtils.split(temp, "#");
						if (filters.length != 2) {
							continue;
						}
						String[] mappings = StringUtil.split(filters[0], ";");
						for (String mapping : mappings) {
							if (StringUtil.isBlank(mapping)) {
								continue;
							}
							String[] tcs = StringUtil.split(mapping, ".");
							Set<String> cs = ignoreMap.get(tcs[0]);
							if (cs == null) {
								cs = new HashSet<String>();
								ignoreMap.put(tcs[0], cs);
							}
							Boolean bool = filterFlags.get(tcs[0]);
							if (bool == null) {
								filterFlags.put(tcs[0], "true".equals(filters[1]));
							}
							cs.add(tcs[1]);
						}
					}
					dban.setIgnoreColumns(ignoreMap);
					dban.setFilterFlags(filterFlags);
				}
				dban.setFailContinue(applierDb.isFailContinue());
				dban.setReplace(applierDb.isReplace());

				// non-required
				dban.setDbType(DBType.getEnumByType(applierDb.getDbType()));

				node.setApplierData(dban.toJSONString());
			} else if (ApplierType.META_APPLIER.getType() == applierType) {
				if (StringUtil.isBlank(metaApplierNode.getAppMetaTopic())) {
					throw new NullPointerException("必须填写Applier Meta主题");
				}
				// if (metaApplierNode.isMultiThread()) {
				// if (metaApplierNode.getQueueCapacity() < 0) {
				// throw new NullPointerException("队列容量必须大于0");
				// }
				// if (metaApplierNode.getMaxThreadCount() < 0) {
				// throw new NullPointerException("最大线程数必须大于0");
				// }
				// }
				node.setApplierData(metaApplierNode.getMetaApplierNode().toJSONString());
			} else if (ApplierType.MULTI_META_APPLIER.getType() == applierType) {
				node.setApplierData(multiMetaApplierNode.getMultiMetaApplierNode().toJSONString());
			} else if (ApplierType.ANDOR_COMMAND_APPLIER.getType() == applierType) {
				node.setApplierData(applierAndor.getAndorCommandApplierNode().toJSONString());
			}
			if (appGloFilter.isEnableApplierGlobalFilter()) {
				node.setApplierFilterData(appGloFilter.toJSONString());
			}
			result = jwTaskAO.updateTaskInfo(node, host);
			// 更新位点
			if ("on".equalsIgnoreCase(updatePosition)) {
				jwTaskAO.updateLastCommit(node.getName(), host, position);
			}
		} catch (JSONException e) {
			result = new JingWeiResult();
			result.setSuccess(false);
			result.setErrorCode(JingWeiResult.JINGWEI_ERROR_PARSE_EXTRACTOR_DATA);
		} catch (NullPointerException e) {
			result = new JingWeiResult();
			result.setSuccess(false);
			result.setErrorCode(JingWeiResult.JINGWEI_ERROR_EXCEPTION);
			result.setReplaceInfo(new String[] { e.getMessage() + "修改失败" });
		} catch (Exception e) {
			result = new JingWeiResult();
			result.setSuccess(false);
			result.setErrorCode(JingWeiResult.JINGWEI_ERROR_EXCEPTION);
			result.setReplaceInfo(new String[] { e.getMessage() + "修改失败" });
		}
		if (result.isSuccess()) {
			context.put("updated", true);
		} else {
			context.put("messages", result.getMessage());
			context.put("taskNode", node);
			context.put("host", host);
			context.put("sExtractorType", extractorType);
			context.put("sApplierType", applierType);
			context.put("dban", dban);
			if (dban != null) {
				context.put("schemas", JingweiTypeHelper.listToString(dban.getSchemaNames()));
			}
		}
	}

	public void doDeleteTask(Context context, Navigator navigator, @Param(name = "taskName") String taskName,
			@Param(name = "host") String host, @Param(name = "criteria") String criteriaStr,
			@Param(name = "page") String page, @Param(name = "pageSize") String pageSize,
			@Param(name = "extractorType") String extractorType, @Param(name = "applierType") String applierType,
			@Param(name = "hostName") String hostName, @Param(name = "sTaskId") String sTaskId) {
		JingWeiResult result = jwTaskAO.deleteTaskInfo(taskName, host);
		// JingWeiResult result = new JingWeiResult();
		if (result.isSuccess()) {
			// navigator.redirectTo("jingweiModule").withTarget("jingweiTasks.vm")
			// .withParameter("host", host);
		} else {
			context.put("message", result.getMessage());
		}
	}

	@Deprecated
	public void doSearchTask(Context context, Navigator navigator, @Param(name = "host") String host,
			@Param(name = "extractorType") String extractorType, @Param(name = "sTaskId") String sTaskId,
			@Param(name = "applierType") String applierType, @Param(name = "hostName") String hostName,
			@Param(name = "runStatus") String runStatus) {
		JingweiTaskCriteria criteria = new JingweiTaskCriteria();
		criteria.setTaskId(sTaskId);
		criteria.setHostName(hostName);
		criteria.setExtractorType(StringUtil.isNotBlank(extractorType) ? Integer.parseInt(extractorType) : null);
		criteria.setApplierType(StringUtil.isNotBlank(applierType) ? Integer.parseInt(applierType) : null);
		criteria.setRunStatus(runStatus);
		navigator.redirectTo("jingweiModule").withTarget("jingweiTasks.vm").withParameter("host", host);
	}

	/**
	 * @param syncTaskNode
	 * @param host
	 * @return List<String> 保存创建任务失败的GROUPS的名字
	 * @throws JSONException
	 */
	@SuppressWarnings("unused")
	private JingWeiResult createBatchTask(SyncTaskNode syncTaskNode, String host) throws BatchConfigException {

		if (syncTaskNode.getExtractorType() != ExtractorType.BINLOG_EXTRACTOR) {
			throw new BatchConfigException("必须使用binlogextractor！");
		}

		// group的名字
		String jwGroup = this.request.getParameter("batchJwGroup");

		// 逗号分隔的TDDL GROUPS名
		String groups = this.request.getParameter("batchTddlGroups");

		// 任务名前缀
		String prefix = this.request.getParameter("batchTaskPrefix");

		// 后缀起始
		Integer start = Integer.valueOf(this.request.getParameter("batchStartSeq"));

		// 后缀步长
		Integer step = Integer.valueOf(this.request.getParameter("batchStep"));

		List<String> tddlGroups = ConfigUtil.commaSepString2List(groups);

		// 失败的group 名字
		List<String> list = new ArrayList<String>(tddlGroups.size());

		int currentSuffixIndex = start;
		for (String groupName : tddlGroups) {
			String taskName = new StringBuilder(prefix).append("-").append(currentSuffixIndex).toString();

			currentSuffixIndex += step;

			// 修改任务名
			syncTaskNode.setName(taskName);

			// 修改TDDL_GROUP NAME
			String binlogExtractorData = syncTaskNode.getExtractorData();

			BinLogExtractorNode node = new BinLogExtractorNode();

			try {
				node.jsonStringToNodeSelf(binlogExtractorData);
			} catch (JSONException e1) {
				throw new BatchConfigException("Json 异常，从binlog extractor node获取json字符串错误");
			}

			node.setGroupName(groupName);

			try {
				syncTaskNode.setExtractorData(node.toJSONString());
			} catch (JSONException e) {
				throw new BatchConfigException("Json 异常，从binlog extractor node获取json字符串错误");
			}

			JingWeiResult result = this.jwTaskAO.addTaskInfo(syncTaskNode, host);

			if (!result.isSuccess()) {
				list.add(groupName);
			}
		}

		JingWeiResult result = new JingWeiResult();
		if (list.isEmpty()) {
			result.setSuccess(true);
		} else {
			result.setSuccess(false);
			throw new BatchConfigException("创建失败任务对应的TDDL GROUP是：" + ConfigUtil.collection2CommaSepStr(list));
		}

		return result;
	}

	public void doDeleteSelectTasks(Context context, Navigator navigator, @Param(name = "host") String host,
			@Param(name = "criteria") String criteriaStr, @Param(name = "page") String page,
			@Param(name = "pageSize") String pageSize, @Param(name = "extractorType") String extractorType,
			@Param(name = "applierType") String applierType, @Param(name = "hostName") String hostName,
			@Param(name = "sTaskId") String sTaskId, @Param(name = "tasks") String tasks) {
		if (StringUtil.isEmpty(tasks)) {
			return;
		}

		String[] taskNames = tasks.split(",");
		for (String task : taskNames) {
			this.jwTaskAO.deleteTaskInfo(task, host);
		}
	}

	public void doGetPosition(Context context, @Param(name = "host") String host) {
		// 所有的task

		// 任务列表
		Set<String> candidates = null;

		String group = request.getParameter("group");
		String user = request.getParameter("user");
		String password = request.getParameter("password");

		String ip = request.getParameter("master");
		String port = request.getParameter("port");

		Integer jadeEnvNumber = JadeEnvMapManager.getJadeEnvIdFromJwHostIndex(host);

		String errMsg = StringUtil.EMPTY_STRING;
		if (null == jadeEnvNumber) {
			errMsg = "获取jade环境失败！";
		}

		String position = StringUtil.EMPTY_STRING;

		PositionInfo positionInfo = null;

		if (StringUtil.isBlank(ip)) {
			// 自动切换
			try {
				positionInfo = this.positionHelper.getPosition(group, user, password, String.valueOf(jadeEnvNumber));
			} catch (GetAtomInfoFromTddlGroupFailedException e) {
				log.error(e);
			}
			if (null != positionInfo) {
				position = positionInfo.getPosition();
				ip = positionInfo.getIp();
				port = String.valueOf(positionInfo.getPort());
			}
		} else {
			// 用Ip、port获取
			try {
				position = MasterPositionHelper.getMysqlPosition(ip, Integer.valueOf(port), user, password);
			} catch (Exception e) {
				errMsg = "获取 position 失败！";
				log.error("errMsg " + ip + " " + port, e);
			}
		}

		if (StringUtil.isEmpty(position)) {
			errMsg = "获取 position 失败！";
		}

		PrintWriter writer = null;
		JSONObject jsonObj = new JSONObject();

		try {
			jsonObj.put("position", position);
			jsonObj.put("ip", ip);
			jsonObj.put("port", port);
			jsonObj.put("errMsg", errMsg);
			writer = response.getWriter();
			response.setContentType("application/json;charset=utf-8");
			if (null != writer) {
				writer.write(jsonObj.toString());
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}
	}
}
