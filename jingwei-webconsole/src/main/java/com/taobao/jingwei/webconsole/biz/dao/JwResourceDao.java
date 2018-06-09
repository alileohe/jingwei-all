package com.taobao.jingwei.webconsole.biz.dao;

import java.util.List;

import com.taobao.jingwei.webconsole.biz.dao.model.JwResource;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 10, 2013 10:42:28 AM
 *
 * @desc ∑√Œ ◊ ‘¥DAO
 */
public interface JwResourceDao {
	void save(JwResource jwResource);

	List<JwResource> getAll(String zkEnv);

	int deleteById(long id, String zkEnv);
}
