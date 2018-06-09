package com.taobao.jingwei.server.service;

import com.alibaba.common.lang.StringUtil;

/**
 * @desc �����˾���server��Ϊ��������ʧ�ܵ�ʱ�򷵻صĴ����룻�ͻ��˿�ʹ���ַ�������ʧ������
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 15, 2013 7:28:46 PM
 * 
 */
public enum ServerErrorCode {
	// ɾ��server�ϵ�tar����tarΪ�յ�ʱ��
	DELETE_TAR_NAME_IS_EMPTY("request param tarName should not be null or empty."),

	/** ɾ��ʧ�� */
	DELETE_TAR_FAILED("delete tar failed."),

	/** ɾ��workʧ�� */
	DELETE_WORK_FAILED("delete work failed."),

	/** Ҫ��ԭ��tar���ǿ� */
	BAK_TAR_FILE_IS_EMPTY("bak tar file param is empty."),

	/** ����tar������Ŀ¼ʧ�� */
	COPY_TAR2BACK_FAILED("copy tar to bak failed.");

	/** ���� */
	private String desc;

	ServerErrorCode(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

	public static ServerErrorCode getErrCodeByTypeString(String typeString) {
		if (StringUtil.isBlank(typeString)) {
			return null;
		}

		ServerErrorCode type = null;
		for (ServerErrorCode typeEnum : ServerErrorCode.values()) {
			if (StringUtil.equals(typeEnum.toString(), typeString)) {
				type = typeEnum;
				break;
			}
		}

		return type;
	}

	public static void main(String[] args) {
		System.out.println(DELETE_TAR_NAME_IS_EMPTY);

		System.out.println(DELETE_TAR_NAME_IS_EMPTY.getDesc());

		System.out.println(getErrCodeByTypeString("DELETE_TAR_NAME_IS_EMPTY"));
	}

}
