package com.taobao.jingwei.webconsole.util.upload;

import java.io.Serializable;

import com.alibaba.common.lang.StringUtil;

/**
 * ��¼�ϴ��ļ���״̬���ɹ����쳣
 * @author shuohailhl
 *
 */
public class UploadStatus implements Serializable {

	private static final long serialVersionUID = -2174439049411256405L;

	private boolean success = false;

	/** ������Ϣ */
	private String errMessage = StringUtil.EMPTY_STRING;
}
