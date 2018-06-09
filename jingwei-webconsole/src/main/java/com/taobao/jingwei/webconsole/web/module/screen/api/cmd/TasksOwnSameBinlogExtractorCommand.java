package com.taobao.jingwei.webconsole.web.module.screen.api.cmd;

import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.extractor.BinLogExtractorNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;
import com.taobao.jingwei.webconsole.util.ApiErrorCode;

/**
 * @desc ���ϵ������ӵ����ͬ��mysql binlog extractor����
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 24, 2013 11:15:42 AM
 */
public class TasksOwnSameBinlogExtractorCommand extends AbstractConsoleCommand {

	private JingweiTaskAO jwTaskAO;

	public TasksOwnSameBinlogExtractorCommand() {
		this(null, null);
	}

	public TasksOwnSameBinlogExtractorCommand(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public boolean process(JSONObject json, String hostIndex) throws JSONException {

		// Ҫ���µ�task���б�
		String taskNames = request.getParameter("taskNames");

		List<String> taskNameList = ConfigUtil.commaSepString2List(taskNames);

		return false;
	}

	@Override
	public void success(JSONObject json) throws JSONException {
		// TODO Auto-generated method stub

	}

	/**
	 * �ж�һ���������Ƿ���binlog extractor������extractor���ö���ͬ��ֻ֧��auto switch�ģ���
	 * 
	 * @param taskNames
	 * @return <code>true</code>��ʾ���е���������ͬ����,���taskNamesΪ�ջ���������1������true�����򷵻�<code>false</code>
	 * @throws JSONException
	 */
	public ApiErrorCode haveSameConfigBinlogExtractor(List<String> taskNames, String zkKey) throws JSONException {
		if (taskNames == null || taskNames.isEmpty()) {
			return ApiErrorCode.TASK_LIST_IS_NULL_OR_EMPTY;
		}

		// ��Ϊ��׼�Ľڵ�
		String criteriaTaskName = taskNames.get(0);
		SyncTaskNode criteriaSyncTaskNode = jwTaskAO.getTaskInfo(criteriaTaskName, zkKey);

		int size = taskNames.size();
		BinLogExtractorNode criteriaNode = new BinLogExtractorNode();

		// ����ڵ�ֻ��һ�������ж����������Ƿ�binlog��auto switch
		ApiErrorCode errCode = GetMysqlBinlogPosByTaskName.checkBinlogTypeAndAutoSwitch(jwTaskAO, criteriaTaskName,
				zkKey, criteriaNode);
		if (errCode != ApiErrorCode.SUCCESS) {
			return errCode;
		}

		for (int i = 1; i < size; i++) {

			BinLogExtractorNode targetNode = new BinLogExtractorNode();
			ApiErrorCode targetCode = GetMysqlBinlogPosByTaskName.checkBinlogTypeAndAutoSwitch(jwTaskAO,
					criteriaTaskName, zkKey, targetNode);
			if (targetCode != ApiErrorCode.SUCCESS) {
				return errCode;
			}
		}

		return ApiErrorCode.SUCCESS;
	}

	public JingweiTaskAO getJwTaskAO() {
		return jwTaskAO;
	}

	public void setJwTaskAO(JingweiTaskAO jwTaskAO) {
		this.jwTaskAO = jwTaskAO;
	}

	/**
	 * �ж�binlog extractor�������Ƿ���ͬ,�Ƚ��������
	 * 
	 * <pre>
	 * <li>  �û���
	 * <li>  ����
	 * <li>  charset
	 * <li>  �������ʽ
	 * <li>  �������ʽ
	 * <li>  �л�����
	 * </pre>
	 * 
	 * @param criteriaNode
	 * @param targetNode
	 * @return ApiErrorCode <code>ApiErrorCode.SUCCESS</code>�����ͬ����ͬ�򷵻ز�ͬ ErrCode
	 * @see com.taobao.jingwei.webconsole.util.ApiErrorCode#CONFIG_ATTRIBUTE_NOT_SAME
	 */
	public ApiErrorCode identityExtractorConfig(BinLogExtractorNode criteriaNode, BinLogExtractorNode targetNode) {
		// �л�����
		if (!StringUtil.equals(criteriaNode.getSwitchPolicy(), targetNode.getSwitchPolicy())) {
			return ApiErrorCode.CONFIG_ATTRIBUTE_NOT_SAME.setOption("switch policy");
		}

		Properties criteriaProps = criteriaNode.getConf();
		Properties targetProps = targetNode.getConf();

		if (criteriaProps == null || targetProps == null) {
			return ApiErrorCode.CONFIG_ATTRIBUTE_NOT_SAME.setOption("extractor props is null");
		}

		// �Ƚ�charset
		String criteriaCharset = criteriaProps.getProperty(DBSYNC_AUTO_SWITCH_CHARSET_PROP_KEY);
		String targetCharset = targetProps.getProperty(DBSYNC_AUTO_SWITCH_CHARSET_PROP_KEY);

		if (StringUtil.equals(criteriaCharset, targetCharset)) {
			return ApiErrorCode.CONFIG_ATTRIBUTE_NOT_SAME.setOption("charset not same");
		}

		// �Ƚ�username
		String criteUser = criteriaProps.getProperty(DBSYNC_USER_PROP_KEY);
		String targetUser = targetProps.getProperty(DBSYNC_USER_PROP_KEY);
		if (StringUtil.equals(criteUser, targetUser)) {
			return ApiErrorCode.CONFIG_ATTRIBUTE_NOT_SAME.setOption("username not same");
		}

		// �Ƚ�password
		String critePassword = criteriaProps.getProperty(DBSYNC_PASSWORD_PROP_KEY);
		String targetPassword = targetProps.getProperty(DBSYNC_PASSWORD_PROP_KEY);
		if (StringUtil.equals(critePassword, targetPassword)) {
			return ApiErrorCode.CONFIG_ATTRIBUTE_NOT_SAME.setOption("schema regex not same");
		}

		// �ȽϿ������ʽ
		String criteScheReg = criteriaProps.getProperty(DBSYNC_SCHEMA_REGEX_PROP_KEY);
		String targetTableReg = targetProps.getProperty(DBSYNC_TABLE_REGEX_PROP_KEY);
		if (StringUtil.equals(criteScheReg, targetTableReg)) {
			return ApiErrorCode.CONFIG_ATTRIBUTE_NOT_SAME.setOption("table regex not same");
		}

		return ApiErrorCode.SUCCESS;
	}
}
