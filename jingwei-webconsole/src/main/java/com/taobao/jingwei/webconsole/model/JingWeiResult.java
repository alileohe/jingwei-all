/**
 * 
 */
package com.taobao.jingwei.webconsole.model;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.biz.command.result.ResultSupport;
import com.alibaba.common.lang.StringUtil;

/**
 * ���ڴ�Ž����Ϣ�Լ�������Ϣ
 * 
 * @author qingren 2011-8-28
 */
public class JingWeiResult extends ResultSupport {
	private static final long serialVersionUID = 471623667113910910L;
	/**
	 * ������Ϊnull
	 */
	public static final String JINGWEI_ERROR_CODE_FORBIDDEN_NULL = "01";

	/**
	 * Ŀ������Ѵ���
	 */
	public static final String JINGWEI_ERROR_CODE_EXISTED = "02";

	/**
	 * Ŀ����󲻴���
	 */
	public static final String JINGWEI_ERROR_CODE_NOT_EXISTED = "03";

	/**
	 * ��{0}����֮ǰ���뽫{1}�ĵ�ǰ������Ϊ{2}
	 */
	public static final String JINGWEI_ERROR_CODE_OPT_UNMEETS = "04";

	/**
	 * {0}�ѱ�ʹ��.
	 */
	public static final String JINGWEI_ERROR_CODE_USED = "05";

	/**
	 * {0}��������״̬�������Ƴ���
	 */
	public static final String JINGWEI_ERROR_CODE_TASK_RUNNING = "06";

	/**
	 * task[{0}]δ��agent������
	 */
	public static final String JINGWEI_ERROR_CODE_TASK_NOT_QUOTED = "07";

	/**
	 * agent[{0}]δ������
	 */
	public static final String JINGWEI_ERROR_CODE_AGENT_NOT_STARTED = "08";

	/**
	 * �ڵ㴴��ʧ��
	 */
	public static final String JINGWEI_ERROR_CODE_PATH_CREATE_FAILED = "09";

	/**
	 * �ڵ�ɾ��ʧ��
	 */
	public static final String JINGWEI_ERROR_CODE_PATH_DELETE_FAILED = "10";

	/**
	 * ����extractor data����
	 */
	public static final String JINGWEI_ERROR_PARSE_EXTRACTOR_DATA = "11";

	public static final String JINGWEI_ERROR_EXCEPTION = "12";
	
	/** /jingwei/servers/**server/tasks/**task�ڵ��Ѿ����� */
	public static final String JINGWEI_ERROR_SERVER_TASK_EXIST = "13";
	
	/** �������µ����Ͳ�ƥ��*/
	public static final String UPDATE_TYPE_UNMATCH_ERROR = "14";

	/**
	 * �����������쳣
	 */
	public static final String JINGWEI_ERROR_CODE_OPERATION_FAILED = "100";

	public static final Map<String, String> msgHolder;

	static {
		msgHolder = new HashMap<String, String>();
		msgHolder.put(JINGWEI_ERROR_CODE_FORBIDDEN_NULL, "{0}����Ϊ�ա�");
		msgHolder.put(JINGWEI_ERROR_CODE_EXISTED, "{0}�Ѵ��ڡ�");
		msgHolder.put(JINGWEI_ERROR_CODE_NOT_EXISTED, "{0}�����ڡ�");
		msgHolder.put(JINGWEI_ERROR_CODE_OPERATION_FAILED, "�������ڲ������쳣������ʧ�ܡ�");
		msgHolder.put(JINGWEI_ERROR_CODE_OPT_UNMEETS,
				"��{0}����֮ǰ���뽫{1}�ĵ�ǰ������Ϊ{2}��");
		msgHolder.put(JINGWEI_ERROR_CODE_USED, "{0}�ѱ�ʹ�á�");
		msgHolder.put(JINGWEI_ERROR_CODE_TASK_RUNNING, "{0}��������״̬���޷����д˲���");
		msgHolder.put(JINGWEI_ERROR_CODE_TASK_NOT_QUOTED, "task[{0}]δ��agent����");
		msgHolder.put(JINGWEI_ERROR_CODE_AGENT_NOT_STARTED, "agent[{0}]δ����");
		msgHolder.put(JINGWEI_ERROR_CODE_PATH_CREATE_FAILED, "�ڵ�{0}����ʧ��");
		msgHolder.put(JINGWEI_ERROR_CODE_PATH_DELETE_FAILED, "�ڵ�{0}ɾ��ʧ��");
		msgHolder.put(JINGWEI_ERROR_PARSE_EXTRACTOR_DATA,
				"Binlog Extractor���ݸ�ʽ����");
		msgHolder.put(JINGWEI_ERROR_EXCEPTION, "{0}");
		msgHolder.put(JINGWEI_ERROR_SERVER_TASK_EXIST, "������{0}�Ѿ�����{1}����");
		msgHolder.put(UPDATE_TYPE_UNMATCH_ERROR, "Ҫ���µ�������{0},ʵ����{1}");
	}

	/**
	 * �������
	 */
	private String errorCode;

	/**
	 * ��Ϣ�е��������
	 */
	private String[] replaceInfo;

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * ��ȡ������Ϣ
	 * 
	 * @return
	 */
	@SuppressWarnings("all")
	public String getMessage() {
		String msg = "";
		if (StringUtil.isBlank(errorCode)) {
			;
		} else if (replaceInfo == null || replaceInfo.length == 0) {
			msg = msgHolder.get(this.errorCode);
		} else {
			msg = MessageFormat.format(msgHolder.get(errorCode), replaceInfo);
		}
		return msg;
	}

	/**
	 * ��ȡ������Ϣ
	 * 
	 * @return
	 */
	@SuppressWarnings("all")
	public static String getMessage(String errorCode, String... replaceInfo) {
		String msg = "";
		if (StringUtil.isBlank(errorCode)) {
			;
		} else if (replaceInfo == null || replaceInfo.length == 0) {
			msg = msgHolder.get(errorCode);
		} else {
			msg = MessageFormat.format(msgHolder.get(errorCode), replaceInfo);
		}
		return msg;
	}

	public String[] getReplaceInfo() {
		return replaceInfo;
	}

	public void setReplaceInfo(String[] replaceInfo) {
		this.replaceInfo = replaceInfo;
	}
}
