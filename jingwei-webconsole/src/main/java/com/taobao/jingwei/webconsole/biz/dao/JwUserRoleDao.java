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
	 * ���ݽ�ɫ������������ȡ
	 * @return  ������������ڣ����ؿ�List
	 */
	List<JwUserRole> selectByRoleName(String roleName, String zkEnv);
	
	/**
	 * ����user-name(��������ȡ
	 * @return  ������������ڣ����ؿ�List
	 */
	List<JwUserRole> selectByNickName(String nickName, String zkEnv);
	
	/**
	 * ��ȡ���н�ɫ��
	 * @return
	 */
	List<String> getAllDistinctRoleNames(String zkEnv);
	
	/**
	 * ���ݻ����ͽ�ɫ����ѯ��¼��role��nicknameΨһԼ��
	 */
	JwUserRole selectByUserAndRole(String user, String role, String zkEnv);
}
