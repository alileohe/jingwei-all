package com.taobao.jingwei.webconsole.biz.exception;

/**
 * ��Ҫɾ��monitor��server�Ľڵ㣬�豣֤server��monitor����û���ӽڵ�
 * @author shuohailhl
 *
 */
public class HasChildCannotDeleteException extends JingweiException {

	private static final long serialVersionUID = 1L;

	public HasChildCannotDeleteException(String msg) {
		super(msg);
	}

}
