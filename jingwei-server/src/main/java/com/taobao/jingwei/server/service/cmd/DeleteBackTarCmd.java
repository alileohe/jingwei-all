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
 * @desc ɾ��$SERVER_HOME/plugin/bakĿ¼�µ�xx.tar.gz�ļ�
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 18, 2013 7:10:03 PM
 * 
 */
public class DeleteBackTarCmd extends AbstractHttpCmd {
	private static final Log log = LogFactory.getLog(DeleteBackTarCmd.class);

	/** ��ȡָ��server�����ϵ�tar */
	public static final String CMD_STR = "deleteBackTar";

	/** ����tarName Ҫɾ�����ļ��� */
	public static final String TAR_NAME = "tarName";

	public DeleteBackTarCmd(HttpRequest request, HttpResponse response) {
		super(request, response);
	}

	@Override
	protected void getJsonResponse(JSONObject jsonObj) throws JSONException {
		// ��ȡ������Ҫɾ����tar��
		String tar = super.params.get(TAR_NAME);
		log.warn("tar name is " + tar);

		// ����Ϊ���쳣
		if (StringUtil.isEmpty(tar)) {
			log.error(ServerErrorCode.DELETE_TAR_NAME_IS_EMPTY.getDesc());

			jsonObj.put("isSuccess", Boolean.FALSE);
			jsonObj.put("cause", ServerErrorCode.DELETE_TAR_NAME_IS_EMPTY.getDesc());
			jsonObj.put("errCode", ServerErrorCode.DELETE_TAR_NAME_IS_EMPTY);

			return;
		}

		// ɾ��$JINGWEI_SERVER_HOME/plugin/target
		boolean success = ServerUtil.removeBakTar(tar);

		if (!success) {
			log.error(ServerErrorCode.DELETE_TAR_FAILED.getDesc() + tar);

			jsonObj.put("isSuccess", Boolean.FALSE);
			jsonObj.put("cause", ServerErrorCode.DELETE_TAR_FAILED.getDesc());
			jsonObj.put("errCode", ServerErrorCode.DELETE_TAR_FAILED);

			return;
		}

		// ���سɹ�
		jsonObj.put("isSuccess", Boolean.TRUE);
	}

}
