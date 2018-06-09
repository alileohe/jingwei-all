package com.taobao.jingwei.server.service.cmd;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.server.service.ServerErrorCode;
import com.taobao.jingwei.server.util.ServerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @desc ɾ��target��workĿ¼�µ��ļ� $JINGWEI_SERVER_HOME/plugin/target ��
 *       $JINGWEI_SERVER_HOME/plugin/work
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 7, 2013 12:58:28 PM
 */
public class DeleteTarUnderTargetAndWorkCmd extends AbstractHttpCmd {
	private static final Log log = LogFactory.getLog(DeleteTarUnderTargetAndWorkCmd.class);

	/** ��ȡָ��server�����ϵ�tar */
	public static final String CMD_STR = "deleteServerTar";

	/** ����tarName Ҫɾ�����ļ��� */
	public static final String TAR_NAME = "tarName";

	/** tar Ϊ�յ��쳣 */
	private static final String EMPTY_TAR_PARAM = "request param tarName should not be null or empty.";

	/** ɾ��tarʧ�� */
	private static final String DELETE_TAR_FAILED = "delete tar failed";

	/** ɾ��workʧ�� */
	private static final String DELETE_WORK_FAILED = "delete work failed";

	public DeleteTarUnderTargetAndWorkCmd(HttpRequest request, HttpResponse response) {
		super(request, response);
	}

	@Override
	protected void getJsonResponse(JSONObject jsonObj) throws JSONException {
		// ��ȡ������Ҫɾ����tar��
		String tar = super.params.get(TAR_NAME);
		log.warn("tar name is " + tar);

		// ����Ϊ���쳣
		if (StringUtil.isEmpty(tar)) {
			log.error(EMPTY_TAR_PARAM);

			jsonObj.put("isSuccess", Boolean.FALSE);
			jsonObj.put("cause", EMPTY_TAR_PARAM);
			jsonObj.put("errCode", ServerErrorCode.DELETE_TAR_NAME_IS_EMPTY);

			return;
		}

		// ɾ��$JINGWEI_SERVER_HOME/plugin/target
		boolean success = ServerUtil.removeTargetTar(tar);

		if (!success) {
			log.error(DELETE_TAR_FAILED + tar);

			jsonObj.put("isSuccess", Boolean.FALSE);
			jsonObj.put("cause", DELETE_TAR_FAILED);
			jsonObj.put("errCode", ServerErrorCode.DELETE_TAR_FAILED);

			return;
		}

		// ɾ��workĿ¼�µ��ļ�
		String workDirName = tar.substring(0, tar.indexOf("."));
		success = ServerUtil.removeWork(workDirName);

		if (!success) {
			log.error(DELETE_WORK_FAILED + tar);

			jsonObj.put("isSuccess", Boolean.FALSE);
			jsonObj.put("cause", DELETE_WORK_FAILED);
			jsonObj.put("errCode", ServerErrorCode.DELETE_WORK_FAILED);

			return;
		}

		// ���سɹ�
		jsonObj.put("isSuccess", Boolean.TRUE);
	}
}
