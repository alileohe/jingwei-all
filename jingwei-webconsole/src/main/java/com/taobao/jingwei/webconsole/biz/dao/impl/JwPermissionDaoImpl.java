package com.taobao.jingwei.webconsole.biz.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.taobao.jingwei.webconsole.biz.dao.JwPermissionDao;
import com.taobao.jingwei.webconsole.biz.dao.model.JwPermission;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 3, 2013 12:11:41 PM
 *
 * @desc 
 */
public class JwPermissionDaoImpl extends SqlMapClientDaoSupport implements JwPermissionDao {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<JwPermission> getAll(String zkEnv) {
		Map map = new HashMap();
		map.put("zkEnv", zkEnv);
		List<JwPermission> list = getSqlMapClientTemplate().queryForList("permission.getAll", map);
		return list;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int deleteById(long id, String zkEnv) {
		Map map = new HashMap();
		map.put("zkEnv", zkEnv);
		map.put("id", id);
		int effectCount = getSqlMapClientTemplate().delete("permission.deleteById", map);
		return effectCount;
	}

	@Override
	public void save(JwPermission jwPermission) {
		getSqlMapClientTemplate().insert("permission.insert", jwPermission);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<JwPermission> selectByRoleName(String roleName, String zkEnv) {
		Map map = new HashMap();
		map.put("zkEnv", zkEnv);
		map.put("roleName", roleName);
		List<JwPermission> list = getSqlMapClientTemplate().queryForList("permission.selectByRoleName", map);
		return list;
	}
}
