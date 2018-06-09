package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.PositionNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.server.util.GroupUtil;
import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;
import com.taobao.jingwei.webconsole.util.ApiErrorCode;
import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

/**
 * @desc 更新mysqlbilog extractor类型的任务的位点； 每一行作为一个条目，任务名后面，是位点
 * 
 *       <pre>
 *             task1 000360:1979174#15212454
 *             task2 000361:574564445#15212454
 *             ......
 * </pre>
 * 
 *       结果为json格式
 * 
 *       <pre>
 *   {"content",
 * </pre>
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 23, 2013 3:44:29 PM
 */
public class BatchUpdateMysqlBinlogPosCommand extends AbstractConsoleCommand {

	public static final String CMD_STR = "batchUpdateMysqlBinlogPosCommand";

	public static final String POSITION_CONTENT = "positionContent";

	/** task ao */
	private JingweiTaskAO jwTaskAO;

	private JingweiZkConfigManager jwConfigManager;

	public BatchUpdateMysqlBinlogPosCommand() {
		this(null, null);
	}

	public BatchUpdateMysqlBinlogPosCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {

		String positionContent = request.getParameter(POSITION_CONTENT);

		if (StringUtil.isBlank(positionContent)) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.POSITION_CONTENT_IS_EMPTY, response);
			return false;
		}

		InputStreamReader ir = new InputStreamReader(ConfigUtil.string2InputStream(positionContent));
		BufferedReader br = new BufferedReader(ir);

		JSONArray array = new JSONArray();
		json.put(CONTENT, array);

		String line = StringUtil.EMPTY_STRING;
		try {
			while ((line = br.readLine()) != null) {
				String[] entry = line.split(BLANK);
				String taskName = entry[0];
				String position = entry[1];

				JSONObject result = this.update(taskName, position, hostIndex);
				array.put(result);
			}
		} catch (IOException e) {
			log.error(e);
			JsonUtil.writeFailed2Response(json, ApiErrorCode.POSITION_CONTENT_FORMAT_ERROR, response);
			return false;
		}

		return true;
	}

	private JSONObject update(String taskName, String position, String hostIndex) throws JSONException {

		JSONObject entry = new JSONObject();
		entry.put(TAR_NAME_KEY, taskName);

		// 获取配置
		SyncTaskNode syncTaskNode = jwTaskAO.getTaskInfo(taskName, hostIndex);

		// check extractor类型
		if (syncTaskNode.getExtractorType() != ExtractorType.BINLOG_EXTRACTOR) {
			entry.put(IS_SUCCESS, false);
			entry.put(CAUSE, ApiErrorCode.EXTRACTOR_TYPE_IS_NOT_MYSQLBINLOG);
		}

		boolean running = GroupUtil.runningOnOneHost(jwConfigManager.getZkConfigManager(hostIndex), taskName);

		if (running) {
			entry.put(IS_SUCCESS, false);
			entry.put(CAUSE, ApiErrorCode.RUNNING_SHOULD_NOT_CHANGE_POSITION);
			return entry;
		}

		try {
			this.jwTaskAO.updateLastCommit(taskName, hostIndex, position);
		} catch (Exception e) {
			log.error(e);
		}

		// 获取位点
		PositionNode positionNode = jwTaskAO.getLastCommit(taskName, hostIndex);

		if (positionNode.getPosition().equals(position)) {
			entry.put(IS_SUCCESS, true);
		} else {
			entry.put(IS_SUCCESS, false);
			entry.put(CAUSE, ApiErrorCode.UPDTE_POSITION_FAILED);
		}

		return entry;
	}

	@Override
	public void success(JSONObject json) throws JSONException {
		// none
	}

	public JingweiTaskAO getJwTaskAO() {
		return jwTaskAO;
	}

	public void setJwTaskAO(JingweiTaskAO jwTaskAO) {
		this.jwTaskAO = jwTaskAO;
	}

	public JingweiZkConfigManager getJwConfigManager() {
		return jwConfigManager;
	}

	public void setJwConfigManager(JingweiZkConfigManager jwConfigManager) {
		this.jwConfigManager = jwConfigManager;
	}
}
