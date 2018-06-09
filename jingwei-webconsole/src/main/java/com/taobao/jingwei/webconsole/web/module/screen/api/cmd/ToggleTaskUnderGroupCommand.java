package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.OperateNode.OperateEnum;
import com.taobao.jingwei.webconsole.biz.ao.JingweiGroupAO;
import com.taobao.jingwei.webconsole.util.ApiErrorCode;
import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

/**
 * @desc 停止group类型的的任务
 * 
 *       <pre>
 * http://10.13.43.86/jingwei/api/JingweiGateWay.do?act=toggleTaskUnderGroup&host=XXX&groupName=XXX&taskName=XXX
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Apr 8, 2013 8:39:34 AM
 * 
 */
public class ToggleTaskUnderGroupCommand extends AbstractConsoleCommand {

	private static final Log log = LogFactory.getLog(ToggleTaskUnderGroupCommand.class);

	public static final String CMD_STR = "toggleTaskUnderGroup";

	/** <code>true</code>表示启动任务 <code>false</code>表示停止任务 */
	public static final String START_TASK = "startTask";

	private JingweiGroupAO jwGroupAO;

	public ToggleTaskUnderGroupCommand() {
		this(null, null);
	}

	public ToggleTaskUnderGroupCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {

		String groupName = request.getParameter(GROUP_NAME_PARAM);

		if (StringUtil.isBlank(groupName)) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.GROUP_NAME_IS_NULL_OR_EMPTY, response);
			return false;
		}

		String taskName = request.getParameter(TASK_NAME_PARAM);

		if (StringUtil.isBlank(taskName)) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.TASK_NAME_IS_EMPTY, response);
			return false;
		}

		boolean start = Boolean.valueOf(request.getParameter(START_TASK));

		OperateEnum operateEnum = null;
		// 修改状态
		if (start) {
			operateEnum = OperateEnum.NODE_START;
		} else {
			operateEnum = OperateEnum.NODE_STOP;
		}

		try {
			jwGroupAO.updateTaskOperate(groupName, taskName, operateEnum, hostIndex);
			log.warn("toggle task : " + taskName + ", group : " + groupName + ", start :" + operateEnum);
		} catch (Exception e) {
			log.error(e);
			JsonUtil.writeFailed2Response(json, ApiErrorCode.TOGGLE_GROUP_NAME_FAILED, response);
			return false;
		}

		// 操作成功，返回操作结果
		return true;
	}

	@Override
	public void success(JSONObject json) throws JSONException {

	}

	public JingweiGroupAO getJwGroupAO() {
		return jwGroupAO;
	}

	public void setJwGroupAO(JingweiGroupAO jwGroupAO) {
		this.jwGroupAO = jwGroupAO;
	}

}
