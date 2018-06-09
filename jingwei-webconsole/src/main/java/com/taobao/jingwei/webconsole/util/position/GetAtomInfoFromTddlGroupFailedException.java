package com.taobao.jingwei.webconsole.util.position;

/**
 * @desc 根据group name获取ip和端口，标记是否失败；如果失败则抛异常GetAtomInfoFromTddlGroupFailedException
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 22, 2013 3:15:20 PM
 */
public class GetAtomInfoFromTddlGroupFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	public GetAtomInfoFromTddlGroupFailedException(String string) {
		super(string);
	}

}
