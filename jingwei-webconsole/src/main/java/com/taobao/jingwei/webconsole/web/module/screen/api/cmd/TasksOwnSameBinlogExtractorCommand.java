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
 * @desc 检查系列任务拥有相同的mysql binlog extractor配置
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
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

		// 要更新的task的列表
		String taskNames = request.getParameter("taskNames");

		List<String> taskNameList = ConfigUtil.commaSepString2List(taskNames);

		return false;
	}

	@Override
	public void success(JSONObject json) throws JSONException {
		// TODO Auto-generated method stub

	}

	/**
	 * 判断一组任务名是否都是binlog extractor，并且extractor配置都相同（只支持auto switch的）；
	 * 
	 * @param taskNames
	 * @return <code>true</code>表示所有的任务含有相同配置,如果taskNames为空或者数量是1，返回true；否则返回<code>false</code>
	 * @throws JSONException
	 */
	public ApiErrorCode haveSameConfigBinlogExtractor(List<String> taskNames, String zkKey) throws JSONException {
		if (taskNames == null || taskNames.isEmpty()) {
			return ApiErrorCode.TASK_LIST_IS_NULL_OR_EMPTY;
		}

		// 作为标准的节点
		String criteriaTaskName = taskNames.get(0);
		SyncTaskNode criteriaSyncTaskNode = jwTaskAO.getTaskInfo(criteriaTaskName, zkKey);

		int size = taskNames.size();
		BinLogExtractorNode criteriaNode = new BinLogExtractorNode();

		// 如果节点只有一个，则判断他的类型是否binlog和auto switch
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
	 * 判断binlog extractor的配置是否相同,比较下面的项
	 * 
	 * <pre>
	 * <li>  用户名
	 * <li>  密码
	 * <li>  charset
	 * <li>  库名表达式
	 * <li>  表名表达式
	 * <li>  切换规则
	 * </pre>
	 * 
	 * @param criteriaNode
	 * @param targetNode
	 * @return ApiErrorCode <code>ApiErrorCode.SUCCESS</code>如果相同；不同则返回不同 ErrCode
	 * @see com.taobao.jingwei.webconsole.util.ApiErrorCode#CONFIG_ATTRIBUTE_NOT_SAME
	 */
	public ApiErrorCode identityExtractorConfig(BinLogExtractorNode criteriaNode, BinLogExtractorNode targetNode) {
		// 切换规则
		if (!StringUtil.equals(criteriaNode.getSwitchPolicy(), targetNode.getSwitchPolicy())) {
			return ApiErrorCode.CONFIG_ATTRIBUTE_NOT_SAME.setOption("switch policy");
		}

		Properties criteriaProps = criteriaNode.getConf();
		Properties targetProps = targetNode.getConf();

		if (criteriaProps == null || targetProps == null) {
			return ApiErrorCode.CONFIG_ATTRIBUTE_NOT_SAME.setOption("extractor props is null");
		}

		// 比较charset
		String criteriaCharset = criteriaProps.getProperty(DBSYNC_AUTO_SWITCH_CHARSET_PROP_KEY);
		String targetCharset = targetProps.getProperty(DBSYNC_AUTO_SWITCH_CHARSET_PROP_KEY);

		if (StringUtil.equals(criteriaCharset, targetCharset)) {
			return ApiErrorCode.CONFIG_ATTRIBUTE_NOT_SAME.setOption("charset not same");
		}

		// 比较username
		String criteUser = criteriaProps.getProperty(DBSYNC_USER_PROP_KEY);
		String targetUser = targetProps.getProperty(DBSYNC_USER_PROP_KEY);
		if (StringUtil.equals(criteUser, targetUser)) {
			return ApiErrorCode.CONFIG_ATTRIBUTE_NOT_SAME.setOption("username not same");
		}

		// 比较password
		String critePassword = criteriaProps.getProperty(DBSYNC_PASSWORD_PROP_KEY);
		String targetPassword = targetProps.getProperty(DBSYNC_PASSWORD_PROP_KEY);
		if (StringUtil.equals(critePassword, targetPassword)) {
			return ApiErrorCode.CONFIG_ATTRIBUTE_NOT_SAME.setOption("schema regex not same");
		}

		// 比较库名表达式
		String criteScheReg = criteriaProps.getProperty(DBSYNC_SCHEMA_REGEX_PROP_KEY);
		String targetTableReg = targetProps.getProperty(DBSYNC_TABLE_REGEX_PROP_KEY);
		if (StringUtil.equals(criteScheReg, targetTableReg)) {
			return ApiErrorCode.CONFIG_ATTRIBUTE_NOT_SAME.setOption("table regex not same");
		}

		return ApiErrorCode.SUCCESS;
	}
}
