package com.taobao.jingwei.webconsole.biz.dao;

import java.util.List;

import com.taobao.jingwei.webconsole.biz.dao.model.JwPermission;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 3, 2013 11:52:46 AM
 *
 * @desc permission dao interface
 */
public interface JwPermissionDao {
	List<JwPermission> getAll(String zkEnv);

	int deleteById(long id, String zkEnv);

	void save(JwPermission jwPermission);

	/**
	 * ���ݽ�ɫ������������ȡpremission
	 * @return  ������������ڻ��߻�����Ӧ��Ȩ�޲����ڣ����ؿ�List
	 */
	List<JwPermission> selectByRoleName(String roleName, String zkEnv);
}
