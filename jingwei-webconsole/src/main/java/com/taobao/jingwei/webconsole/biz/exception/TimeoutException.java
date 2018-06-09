package com.taobao.jingwei.webconsole.biz.exception;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Aug 3, 2013 1:51:53 PM
 *
 * @desc ����console֮���Լ�����console��server֮��ͨ�ų�ʱ�쳣
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
