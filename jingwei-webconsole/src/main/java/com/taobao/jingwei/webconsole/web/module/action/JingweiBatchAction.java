package com.taobao.jingwei.webconsole.web.module.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jodd.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ini4j.InvalidFileFormatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.extractor.BinLogExtractorNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.model.JingWeiResult;
import com.taobao.jingwei.webconsole.model.config.BatchFactory;
import com.taobao.jingwei.webconsole.model.config.CommonConfig;
import com.taobao.jingwei.webconsole.model.config.CommonFilterConfig;
import com.taobao.jingwei.webconsole.model.config.ConfigHandler;
import com.taobao.jingwei.webconsole.model.config.Request2CommonFilterApplierNodeHelper;
import com.taobao.jingwei.webconsole.model.config.Request2MultiApplierNodeHelper;
import com.taobao.jingwei.webconsole.model.config.Request2MysqlExtractorNodeHelper;
import com.taobao.jingwei.webconsole.model.config.Request2SyncTaskNodeHelper;
import com.taobao.jingwei.webconsole.model.config.SyncTaskConfig;
import com.taobao.jingwei.webconsole.model.config.applier.ApplierConfig;
import com.taobao.jingwei.webconsole.model.config.exception.BatchConfigException;
import com.taobao.jingwei.webconsole.model.config.extractor.ExtractorConfig;
import com.taobao.jingwei.webconsole.model.config.extractor.MysqlExtractorConfig;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;
import com.taobao.jingwei.webconsole.util.DataCacheType;
import com.taobao.jingwei.webconsole.util.EnvDataCache;
import com.taobao.jingwei.webconsole.util.PageFilter;
import com.taobao.jingwei.webconsole.util.StringXmlApplicationContext;
import com.taobao.jingwei.webconsole.web.module.screen.JingweiBatchUpdate;

/**
 * @desc
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Dec 29, 2012 1:34:50 PM
 */

public class JingweiBatchAction {

	private Log log = LogFactory.getLog(ConfigHandler.class);

	@Autowired
	private JingweiZkConfigManager jwConfigManager;

	@Autowired
	private HttpServletResponse response;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private EnvDataCache envDataCache;

	@Autowired
	private JingweiTaskAO jwTaskAO;

	public void doCreateTask(Context context, @Param(name = "host") String host,
			@Param(name = "batchConfig") String batchConfigStr, @Param(name = "commonConfig") String commonConfigStr,
			@Param(name = "filterConfig") String filterConfigStr,
			@Param(name = "applierConfig") String applierConfigStr,
			@Param(name = "extractorConfig") String extractorConfigStr,
			@Param(name = "extractorProps") String extarctorPropsStr) {

		// common config
		StringXmlApplicationContext ctx = new StringXmlApplicationContext(commonConfigStr.trim(), null);
		CommonConfig commonConfig = (CommonConfig) ctx.getBean("commonConfig");

		// applier config
		ApplierConfig applierConfig = null;
		if (StringUtil.isNotBlank(applierConfigStr)) {
			StringXmlApplicationContext ctx1 = new StringXmlApplicationContext(applierConfigStr.trim(), null);
			applierConfig = (ApplierConfig) ctx1.getBean("applierConfig");
		}

		// extractor config
		StringXmlApplicationContext ctx2 = new StringXmlApplicationContext(extractorConfigStr.trim(), null);
		ExtractorConfig extractorConfig = (ExtractorConfig) ctx2.getBean("extractorConfig");

		// 如果是mysql的，需要porps格式的配置文件
		if (extractorConfig instanceof MysqlExtractorConfig) {
			extractorConfig.setProps(extarctorPropsStr);
		}

		// filter config 
		CommonFilterConfig commonFilterConfig = null;
		if (StringUtil.isNotBlank(filterConfigStr) && StringUtil.isNotBlank(filterConfigStr.trim())) {
			StringXmlApplicationContext ctx3 = new StringXmlApplicationContext(filterConfigStr.trim(), null);
			commonFilterConfig = (CommonFilterConfig) ctx3.getBean("commonFilterConfig");
		}

		SyncTaskConfig syncTaskConfig = new SyncTaskConfig();
		syncTaskConfig.setCommonConfig(commonConfig);
		syncTaskConfig.setExtractorConfig(extractorConfig);
		syncTaskConfig.setApplierConfig(applierConfig);
		syncTaskConfig.setCommonFilterConfig(commonFilterConfig);

		//		try {
		//			ConfigHandler.publishSyncTaskNode(this.jwConfigManager.getZkConfigManager(host), syncTaskConfig);
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}

