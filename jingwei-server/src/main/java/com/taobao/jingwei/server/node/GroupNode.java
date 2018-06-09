package com.taobao.jingwei.server.node;

import com.taobao.jingwei.common.node.AbstractNode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @desc path: /jingwei/groups/xx_group
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * @date Mar 17, 2013 4:22:04 PM
 */
public class GroupNode extends AbstractNode {

	/** ֧�������޸� */
	private Boolean supportBatchUpdate = false;

	private static final String SUPPORT_BATCH_UPDATE = "SUPPORT_BATCH_UPDATE";

	/**
	 * ����group���ֻ�ȡ·��
	 * 
	 * @param groupName
	 * @return
	 */
	public static final String getDataIdOrNodePath(String groupName) {
		return new StringBuilder(JINGWEI_GROUP_ROOT_PATH).append(FILE_SEP).append(groupName).toString();
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public String getDataIdOrNodePath() {
		return new StringBuilder(JINGWEI_GROUP_ROOT_PATH).append(FILE_SEP).append(super.getName()).toString();
	}

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
		jsonObject.put(SUPPORT_BATCH_UPDATE, this.getSupportBatchUpdate());
	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {

		if (jsonObject.has(SUPPORT_BATCH_UPDATE)) {
			this.setSupportBatchUpdate(jsonObject.getBoolean(SUPPORT_BATCH_UPDATE));
		}
	}

	public Boolean getSupportBatchUpdate() {
		return supportBatchUpdate;
	}

	public void setSupportBatchUpdate(Boolean supportBatchUpdate) {
		this.supportBatchUpdate = supportBatchUpdate;
	}

}
