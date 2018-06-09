package com.taobao.jingwei.webconsole.biz.exception;

public class JingweiException extends Exception {

	private static final long serialVersionUID = 4668726845919487624L;

	public JingweiException() {
		super();
	}

	public JingweiException(String msg) {
		super(msg);
	}

	public JingweiException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public JingweiException(Throwable cause) {
		super(cause);
	}

}
