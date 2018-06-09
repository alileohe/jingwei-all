package com.taobao.jingwei.webconsole.biz.exception;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Aug 3, 2013 1:51:53 PM
 *
 * @desc 精卫console之间以及精卫console和server之间通信超时异常
 */
public class TimeoutException extends JingweiException {

	private static final long serialVersionUID = 1L;

	public TimeoutException() {
		super();
	}

	public TimeoutException(String msg) {
		super(msg);
	}

	public TimeoutException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public TimeoutException(Throwable cause) {
		super(cause);
	}
}
