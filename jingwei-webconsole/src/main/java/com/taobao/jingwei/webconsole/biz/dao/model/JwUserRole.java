package com.taobao.jingwei.webconsole.biz.dao.model;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 13, 2013 2:51:51 PM
 *
 * @desc 
 */
public class JwUserRole extends ZkEnv {
	private long id;
	private String nickName;
	private String roleName;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

}
