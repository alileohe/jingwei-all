package com.taobao.jingwei.common.node;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ���ö����node�������
 *
 * @author qihao
 */
public abstract class AbstractNode implements JingWeiConstants {

	protected static Log logger = LogFactory.getLog(AbstractNode.class);

	/**
	 * �ڵ����ƣ��������Ϊ�ڵ�ID
	 */
	private String name;

	/**
	 * �ڵ�����
	 */
	private String desc;

	/**
	 * json�洢����key����
	 */
	private final static String NAME_KEY = "name";
	private final static String DESC_KEY = "desc";

	/**
	 * �ڵ�������Ϣ�Ƿ�־�
	 *
	 * @return
	 */
	public abstract boolean isPersistent();

	/**
	 * ��ȡ���ݵ�DATAID����zk��path
	 *
	 * @return
	 */
	public abstract String getDataIdOrNodePath();

	/**
	 * ���ڵ��Լ���������Ե�JSONObject�У��÷�������toJSONString
	 * ��ʱ�����
	 *
	 * @param jsonObject
	 * @throws JSONException
	 */
	protected abstract void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException;

	/**
	 * ���ڵ����ת����JSONObject �ַ���
	 *
	 * @return
	 * @throws JSONException
	 */
	public String toJSONString() throws JSONException {
		JSONObject jsonObject = this.comAttributeToJSONObject();
		//�������������������Է���
		this.specilizeAttributeToJsonObject(jsonObject);
		return jsonObject.toString();
	}

	/**
	 * ��JSONObject�����л�ȡ��ȡ�ڵ���������ֵ��䵽�Լ���Ӧ��������
	 * �÷�������fillJSONStrAttributeToSelf����
	 *
	 * @param jsonObject
	 * @throws JSONException
	 */
	protected abstract void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException;

	/**
	 * ��jsonStringת����JSONObject�������Ӧ������ֵ��䵽
	 * �Լ���������ȥ
	 *
	 * @param jsonString
	 * @throws JSONException
	 */
	public void jsonStringToNodeSelf(String jsonString) throws JSONException {
		if (StringUtil.isBlank(jsonString)) {
			throw new JSONException("constructor JSONObject is empty!");
		}
		JSONObject restObject = new JSONObject(jsonString);
		//��JSONObject�й�������ֵ���õ��Լ���Ӧ������ȥ
		this.jsonObjectToComAttribute(restObject);
		//��������ķ�������JSONObject�е�����ֵ��Ӧ����䵽�Լ���������
		this.jsonObjectToSpecilizeAttribute(restObject);
	}

	/**
	 * ���Լ������еĹ���������䵽JSONObject��
	 *
	 * @return
	 * @throws JSONException
	 */
	protected JSONObject comAttributeToJSONObject() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(NAME_KEY, StringUtil.defaultIfBlank(this.getName()).trim());
		jsonObject.put(DESC_KEY, StringUtil.defaultIfBlank(this.getDesc()).trim());
		return jsonObject;
	}

	/**
	 * ��JSONObject�нڵ����������ֵ����䵽�ڵ��Ӧ����������ֵ��
	 *
	 * @param jsonObject
	 * @throws JSONException
	 */
	protected void jsonObjectToComAttribute(JSONObject jsonObject) throws JSONException {
		if (null == jsonObject) {
			return;
		}
		this.setName(jsonObject.getString(NAME_KEY));
		this.setDesc(jsonObject.getString(DESC_KEY));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
