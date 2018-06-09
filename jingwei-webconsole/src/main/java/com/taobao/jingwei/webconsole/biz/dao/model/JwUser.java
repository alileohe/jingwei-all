package com.taobao.jingwei.webconsole.biz.dao.model;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time May 22, 2013 2:42:09 PM
 *
 * @desc 
 */
public class JwUser extends ZkEnv {
	String nickName;

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

}
