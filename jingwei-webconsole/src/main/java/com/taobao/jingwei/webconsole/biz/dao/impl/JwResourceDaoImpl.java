package com.taobao.jingwei.webconsole.biz.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.taobao.jingwei.webconsole.biz.dao.JwResourceDao;
import com.taobao.jingwei.webconsole.biz.dao.model.JwResource;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 3, 2013 12:11:41 PM
 *
 * @desc 
 */
public class JwResourceDaoImpl extends SqlMapClientDaoSupport implements JwResourceDao {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<JwResource> getAll(String zkEnv) {
		Map map = new HashMap();
		map.put("zkEnv", zkEnv);
		List<JwResource> list = getSqlMapClientTemplate().queryForList("resource.getAll", map);
		return list;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int deleteById(long id, String zkEnv) {
		Map map = new HashMap();
		map.put("zkEnv", zkEnv);
		map.put("id", id);
		int effectCount = getSqlMapClientTemplate().delete("resource.deleteById", map);
		return effectCount;
	}

	@Override
	public void save(JwResource jwResource) {
		getSqlMapClientTemplate().insert("resource.save", jwResource);
	}
}
