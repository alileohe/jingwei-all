package com.taobao.jingwei.common.node;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 配置对象的node抽象对象
 *
 * @author qihao
 */
public abstract class AbstractNode implements JingWeiConstants {

	protected static Log logger = LogFactory.getLog(AbstractNode.class);

	/**
	 * 节点名称，可以理解为节点ID
	 */
	private String name;

	/**
	 * 节点描述
	 */
	private String desc;

	/**
	 * json存储数据key定义
	 */
	private final static String NAME_KEY = "name";
	private final static String DESC_KEY = "desc";

	/**
	 * 节点配置信息是否持久
	 *
	 * @return
	 */
	public abstract boolean isPersistent();

	/**
	 * 获取数据的DATAID或者zk的path
	 *
	 * @return
	 */
	public abstract String getDataIdOrNodePath();

	/**
	 * 将节点自己特殊的属性到JSONObject中，该方法会在toJSONString
	 * 的时候调用
	 *
	 * @param jsonObject
	 * @throws JSONException
	 */
	protected abstract void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException;

	/**
	 * 将节点对象转换成JSONObject 字符串
	 *
	 * @return
	 * @throws JSONException
	 */
	public String toJSONString() throws JSONException {
		JSONObject jsonObject = this.comAttributeToJSONObject();
		//调用子类的填充特殊属性方法
		this.specilizeAttributeToJsonObject(jsonObject);
		return jsonObject.toString();
	}

	/**
	 * 从JSONObject对象中获取获取节点特殊属性值填充到自己对应的属性中
	 * 该方法会在fillJSONStrAttributeToSelf调用
	 *
	 * @param jsonObject
	 * @throws JSONException
	 */
	protected abstract void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException;

	/**
	 * 将jsonString转换成JSONObject并将其对应的属性值填充到
	 * 自己的属性中去
	 *
	 * @param jsonString
	 * @throws JSONException
	 */
	public void jsonStringToNodeSelf(String jsonString) throws JSONException {
		if (StringUtil.isBlank(jsonString)) {
			throw new JSONException("constructor JSONObject is empty!");
		}
		JSONObject restObject = new JSONObject(jsonString);
		//将JSONObject中公用属性值设置到自己对应属性中去
		this.jsonObjectToComAttribute(restObject);
		//调用子类的方法，将JSONObject中的属性值对应的填充到自己的属性中
		this.jsonObjectToSpecilizeAttribute(restObject);
	}

	/**
	 * 将自己所具有的公共属性填充到JSONObject中
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
	 * 将JSONObject中节点的特殊属性值，填充到节点对应的特殊属性值中
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