		// 批量处理的配置
		BatchFactory batchFactory = null;
		if (StringUtil.isNotBlank(batchConfigStr) && StringUtil.isNotBlank(batchConfigStr.trim())) {
			StringXmlApplicationContext ctx4 = new StringXmlApplicationContext(batchConfigStr.trim(), null);
			batchFactory = (BatchFactory) ctx4.getBean("batchFactory");
		}

		List<SyncTaskConfig> syncTaskConfigList = Collections.emptyList();
		try {
			syncTaskConfigList = batchFactory.getSyncTaskConfigs(syncTaskConfig);
		} catch (BatchConfigException e1) {

			log.error(e1);
			e1.printStackTrace();
			context.put("messages", "batch create task config error. " + e1.getMessage());
			return;
		}

		List<String> publishFailureTaskList = this.publishFailureTaskList(
				this.jwConfigManager.getZkConfigManager(host), syncTaskConfigList);

		if (publishFailureTaskList.isEmpty()) {
			context.put(
					"messages",
					"publish all task success : "
							+ ConfigUtil.collection2CommaSepStr(this.syncTaskNameList(syncTaskConfigList)));
		} else {
			context.put("messages", "publish task error : " + ConfigUtil.collection2CommaSepStr(publishFailureTaskList));
		}
	}

	/**
	 * 返回写zk失败的任务列表
	 * 
	 * @return
	 */
	public List<String> publishFailureTaskList(ConfigManager configManager, List<SyncTaskConfig> syncTaskConfigList) {
		List<String> list = new ArrayList<String>(syncTaskConfigList.size());
		for (SyncTaskConfig syncTaskConfig : syncTaskConfigList) {
			try {
				ConfigHandler.publishSyncTaskNode(configManager, syncTaskConfig);
			} catch (Exception e) {
				list.add(syncTaskConfig.getCommonConfig().getTaskName());
				log.error(e);
				e.printStackTrace();
			}
		}

		return list;
	}

	private List<String> syncTaskNameList(List<SyncTaskConfig> syncTaskConfigList) {
		List<String> list = new ArrayList<String>();

		for (SyncTaskConfig config : syncTaskConfigList) {
			list.add(config.getCommonConfig().getTaskName());
		}
		return list;
	}

	public void doTaskPromotions(Context context, @Param(name = "host") String host) {

		// 改用新缓存
		List<String> taskNameList = envDataCache.getZkPathCache(host).get(
				DataCacheType.JingweiAssembledTask.toString(), new PageFilter(null) {
					@Override
					public boolean filter(Object target) {
						return true;
					}
				});

		List<String> list = new ArrayList<String>();

		String word = (String) request.getParameter("word");

		if (StringUtil.isNotBlank(word)) {
			for (String taskName : taskNameList) {
				if (taskName.startsWith(word)) {
					list.add(taskName);
				}
			}
		}

		//list.addAll(taskNameList);

		Collections.sort(list);

		PrintWriter writer = null;
		JSONObject jsonObj = new JSONObject();

		JSONArray jsonArray = new JSONArray(list);

		try {
			jsonObj.put("candidates", jsonArray);
			writer = response.getWriter();
			response.setContentType("application/json;charset=utf-8");
			if (null != writer) {
				writer.write(jsonObj.toString());
			}

		} catch (IOException e) {
			log.error(e);
			e.printStackTrace();
		} catch (JSONException e) {
			log.error(e);
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}
	}

	/**
	 * 判断选择的任务的extractor和applier的类型都相同，且
	 * 
	 * @param context
	 * @param host
	 */
	public void doCheckTask(Context context, @Param(name = "host") String host) {
		List<String> list = new ArrayList<String>();

		String tasks = (String) request.getParameter("tasks");

		String type = (String) request.getParameter("type");

		Map<String, String> unmatchTasks = this.checkType(type, ConfigUtil.commaSepString2List(tasks), host);

		list.addAll(ConfigUtil.map2List(unmatchTasks));

		Collections.sort(list);

		PrintWriter writer = null;
		JSONObject jsonObj = new JSONObject();

		JSONArray jsonArray = new JSONArray(list);

		try {
			jsonObj.put("candidates", jsonArray);
			writer = response.getWriter();
			response.setContentType("application/json;charset=utf-8");
			if (null != writer) {
				writer.write(jsonObj.toString());
			}
		} catch (IOException e) {
			log.error(e);
			e.printStackTrace();
		} catch (JSONException e) {
			log.error(e);
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		}
	}

	/**
	 * key是任务名，value是conponent的类型，返回不匹配的任务名和实际类型
	 * 
	 * @param type
	 * @param tasks
	 * @return
	 */
	private Map<String, String> checkType(String type, List<String> tasks, String host) {
		Map<String, String> map = new HashMap<String, String>();

		for (String task : tasks) {
			SyncTaskNode node = jwTaskAO.getTaskInfo(task, host);
			ApplierType realApplierType = node.getApplierType();
			ExtractorType realExtractorType = node.getExtractorType();

			if (type.equalsIgnoreCase(JingweiBatchUpdate.SUPPORT_DATABASE_APPLIER)) {

				if (realApplierType != ApplierType.DATABASE_APPLIER) {
					map.put(task, realApplierType.toString());
				}
			} else if (type.equalsIgnoreCase(JingweiBatchUpdate.SUPPORT_MULTI_META_APPLIER)) {
				if (realApplierType != ApplierType.MULTI_META_APPLIER) {
					map.put(task, realApplierType.toString());
				}
			} else if (type.equalsIgnoreCase(JingweiBatchUpdate.SUPPORT_MYSQL_EXTRACTOR)) {
				if (realExtractorType != ExtractorType.BINLOG_EXTRACTOR) {
					map.put(task, realApplierType.toString());
				}
			}
		}

		return map;
	}

	public void doUpdateTaskConfig(Context context, @Param(name = "host") String host,
			@Param(name = "tasks") String tasks) {
		// 需要修改的任务名集合
		Set<String> taskNames = ConfigUtil.commaSepString2Set(tasks);

		// 保存更新操作返回结果
		Map<String, JingWeiResult> results = new TreeMap<String, JingWeiResult>();

		for (String taskName : taskNames) {
			// 获取原来的配置
			SyncTaskNode syncTaskNode = jwTaskAO.getTaskInfo(taskName, host);

			// 使用页面填写的配置
			JingWeiResult result = null;
			try {
				result = this.updateTaskNode(syncTaskNode, host);
			} catch (JSONException e) {
				log.error("json error!", e);
			}

			results.put(taskName, result);
		}

		StringBuilder successTaskNames = new StringBuilder();
		StringBuilder failTaskNames = new StringBuilder();

		for (Map.Entry<String, JingWeiResult> en : results.entrySet()) {
			if (en.getValue().isSuccess()) {
				successTaskNames.append(en.getKey()).append(",");
			} else {
				failTaskNames.append(en.getKey()).append(",").append("cause:")
						.append(Arrays.deepToString(en.getValue().getReplaceInfo()));
			}
		}

		// 返回页面操作结果
		context.put("messages", " update tasks success : " + successTaskNames + "; failed : " + failTaskNames);
	}

	/**
	 * 更新zk对应的task节点，返回true表示成功
	 * 
	 * @param request
	 * @param syncTaskNode
	 * @param configManager
	 * @return
	 * @throws JSONException
	 */
	private JingWeiResult updateTaskNode(SyncTaskNode syncTaskNode, String host) throws JSONException {
		String configType = this.request.getParameter("configType");

		JingWeiResult jingWeiResult = new JingWeiResult();

		// 更改多线程配置
		if (configType.equalsIgnoreCase(JingweiBatchUpdate.SUPPORT_COMMON_CONFIG)) {

			Request2SyncTaskNodeHelper.updateCommonConfig(request, syncTaskNode);
		} else if (configType.equalsIgnoreCase(JingweiBatchUpdate.SUPPORT_DATABASE_APPLIER)) {
			// 更改配置
			Request2SyncTaskNodeHelper.updateDatabaseApplierConfig(request, syncTaskNode);

		} else if (configType.equalsIgnoreCase(JingweiBatchUpdate.SUPPORT_MULTI_META_APPLIER)) {
			// Multi Meta applier
			try {
				Request2MultiApplierNodeHelper.updateMultiApplierConfig(request, syncTaskNode);
			} catch (InvalidFileFormatException e) {
				jingWeiResult.setSuccess(false);
				jingWeiResult.setReplaceInfo(new String[] { "ini 列过滤配置项错误！" });
				return jingWeiResult;
			} catch (BatchConfigException e) {
				jingWeiResult.setSuccess(false);
				jingWeiResult.setReplaceInfo(new String[] { e.getMessage() });
				return jingWeiResult;
			}
		} else if (configType.equalsIgnoreCase(JingweiBatchUpdate.SUPPORT_COMMON_FILTER_APPLIER)) {
			try {
				Request2CommonFilterApplierNodeHelper.updateCommonFilterApplierConfig(request, syncTaskNode);
			} catch (InvalidFileFormatException e) {
				jingWeiResult.setSuccess(false);
				jingWeiResult.setReplaceInfo(new String[] { "ini 列过滤配置项错误！" });
				return jingWeiResult;
			}
		} else if (configType.equalsIgnoreCase(JingweiBatchUpdate.SUPPORT_MYSQL_EXTRACTOR)) {

			// 判断是否匹配类型
			ExtractorType realExtractorType = syncTaskNode.getExtractorType();
			if (realExtractorType != ExtractorType.BINLOG_EXTRACTOR) {
				jingWeiResult.setSuccess(false);
				jingWeiResult.setErrorCode(JingWeiResult.UPDATE_TYPE_UNMATCH_ERROR);
				jingWeiResult.setReplaceInfo(new String[] { "BINLOG_EXTRACTOR", realExtractorType.name() });

				log.error(jingWeiResult.getMessage());

				return jingWeiResult;
			}

			// 只支持自动切换模式的批量修改 
			String extractorData = syncTaskNode.getExtractorData();

			BinLogExtractorNode node = new BinLogExtractorNode();
			node.jsonStringToNodeSelf(extractorData);

			boolean isAutoSwitch = node.isAutoSwitch();

			if (!isAutoSwitch) {
				jingWeiResult.setSuccess(false);
				jingWeiResult.setReplaceInfo(new String[] { "不支持非自动切换类型的binlog extractor做批量更新！" });

				log.error(jingWeiResult.getMessage());

				return jingWeiResult;
			}

			BinLogExtractorNode updateNode = Request2MysqlExtractorNodeHelper.updateMysqlExtractorConfig(request, node);

			syncTaskNode.setExtractorData(updateNode.toJSONString());
		}

		jingWeiResult = this.jwTaskAO.updateTaskInfo(syncTaskNode, host);

		return jingWeiResult;
	}
}
