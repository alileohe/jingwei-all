package com.taobao.jingwei.webconsole.biz.dao.model;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 3, 2013 11:53:17 AM
 *
 * @desc 
 */
public class JwPermission extends ZkEnv {
	private long id;
	private String roleName;
	private String resourceName;
	private int resourceType;

	public int getResourceType() {
		return resourceType;
	}

	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

}
