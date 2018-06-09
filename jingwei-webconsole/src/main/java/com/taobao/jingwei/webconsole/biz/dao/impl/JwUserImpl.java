package com.taobao.jingwei.webconsole.biz.dao.impl;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.taobao.jingwei.webconsole.biz.dao.JwUserDao;
import com.taobao.jingwei.webconsole.biz.dao.model.JwUser;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time May 22, 2013 3:09:16 PM
 *
 * @desc 
 */
public class JwUserImpl extends SqlMapClientDaoSupport implements JwUserDao {

	@Override
	public void save(JwUser user) {
		getSqlMapClientTemplate().insert("user.save", user);
	}

}
