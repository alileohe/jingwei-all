package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.taobao.jingwei.webconsole.web.module.screen.api.JsonUtil;

/**
 * @desc console���ش��������
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 23, 2013 10:34:49 AM
 */
public abstract class AbstractConsoleCommand extends AbstractApiCommand {

	/** mysql binlog extractor prop key user */
	public static final String DBSYNC_USER_PROP_KEY = "replicator.global.db.user";

	/** mysql binlog extractor prop key password */
	public static final String DBSYNC_PASSWORD_PROP_KEY = "replicator.global.db.password";

	/** mysql binlog extractor prop key charset */
	public static final String DBSYNC_AUTO_SWITCH_CHARSET_PROP_KEY = "replicator.extractor.mysql.charset";

	/** �������ʽ */
	public static final String DBSYNC_SCHEMA_REGEX_PROP_KEY = "replicator.global.filter.dbRegex";

	/** �������ʽ */
	public static final String DBSYNC_TABLE_REGEX_PROP_KEY = "replicator.global.filter.tabRegex";

	/** json��� �Ƿ�ɹ���� */
	public static final String IS_SUCCESS = "isSuccess";

	/** json��� ʧ��ԭ�� */
	public static final String CAUSE = "cause";

	/** ������ ����key */
	public static final String CONTENT = "content";

	/** task name key */
	public static final String TAR_NAME_KEY = "taskName";

	public AbstractConsoleCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public void invoke() throws JSONException {

		// ������ zookeeper ������ʶ
		String hostIndex = request.getParameter("host");

		JSONObject json = new JSONObject();
		// �����쳣ֱ�ӷ���
		boolean success = this.process(json, hostIndex);

		// ���ʧ���򷵻�
		if (!success) {
			if (log.isInfoEnabled()) {
				log.info("pocess cmd failed : " + request.getQueryString());
			}
			JsonUtil.writeJson2Client(json, response);
			return;
		}

		json.put("isSuccess", true);
		this.success(json);

		JsonUtil.writeJson2Client(json, response);
	}

	/**
	 * @param json ���ص�json����
	 * @Param zookeeper ������ʶ
	 * @return <code>true</code>��ʾ����ɹ�,<code>false</code>��ʾ����ʧ��
	 */
	public abstract boolean process(JSONObject json, String hostIndex) throws JSONException;

	public abstract void success(JSONObject json) throws JSONException;
}
