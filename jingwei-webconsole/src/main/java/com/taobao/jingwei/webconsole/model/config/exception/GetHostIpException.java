package com.taobao.jingwei.webconsole.model.config.exception;

/**
 * 根据hostname获取ip异常
 * @author shuohailhl
 *
 */
public class GetHostIpException extends Exception {
	private static final long serialVersionUID = 2891930698357575612L;

	public GetHostIpException(String string) {
		super(string);
	}
}
