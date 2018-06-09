package com.taobao.jingwei.webconsole.biz.exception;

/**
 * 想要删除monitor和server的节点，需保证server或monitor下面没有子节点
 * @author shuohailhl
 *
 */
public class HasChildCannotDeleteException extends JingweiException {

	private static final long serialVersionUID = 1L;

	public HasChildCannotDeleteException(String msg) {
		super(msg);
	}

}
