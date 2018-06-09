package com.taobao.jingwei.webconsole.util.upload;

import java.io.Serializable;

import com.alibaba.common.lang.StringUtil;

/**
 * 记录上传文件的状态，成功和异常
 * @author shuohailhl
 *
 */
public class UploadStatus implements Serializable {

	private static final long serialVersionUID = -2174439049411256405L;

	private boolean success = false;

	/** 错误信息 */
	private String errMessage = StringUtil.EMPTY_STRING;
}
