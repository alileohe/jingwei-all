package com.taobao.jingwei.webconsole.model.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.taobao.jingwei.common.node.extractor.BinLogExtractorNode;
import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;

/**
 * 
 * @desc ��������mysql-binlog������;ֻ֧���Զ��л�ģʽ����������
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 17, 2013 5:03:51 PM
 * 
 */
public class Request2MysqlExtractorNodeHelper {
	private static Log log = LogFactory.getLog(Request2MysqlExtractorNodeHelper.class);

	public static BinLogExtractorNode updateMysqlExtractorConfig(HttpServletRequest request, BinLogExtractorNode binlog)
			throws JSONException {

		StringBuilder sb = new StringBuilder();
		Set<Object> set = binlog.getConf().keySet();
		List<String> list = new ArrayList<String>();
		for (Object key : set) {
			list.add(key.toString());
		}
		Collections.sort(list);
		for (String key : list) {
			sb.append(key + "=" + binlog.getConf().getProperty(key));
			sb.append(System.getProperty("line.separator"));
		}

		// �û���
		Boolean mysqlExtractorUserChecked = ConfigUtil.isChecked(request, "mysqlExtractorUserCheckBox");
		if (mysqlExtractorUserChecked) {

			String userName = request.getParameter("mysqlExtractorUser");
			binlog.getConf().setProperty("replicator.global.db.user", userName);

			log.warn("batch update binlog extractor, chang user to " + userName);
		}

		// ����
		Boolean mysqlExtractorPasswordChecked = ConfigUtil.isChecked(request, "mysqlExtractorPasswordCheckBox");
		if (mysqlExtractorPasswordChecked) {

			String password = request.getParameter("mysqlExtractorPassword");
			binlog.getConf().setProperty("replicator.global.db.password", password);

			log.warn("batch update binlog extractor, chang password to " + password);
		}

		// �л�����
		Boolean mysqlExtractorSwitchPolicyChecked = ConfigUtil.isChecked(request, "mysqlExtractorSwitchPolicyCheckBox");
		if (mysqlExtractorSwitchPolicyChecked) {

			String switchPolicy = request.getParameter("mysqlExtractorSwitchPolicy");
			binlog.getConf().setProperty("replicator.plugin.directRelay.switchPolicy", switchPolicy);
			binlog.setSwitchPolicy(switchPolicy);

			log.warn("batch update binlog extractor, chang switch policy to " + switchPolicy);
		}

		// �ַ���
		Boolean mysqlExtractorCharsetChecked = ConfigUtil.isChecked(request, "mysqlExtractorCharsetCheckBox");
		if (mysqlExtractorCharsetChecked) {

			String charset = request.getParameter("mysqlExtractorCharset");
			binlog.getConf().setProperty("replicator.extractor.mysql.charset", charset);

			log.warn("batch update binlog extractor, chang charset to " + charset);
		}

		// �������ʽ
		Boolean mysqlExtractorDbRegexChecked = ConfigUtil.isChecked(request, "mysqlExtractorDbRegexCheckBox");
		if (mysqlExtractorDbRegexChecked) {

			String dbRegex = request.getParameter("mysqlExtractorDbRegex");
			binlog.getConf().setProperty("replicator.global.filter.dbRegex", dbRegex);

			log.warn("batch update binlog extractor, chang dbRegex to " + dbRegex);
		}

		// �������ʽ
		Boolean mysqlExtractorTableRegexChecked = ConfigUtil.isChecked(request, "mysqlExtractorTableRegexCheckBox");
		if (mysqlExtractorTableRegexChecked) {

			String tabRegex = request.getParameter("mysqlExtractorTableRegex");
			binlog.getConf().setProperty("replicator.global.filter.tabRegex", tabRegex);

			log.warn("batch update binlog extractor, chang tabRegex to " + tabRegex);
		}

		// �߼�ģʽ������������
		String oo = request.getParameter("mysqlExtractorPropsCheckBox");
		Boolean mysqlExtractorPropsChecked = ConfigUtil.isChecked(request, "mysqlExtractorPropsCheckBox");
		if (mysqlExtractorPropsChecked) {

			String props = request.getParameter("mysqlExtractorProps");

			BinLogExtractorNode newNode = new BinLogExtractorNode(props);

			newNode.setAutoSwitch(binlog.isAutoSwitch());
			newNode.setGroupName(binlog.getGroupName());
			newNode.setSwitchPolicy(binlog.getSwitchPolicy());

			log.warn("batch update binlog  props : " + props);

			return newNode;
		}

		return binlog;
	}
}
