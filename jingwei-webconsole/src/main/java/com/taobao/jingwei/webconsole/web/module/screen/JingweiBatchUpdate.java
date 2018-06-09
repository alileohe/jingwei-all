package com.taobao.jingwei.webconsole.web.module.screen;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.node.applier.MultiMetaApplierNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.webconsole.biz.ao.JingweiBatchAO;
import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.common.JingweiTypeHelper;
import com.taobao.jingwei.webconsole.model.config.ConfigHolder;
import com.taobao.jingwei.webconsole.model.config.applier.MultiMetaApplierConfig;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;
import com.taobao.jingwei.webconsole.util.EnvDataCache;

public class JingweiBatchUpdate implements JingWeiConstants {

	public static String SUPPORT_COMMON_CONFIG = "COMMON_CONFIG";
	public static String SUPPORT_DATABASE_APPLIER = "DATABASE_APPLIER";
	public static String SUPPORT_MYSQL_EXTRACTOR = "MYSQL_EXTRACTOR";
	public static String SUPPORT_MULTI_META_APPLIER = "MULTI_META_APPLIER";
	public static String SUPPORT_COMMON_FILTER_APPLIER = "COMMON_FILTER_APPLIER";

	private static Log log = LogFactory.getLog(JingweiBatchUpdate.class);

	@Autowired
	private JingweiBatchAO jwBatchAO;

	@Autowired
	private JingweiTaskAO jwTaskAO;

	@Autowired
	private EnvDataCache envDataCache;

	@Autowired
	private HttpServletResponse response;

	@Autowired
	private HttpServletRequest request;

	// 支持批量操作的组件
	public static String[] SUPPORT_CONPONENTS = { SUPPORT_COMMON_CONFIG, SUPPORT_DATABASE_APPLIER,
			SUPPORT_MYSQL_EXTRACTOR, SUPPORT_MULTI_META_APPLIER, SUPPORT_COMMON_FILTER_APPLIER };

	public void execute(Context context, @Param(name = "host") String host,
			@Param(name = "groupNameCriteria") String groupName, @Param(name = "page") String page,
			@Param(name = "pageSize") String pageSize) {

		context.put("configHolder", ConfigHolder.getInstance());

		context.put("supportConponents", SUPPORT_CONPONENTS);

		context.put("databaseApplierDbTypes", JingweiTypeHelper.getDbType().keySet());

		context.put("switchPolicyType", JingweiTypeHelper.getDBSyncSwitchPolicyType());

		// 下面的是通过group页面传过来的参数，支持批量修改
		// 要更新的task的列表
		String taskNames = request.getParameter("taskNames");

		List<String> taskNameList = ConfigUtil.commaSepString2List(taskNames);

		if (taskNameList.isEmpty()) {
			return;
		}

		// 作为模板的任务名
		String templateTaskName = taskNameList.get(0);

		// 更新类型
		String type = request.getParameter("type");

		// zk环境
		String zkKey = request.getParameter("host");

		SyncTaskNode syncTaskNode = jwTaskAO.getTaskInfo(templateTaskName, zkKey);

		ApplierType applierType = syncTaskNode.getApplierType();

		if ("applier".equalsIgnoreCase(type)) {

			if (applierType == ApplierType.MULTI_META_APPLIER) {
				String applierData = syncTaskNode.getApplierData();
				MultiMetaApplierNode multiMetaApplierNode = new MultiMetaApplierNode(applierData);

				MultiMetaApplierConfig multiMetaApplierConfig = new MultiMetaApplierConfig(multiMetaApplierNode);

				context.put("multiMetaApplierConfig", multiMetaApplierConfig);

				context.put("type", SUPPORT_MULTI_META_APPLIER);
			}
		}

		// 公共配置，比如修改多线程
		if ("common".equalsIgnoreCase(type)) {
			context.put("type", SUPPORT_COMMON_CONFIG);
		}

		// mysql binlog extractor
		if ("binlog-extractor".equalsIgnoreCase(type)) {
			context.put("type", SUPPORT_MYSQL_EXTRACTOR);
		}

		context.put("compressionType", JingweiTypeHelper.getCompressionType());

		context.put("host", zkKey);
		// context.put("taskNames", taskNameSet);

		context.put("taskNames", taskNames);
	}
}
