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
 * @desc ����tddl��group name��ȡbinlog position��ע�⣺������TDDL_GROUP���õ�dbKey��˳���ȡλ��
 * 
 *       <pre>
 *       <li> group example: IC_1_GROUP | my160068_cm6_ic_1_3306:r100w100p0,my064119_cm4_ic_1_3306:r0w0p0
 *       <li> ��������˳���Ի�ȡλ�㡣���ȳ���my160068_cm6_ic_1_3306��Ӧ��mysql������ɹ��򷵻ء�Ȼ�����γ���
 *       <li> ��ȡ��position��ʽ ��005877:24951435#16162943
 * </pre>
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 21, 2013 8:56:14 AM
 */
public class GetBinlogPosByTddlGroupCommand extends AbstractConsoleCommand {

	/** command */
	public static final String CMD_STR = "getBinlogPosByTddlGroup";

	/** ʹ�ö��ŷָ� */
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

		// ֻ���Զ��л���

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

		// ��ȡλ��ʧ��
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
