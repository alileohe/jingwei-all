package com.taobao.jingwei.common.node;

import com.alibaba.common.lang.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * �ڵ�״̬��Ϣ�洢�ڵ㣬��Ҫ������parentDataIdOrPath
 * ȷ����StatusNode ���������ĸ�agent����task
 *
 * @author qihao
 */
public class StatusNode extends AbstractNode {

	/**
	 * ��statusNodeӵ���ߵĽڵ㣬��ӵ���ߵ�dataID
	 * ��Ҫ�Լ��ֹ�set,�������������zk����diamond
	 * agent����taskֻҪд���̶���ȡ���Լ�Ŀ¼�µ�
	 * /status����
	 */
	private volatile String ownerDataIdOrPath;

	private volatile StatusEnum statusEnum;

	/**
	 * json�洢����key����
	 */
	private final static String STATUS_KEY = "status";

	/* ��StatusNode �Լ������������õ�JSONObeject��
	  * @see com.taobao.jingwei.common.node.AbstractNode#specilizeAttributeToJsonObject(org.json.JSONObject)
	  */
	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
		if (statusEnum == null) {
			jsonObject.put(STATUS_KEY, StringUtil.EMPTY_STRING);
		} else {
			jsonObject.put(STATUS_KEY, StringUtil.defaultIfBlank(statusEnum.getStatusString()));
		}

	}

	/* ��JSONObject�нڵ�StatusNode���������ֵ����䵽�Լ���Ӧ������
	  * @see com.taobao.yugong.common.config.node.AbstractNode#jsonObjectToSpecilizeAttribute(org.json.JSONObject)
	  */
	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
		this.statusEnum = StatusEnum.getStatusEnumByString(jsonObject.getString(STATUS_KEY));

	}

	public boolean isPersistent() {
		return false;
	}

	public String getOwnerDataIdOrPath() {
		return ownerDataIdOrPath;
	}

	public void setOwnerDataIdOrPath(String ownerDataIdOrPath) {
		this.ownerDataIdOrPath = ownerDataIdOrPath;
	}

	public StatusEnum getStatusEnum() {
		return statusEnum;
	}

	public void setStatusEnum(StatusEnum statusEnum) {
		this.statusEnum = statusEnum;
	}

	/** 
	 * zk·��Ϊ:����/jingwei-v2/tasks/��������/hosts/��������/status
	 * ����/jingwei-v2/tasks/��������/hosts/�������� ΪownerDataIdOrPath
	 */
	public String getDataIdOrNodePath() {
		return StatusNode.getDataIdOrNodePathByOwner(ownerDataIdOrPath);
	}

	public static String getDataIdOrNodePathByOwner(String ownerDataIdOrPath) {
		if (StringUtil.isBlank(ownerDataIdOrPath)) {
			return StringUtil.EMPTY_STRING;
		}
		return ownerDataIdOrPath + ZK_PATH_SEP + JINGWEI_STATUS_NODE_NAME;
	}

	/**
	 * ״̬����ö��
	 *
	 * @author qihao
	 */
	public enum StatusEnum {
		/**
		 * �ڵ��������״̬���������κ�TASK����AGENT
		 */
		RUNNING("Running"), STANDBY("Standby");

		private String statusString;

		StatusEnum(String statusString) {
			this.statusString = statusString;
		}

		public String getStatusString() {
			return statusString;
		}

		/**
		 * ����typeString��ȡStatus��enum����
		 *
		 * @param typeString
		 * @return
		 */
		public static StatusEnum getStatusEnumByString(String statusString) {
			if (StringUtil.isBlank(statusString)) {
				return null;
			}
			StatusEnum retEnum = null;
			for (StatusEnum statusEnum : StatusEnum.values()) {
				if (StringUtil.equals(statusEnum.getStatusString(), statusString)) {
					retEnum = statusEnum;
					break;
				}
			}
			return retEnum;
		}
	}
}
