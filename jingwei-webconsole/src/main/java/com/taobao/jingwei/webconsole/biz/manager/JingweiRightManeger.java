package com.taobao.jingwei.webconsole.biz.manager;

import java.util.Set;

import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jul 13, 2013 11:04:12 AM
 *
 * @desc 权限相关的配置
 */
public class JingweiRightManeger {
	// 逗号分隔的super user
	private String superUsers;

	private Set<String> superUserSet;

	public synchronized Set<String> getSuperUserSet() {

		if (this.superUserSet == null) {
			this.superUserSet = ConfigUtil.commaSepString2Set(this.getSuperUsers(), false);
		}
		return this.superUserSet;
	}

	public void setSuperUserSet(Set<String> superUserSet) {
		this.superUserSet = superUserSet;
	}

	public String getSuperUsers() {
		return superUsers;
	}

	public void setSuperUsers(String superUsers) {
		this.superUsers = superUsers;
	}
}
