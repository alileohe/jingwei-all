package com.taobao.jingwei.webconsole.biz.dao.model;

/**
 * @author shuohai.lhl@taobao.com
 *
 * @time Jun 10, 2013 10:43:12 AM
 *
 * @desc 
 */
public class JwResource extends ZkEnv {

	private long id;
	private String resourceName;
	private int resourceType;

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getResourceType() {
		return resourceType;
	}

	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
	}

}
