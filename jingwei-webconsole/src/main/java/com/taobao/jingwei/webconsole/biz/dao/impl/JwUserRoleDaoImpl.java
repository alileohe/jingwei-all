package com.taobao.jingwei.webconsole.biz.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.taobao.jingwei.webconsole.biz.dao.JwUserRoleDao;
import com.taobao.jingwei.webconsole.biz.dao.model.JwUserRole;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 13, 2013 3:00:50 PM
 *
 * @desc 
 */
public class JwUserRoleDaoImpl extends SqlMapClientDaoSupport implements JwUserRoleDao {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int deleteById(long id, String zkEnv) {
		Map map = new HashMap();
		map.put("id", id);
		map.put("zkEnv", zkEnv);
		int effectCount = getSqlMapClientTemplate().delete("userrole.deleteById", map);
		return effectCount;
	}

	@Override
	public void save(JwUserRole jwUserRole) {
		getSqlMapClientTemplate().insert("userrole.insert", jwUserRole);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<JwUserRole> selectByRoleName(String roleName, String zkEnv) {
		Map map = new HashMap();
		map.put("roleName", roleName);
		map.put("zkEnv", zkEnv);
		List<JwUserRole> list = getSqlMapClientTemplate().queryForList("userrole.selectByRoleName", map);
		return list;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public JwUserRole selectById(long id, String zkEnv) {
		Map map = new HashMap();
		map.put("id", id);
		map.put("zkEnv", zkEnv);
		JwUserRole jwUserRole = (JwUserRole) getSqlMapClientTemplate().queryForObject("userrole.selectById", map);
		return jwUserRole;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<JwUserRole> selectByNickName(String nickName, String zkEnv) {
		Map map = new HashMap();
		map.put("nickName", nickName);
		map.put("zkEnv", zkEnv);
		List<JwUserRole> list = getSqlMapClientTemplate().queryForList("userrole.selectByNickName", map);
		return list;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<String> getAllDistinctRoleNames(String zkEnv) {
		Map map = new HashMap();
		map.put("zkEnv", zkEnv);
		List<String> allDistinctRoleNames = getSqlMapClientTemplate().queryForList("userrole.getAllDistinctRoleNames",
				map);
		return allDistinctRoleNames;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public JwUserRole selectByUserAndRole(String nickName, String roleName, String zkEnv) {
		Map map = new HashMap();
		map.put("nickName", nickName);
		map.put("roleName", roleName);
		map.put("zkEnv", zkEnv);
		JwUserRole jwUserRole = (JwUserRole) getSqlMapClientTemplate().queryForObject("userrole.selectByUserAndRole", map);
		return jwUserRole;
	}

}
