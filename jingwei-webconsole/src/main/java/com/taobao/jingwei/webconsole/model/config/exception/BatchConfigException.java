package com.taobao.jingwei.webconsole.model.config.exception;

/**
 * @desc 批量创建任务的异常
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 8, 2013 9:08:09 AM
 */

public class BatchConfigException extends Exception {

	private static final long serialVersionUID = 1L;

	public BatchConfigException() {
		super();
	}

	public BatchConfigException(String msg) {
		super(msg);
	}

	public BatchConfigException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public BatchConfigException(Throwable cause) {
		super(cause);
	}
}
