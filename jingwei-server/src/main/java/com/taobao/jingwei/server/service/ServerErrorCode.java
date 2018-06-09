package com.taobao.jingwei.server.service;

import com.alibaba.common.lang.StringUtil;

/**
 * @desc 定义了精卫server作为服务，请求失败的时候返回的错误码；客户端可使用字符串检索失败类型
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 15, 2013 7:28:46 PM
 * 
 */
public enum ServerErrorCode {
	// 删除server上的tar，当tar为空的时候
	DELETE_TAR_NAME_IS_EMPTY("request param tarName should not be null or empty."),

	/** 删除失败 */
	DELETE_TAR_FAILED("delete tar failed."),

	/** 删除work失败 */
	DELETE_WORK_FAILED("delete work failed."),

	/** 要还原的tar包是空 */
	BAK_TAR_FILE_IS_EMPTY("bak tar file param is empty."),

	/** 拷贝tar到备份目录失败 */
	COPY_TAR2BACK_FAILED("copy tar to bak failed.");

	/** 描述 */
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
