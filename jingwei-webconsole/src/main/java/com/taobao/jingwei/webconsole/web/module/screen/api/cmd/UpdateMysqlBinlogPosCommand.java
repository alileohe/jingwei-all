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
 * @desc ����mysqlbilog extractor���͵������λ�㣻
 * 
 *       <pre>
 * <li>����������������ִ��;
 * <li>���λ��Ϊ�գ����յ�ǰ��ȡ��λ�����
 * <li>����ɹ��򷵻ظ��³ɹ���λ�㣨ִ��updte���ٲ������ֵ��
 * </pre>
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
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

		// ��ȡ����
		SyncTaskNode syncTaskNode = jwTaskAO.getTaskInfo(taskName, hostIndex);

		// check extractor����
		if (syncTaskNode.getExtractorType() != ExtractorType.BINLOG_EXTRACTOR) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.EXTRACTOR_TYPE_IS_NOT_MYSQLBINLOG, response);
			return false;
		}

		boolean running = GroupUtil.runningOnOneHost(jwConfigManager.getZkConfigManager(hostIndex), taskName);

		if (running) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.RUNNING_SHOULD_NOT_CHANGE_POSITION, response);
			return false;
		}

		// Ҫ�������õ�λ��
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

		// ��ȡλ��
		PositionNode positionNode = jwTaskAO.getLastCommit(taskName, hostIndex);

		// ����ɹ��򷵻��޸ĺ�Ľ��

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
