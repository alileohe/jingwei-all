package com.taobao.jingwei.webconsole.biz.ao.impl;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import com.taobao.jingwei.webconsole.biz.ao.JingweiCommonAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;

public class JingweiCommonAOImpl implements JingweiCommonAO {
	@Autowired
	private JingweiZkConfigManager jwConfigManager;

	@Autowired
	private HttpSession session;

	@Override
	public boolean isPathExist(String path) {
		return jwConfigManager.getZkConfigManager(
				session.getAttribute(JingweiZkConfigManager.SESSION_KEY))
				.exists(path);
	}

}
