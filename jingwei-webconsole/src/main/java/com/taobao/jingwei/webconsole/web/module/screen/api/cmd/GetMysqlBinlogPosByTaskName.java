package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.citrus.util.StringUtil;
import com.taobao.jingwei.common.node.extractor.BinLogExtractorNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.biz.manager.JadeEnvMapManager;
import com.taobao.jingwei.webconsole.util.ApiErrorCode;
import com.taobao.jingwei.webconsole.util.position.MasterPositionHelper;
import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

/**
 * @desc 根据任务名获取mysql binlog的position;只支持group自动切换模式的，不支持ip+port的
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 23, 2013 12:22:59 PM
 */
public class GetMysqlBinlogPosByTaskName extends AbstractConsoleCommand {

	public static final String CMD_STR = "getMysqlBinlogPosByTaskName";

	/** 位点帮助类 */
	private MasterPositionHelper positionHelper;

	/** task ao */
	private JingweiTaskAO jwTaskAO;

	public GetMysqlBinlogPosByTaskName() {
		this(null, null);
	}

	public GetMysqlBinlogPosByTaskName(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {

		String taskName = request.getParameter(TASK_NAME_PARAM);

		if (StringUtil.isBlank(taskName)) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.TASK_NAME_IS_EMPTY, response);
			return false;
		}

		Integer jadeEnvNumber = JadeEnvMapManager.getJadeEnvIdFromJwHostIndex(hostIndex);

		// 掉jade接口失败
		if (null == jadeEnvNumber) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.GET_GADE_ENV_ERROR, response);
			return false;
		}

		BinLogExtractorNode extractorNode = new BinLogExtractorNode();

		ApiErrorCode errorCode = checkBinlogTypeAndAutoSwitch(jwTaskAO, taskName, hostIndex, extractorNode);

		if (errorCode != ApiErrorCode.SUCCESS) {
			JsonUtil.writeFailed2Response(json, errorCode, response);
			return false;
		}

		String groupName = extractorNode.getGroupName();
		if (StringUtil.isBlank(groupName)) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.GROUP_NAME_IS_NULL_OR_EMPTY, response);
			return false;
		}

		String user = extractorNode.getConf().getProperty("replicator.global.db.user");

		String password = extractorNode.getConf().getProperty("replicator.global.db.password");

		return GetBinlogPosByTddlGroupCommand.getPosition(json, positionHelper, groupName, user, password,
				jadeEnvNumber, response);
	}

	@Override
	public void success(JSONObject json) throws JSONException {

	}

	public MasterPositionHelper getPositionHelper() {
		return positionHelper;
	}

	public void setPositionHelper(MasterPositionHelper positionHelper) {
		this.positionHelper = positionHelper;
	}

	public JingweiTaskAO getJwTaskAO() {
		return jwTaskAO;
	}

	public void setJwTaskAO(JingweiTaskAO jwTaskAO) {
		this.jwTaskAO = jwTaskAO;
	}

	/**
	 * 校验是否是mysql binlog类型，并且是auto switch类型的
	 * 
	 * @param jwTaskAO
	 * @param taskName
	 * @param hostIndex
	 * @param extractorNode
	 * @return
	 * @throws JSONException
	 */
	public static ApiErrorCode checkBinlogTypeAndAutoSwitch(JingweiTaskAO jwTaskAO, String taskName, String hostIndex,
			BinLogExtractorNode extractorNode) throws JSONException {
		SyncTaskNode node = jwTaskAO.getTaskInfo(taskName, hostIndex);

		ApiErrorCode returnCode = checkMysqlBinlogExtractor(node);
		if (returnCode != ApiErrorCode.SUCCESS) {
			return returnCode;
		}

		String extractorData = node.getExtractorData();
		returnCode = checkAutoSwitchBinlogExtractor(extractorData, extractorNode);

		if (returnCode != ApiErrorCode.SUCCESS) {
			return returnCode;
		}

		return returnCode;
	}

	/**
	 * 校验是否是mysql binlog类型，
	 * 
	 * @param jwTaskAO
	 * @param taskName
	 * @param hostIndex
	 * @return ApiErrorCode <code>ApiErrorCode.SUCCESS</code>表示成功；其他的表示各种异常
	 * @throws JSONException
	 */
	public static ApiErrorCode checkMysqlBinlogExtractor(SyncTaskNode node) throws JSONException {

		ExtractorType type = node.getExtractorType();

		if (type != ExtractorType.BINLOG_EXTRACTOR) {
			return ApiErrorCode.EXTRACTOR_TYPE_IS_NOT_MYSQLBINLOG;
		}

		return ApiErrorCode.SUCCESS;
	}

	/**
	 * 检查是否是auto switch类型
	 * 
	 * @param extractorData
	 * @param extractorNode
	 * @return
	 * @throws JSONException
	 */
	public static ApiErrorCode checkAutoSwitchBinlogExtractor(String extractorData, BinLogExtractorNode extractorNode)
			throws JSONException {
		// extractor data是空
		if (StringUtil.isBlank(extractorData)) {
			return ApiErrorCode.EXTRACTOR_DATA_IS_EMPTY;
		}

		extractorNode.jsonStringToNodeSelf(extractorData);
		boolean autoSwitch = extractorNode.isAutoSwitch();
		if (!autoSwitch) {
			return ApiErrorCode.ONLY_SUPPORT_AUTO_SWITCH_MYSQL_EXTRACTOR;
		}

		return ApiErrorCode.SUCCESS;
	}

}
