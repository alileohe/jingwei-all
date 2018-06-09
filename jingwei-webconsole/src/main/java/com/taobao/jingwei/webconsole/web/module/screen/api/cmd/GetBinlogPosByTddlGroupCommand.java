package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.taobao.jingwei.webconsole.biz.manager.JadeEnvMapManager;
import com.taobao.jingwei.webconsole.util.ApiErrorCode;
import com.taobao.jingwei.webconsole.util.position.GetAtomInfoFromTddlGroupFailedException;
import com.taobao.jingwei.webconsole.util.position.MasterPositionHelper;
import com.taobao.jingwei.webconsole.util.position.MasterPositionHelper.PositionInfo;
import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

/**
 * @desc 根据tddl的group name获取binlog position；注意：按照在TDDL_GROUP配置的dbKey的顺序获取位点
 * 
 *       <pre>
 *       <li> group example: IC_1_GROUP | my160068_cm6_ic_1_3306:r100w100p0,my064119_cm4_ic_1_3306:r0w0p0
 *       <li> 按照字面顺序尝试获取位点。首先尝试my160068_cm6_ic_1_3306对应的mysql，如果成功则返回。然后依次尝试
 *       <li> 获取的position格式 ：005877:24951435#16162943
 * </pre>
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 21, 2013 8:56:14 AM
 */
public class GetBinlogPosByTddlGroupCommand extends AbstractConsoleCommand {

	/** command */
	public static final String CMD_STR = "getBinlogPosByTddlGroup";

	/** 使用逗号分隔 */
	public static final String GROUPS_PARAM = "groups";

	private MasterPositionHelper positionHelper;

	public GetBinlogPosByTddlGroupCommand() {
		this(null, null);
	}

	public GetBinlogPosByTddlGroupCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	public GetBinlogPosByTddlGroupCommand(HttpServletRequest request, HttpServletResponse response,
			MasterPositionHelper positionHelper) {
		super(request, response);
		this.positionHelper = positionHelper;
	}

	public MasterPositionHelper getPositionHelper() {
		return positionHelper;
	}

	public void setPositionHelper(MasterPositionHelper positionHelper) {
		this.positionHelper = positionHelper;
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {
		String groupName = request.getParameter("group");
		String user = request.getParameter("user");
		String password = request.getParameter("password");

		Integer jadeEnvNumber = JadeEnvMapManager.getJadeEnvIdFromJwHostIndex(hostIndex);

		if (null == jadeEnvNumber) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.DELETE_PERMISSION_FAILED, response);
			log.error(ApiErrorCode.DELETE_PERMISSION_FAILED.getDescAndReset());
			return false;
		}

		// 只做自动切换的

		return getPosition(json, positionHelper, groupName, user, password, jadeEnvNumber, response);

	}

	@Override
	public void success(JSONObject json) throws JSONException {
		// NONE
	}

	public static boolean getPosition(JSONObject json, MasterPositionHelper positionHelper, String groupName,
			String user, String password, int jadeEnvNumber, HttpServletResponse response) throws JSONException {
		PositionInfo positionInfo = null;
		try {
			positionInfo = positionHelper.getPosition(groupName, user, password, String.valueOf(jadeEnvNumber));
		} catch (GetAtomInfoFromTddlGroupFailedException e1) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.GET_ATOM_INFO_FROM_TDDLGROUP_FAILED, response);
			return false;
		}

		// 获取位点失败
		if (null == positionInfo) {
			JsonUtil.writeFailed2Response(json, ApiErrorCode.GET_POSITION_FAILED, response);
			return false;
		}

		String position = positionInfo.getPosition();

		json.put("position", position);
		json.put("ip", positionInfo.getIp());
		json.put("port", positionInfo.getPort());

		return true;
	}

}
