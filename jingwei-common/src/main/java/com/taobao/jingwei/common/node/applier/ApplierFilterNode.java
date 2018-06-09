package com.taobao.jingwei.common.node.applier;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.AbstractNode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Apr 17, 2012 10:52:22 AM
 */

public class ApplierFilterNode extends AbstractNode {

	/** ����������ʽ */
	private String srcSchemaReg;
	/** ����������ʽ */
	private String srcTableReg;

	/** ���� �� ���� */
	private EventFilterNode eventFilterNode;

	/** ����������ʽ */
	private static final String SRC_SCHEMA_REG = "SRC_SCHEMA_REG";
	/** ����������ʽ */
	private static final String SRC_TABLE_REG = "SRC_TABLE_REG";

	private static final String COLUNM_ACTION_DATA = "columnActionData";

	public ApplierFilterNode() {

	}

	public ApplierFilterNode(String jsonStr) {
		try {
			this.jsonStringToNodeSelf(jsonStr);
		} catch (JSONException e) {
			logger.error("new ApplierFilterNode  paser  Error!", e);
		}
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public String getDataIdOrNodePath() {
		return StringUtil.EMPTY_STRING;
	}

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {

		jsonObject.put(SRC_SCHEMA_REG, this.getSrcSchemaReg());
		jsonObject.put(SRC_TABLE_REG, this.getSrcTableReg());
		jsonObject.put(COLUNM_ACTION_DATA, this.getEventFilterNode().toJSONString());

	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
		this.setSrcSchemaReg(jsonObject.getString(SRC_SCHEMA_REG));
		this.setSrcTableReg(jsonObject.getString(SRC_TABLE_REG));

		this.setEventFilterNode(new EventFilterNode(jsonObject.getString(COLUNM_ACTION_DATA)));

	}

	public EventFilterNode getEventFilterNode() {
		return eventFilterNode;
	}

	public void setEventFilterNode(EventFilterNode eventFilterNode) {
		this.eventFilterNode = eventFilterNode;
	}

	public String getSrcSchemaReg() {
		return srcSchemaReg;
	}

	public void setSrcSchemaReg(String srcSchemaReg) {
		this.srcSchemaReg = srcSchemaReg;
	}

	public String getSrcTableReg() {
		return srcTableReg;
	}

	public void setSrcTableReg(String srcTableReg) {
		this.srcTableReg = srcTableReg;
	}

}
