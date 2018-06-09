package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.PositionNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.monitor.util.GroupUtil;
import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.util.ApiErrorCode;
import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

/**
 * @desc 更新mysqlbilog extractor类型的任务的位点；
 * 
 *       <pre>
 * <li>任务正在运行则不能执行;
 * <li>如果位点为空，则按照当前获取的位点更新
 * <li>如果成功则返回更新成功的位点（执行updte后，再查出来的值）
 * </pre>
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 23, 2013 3:44:29 PM
 */
public class UpdateMysqlBinlogPosCommand extends AbstractConsoleCommand {

	public static final String CMD_STR = "updateMysqlBinlogPosCommand";

	public static final String TASK_NAME_PARAM = "taskName";

	public static final String BINLOG_POSITION_PARAM = "position";

	/** task ao */
	private JingweiTaskAO jwTaskAO;

	private JingweiZkConfigManager jwConfigManager;

	public UpdateMysqlBinlogPosCommand() {
		this(null, null);
	}

	public UpdateMysqlBinlogPosCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {

		String taskName = request.getParameter(TASK_NAME_PARAM);

		if (StringUtil.isBlank(taskName)) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.TASK_NAME_IS_EMPTY, response);
			return false;
		}

		// 获取配置
		SyncTaskNode syncTaskNode = jwTaskAO.getTaskInfo(taskName, hostIndex);

		// check extractor类型
		if (syncTaskNode.getExtractorType() != ExtractorType.BINLOG_EXTRACTOR) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.EXTRACTOR_TYPE_IS_NOT_MYSQLBINLOG, response);
			return false;
		}

		boolean running = GroupUtil.runningOnOneHost(jwConfigManager.getZkConfigManager(hostIndex), taskName);

		if (running) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.RUNNING_SHOULD_NOT_CHANGE_POSITION, response);
			return false;
		}

		// 要求重置置的位点
		String requestPosition = this.request.getParameter(BINLOG_POSITION_PARAM);

		if (StringUtil.isBlank(requestPosition)) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.CAN_NOT_UPDTE_EMPTY_POSITION, response);
			return false;
		}

		try {
			this.jwTaskAO.updateLastCommit(taskName, hostIndex, requestPosition);
		} catch (Exception e) {
			log.error(e);
		}

		// 获取位点
		PositionNode positionNode = jwTaskAO.getLastCommit(taskName, hostIndex);

		// 如果成功则返回修改后的结果

		if (StringUtil.isBlank(positionNode.getPosition())) {
			json.put("position", StringUtil.EMPTY_STRING);
		} else {
			json.put("position", positionNode.getPosition());
		}

		return true;
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
