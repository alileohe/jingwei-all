package com.taobao.jingwei.webconsole.biz.dao;

import java.util.List;

import com.taobao.jingwei.webconsole.biz.dao.model.JwUserRole;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 13, 2013 2:54:48 PM
 *
 * @desc 
 */
public interface JwUserRoleDao {
	int deleteById(long id, String zkEnv);
	JwUserRole selectById(long id, String zkEnv);

	void save(JwUserRole jwUserRole);

	/**
	 * 根据角色名（花名）获取
	 * @return  如果花名不存在，返回空List
	 */
	List<JwUserRole> selectByRoleName(String roleName, String zkEnv);
	
	/**
	 * 根据user-name(花名）获取
	 * @return  如果花名不存在，返回空List
	 */
	List<JwUserRole> selectByNickName(String nickName, String zkEnv);
	
	/**
	 * 获取所有角色名
	 * @return
	 */
	List<String> getAllDistinctRoleNames(String zkEnv);
	
	/**
	 * 根据花名和角色名查询记录，role和nickname唯一约束
	 */
	JwUserRole selectByUserAndRole(String user, String role, String zkEnv);
}
