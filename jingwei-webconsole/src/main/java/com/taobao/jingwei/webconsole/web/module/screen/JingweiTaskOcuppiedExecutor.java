package com.taobao.jingwei.webconsole.web.module.screen;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.StatusNode.StatusEnum;
import com.taobao.jingwei.webconsole.biz.ao.JingweiServerAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;

public class JingweiTaskOcuppiedExecutor {

	private static final Log log = LogFactory
			.getLog(JingweiTaskOcuppiedExecutor.class);

	@Autowired
	private JingweiServerAO jwServerAO;

	@Autowired
	private JingweiZkConfigManager jwConfigManager;

	@Autowired
	private HttpServletResponse response;

	public void execute(Context context,
			@Param(name = "serverName") String serverName,
			@Param(name = "host") String host) {

		// 获取使用中的执行器数量
		int occupiedExecutorCount = this.getOldOccupiedExecutorCount(
				serverName, host);
		if (occupiedExecutorCount == 0) {
			occupiedExecutorCount = this.getOccupiedExecutorCount(serverName,
					host);
		}

		PrintWriter writer = null;
		JSONObject jsonObj = new JSONObject();

		try {
			jsonObj.put("occupiedExecutorCount", occupiedExecutorCount);
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

	private int getOldOccupiedExecutorCount(String serverName, String host) {
		Set<String> tasks = jwServerAO.getTaskNames(host);

		int occupiedExecutorCount = 0;

		for (String taskName : tasks) {
			try {
				if (isRunning(jwConfigManager.getZkConfigManager(host),
						taskName, serverName)) {
					occupiedExecutorCount++;
				}
			} catch (JSONException e) {
				log.error(e);
			}
		}

		return occupiedExecutorCount;
	}

	private int getOccupiedExecutorCount(String serverName, String host) {
		Set<String> tasks = jwServerAO.getTaskNames(host);

		int occupiedExecutorCount = 0;

		for (String taskName : tasks) {
			int oneTaskOccupied = jwServerAO.getRunningTaskCount(serverName,
					taskName, host);

			occupiedExecutorCount += oneTaskOccupied;
		}

		return occupiedExecutorCount;
	}

	private boolean isRunning(ConfigManager configManager, String taskName,
			String hostName) throws JSONException {
		// path
		StringBuilder sb = new StringBuilder(
				JingWeiConstants.JINGWEI_TASK_ROOT_PATH);

		sb.append(JingWeiConstants.ZK_PATH_SEP).append(taskName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(
				JingWeiConstants.JINGWEI_TASK_HOST_NODE);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(hostName);
		sb.append(JingWeiConstants.ZK_PATH_SEP).append(
				JingWeiConstants.JINGWEI_STATUS_NODE_NAME);

		String path = sb.toString();

		// data
		String data = configManager.getData(path);

		if (StringUtil.isBlank(data)) {
			return false;
		}

		StatusNode statusNode = new StatusNode();
		statusNode.jsonStringToNodeSelf(data);

		if (statusNode.getStatusEnum() == StatusEnum.RUNNING) {
			return true;
		}

		return false;
	}
}
