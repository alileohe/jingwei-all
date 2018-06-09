package com.taobao.jingwei.webconsole.util.position;

/**
 * @desc Run 'show variables like '%server_id%' return empty
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 21, 2013 7:09:31 PM
 */
public class ShowServerIdEmptyException extends Exception {

	private static final long serialVersionUID = 1L;

	public ShowServerIdEmptyException(String string) {
		super(string);
	}

}