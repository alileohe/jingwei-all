package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.citrus.util.StringUtil;
import com.taobao.jingwei.server.node.GroupNode;
import com.taobao.jingwei.webconsole.biz.ao.JingweiGroupAO;
import com.taobao.jingwei.webconsole.util.ApiErrorCode;
import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

/**
 * @desc 更改精卫group的配置属性supportBatchUpdate，是否支持批量修改；
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 25, 2013 2:36:03 PM
 */
public class ToggleGroupBatchCommand extends AbstractConsoleCommand {
	private static final Log log = LogFactory.getLog(ToggleGroupBatchCommand.class);

	public static final String CMD_STR = "toggleGroupBatchModify";

	public static final String GROUP_NAME_PARAM = "groupName";

	/** true or false */
	public static final String SUPPORT_BATCH_UPDATE_PARAM = "supportBatchUpdate";

	private JingweiGroupAO jwGroupAO;

	public ToggleGroupBatchCommand() {
		this(null, null);
	}

	public ToggleGroupBatchCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {

		String groupName = request.getParameter(GROUP_NAME_PARAM);

		// 校验精卫group name
		if (StringUtil.isBlank(groupName)) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.EMPTY_PARAM_ERR.setOption("group name"), response);
			return false;
		}

		String supportBatchUpdate = request.getParameter(SUPPORT_BATCH_UPDATE_PARAM);

		// 开关参数校验
		if (StringUtil.isBlank(supportBatchUpdate)) {
			JsonUtil.writeFailed2Response(json,
					ApiErrorCode.EMPTY_PARAM_ERR.setOption("please set batch update toggle true or false."), response);
			return false;
		} else if (!(supportBatchUpdate.equalsIgnoreCase("false") || supportBatchUpdate.equalsIgnoreCase("true"))) {
			JsonUtil.writeFailed2Response(json,
					ApiErrorCode.EMPTY_PARAM_ERR.setOption("please set batch update toggle true or false."), response);
			return false;
		}

		GroupNode groupNode = jwGroupAO.getGroupNode(groupName, hostIndex);

		groupNode.setSupportBatchUpdate(Boolean.valueOf(supportBatchUpdate));

		// 更新
		try {
			jwGroupAO.updateGroupNode(groupName, groupNode, hostIndex);
		} catch (Exception e) {
			log.error(e);
			JsonUtil.writeFailed2Response(json, ApiErrorCode.UPDATE_JINGWEI_GROUP_NODE_FAILED.setOption(groupName),
					response);
			return false;
		}

		// 把结果传回去
		json.put(SUPPORT_BATCH_UPDATE_PARAM, supportBatchUpdate);
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
