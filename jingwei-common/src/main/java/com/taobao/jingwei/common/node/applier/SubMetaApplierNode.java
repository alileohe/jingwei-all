package com.taobao.jingwei.common.node.applier;

import com.alibaba.common.lang.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @desc
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 20, 2012 6:40:32 PM
 */

public class SubMetaApplierNode extends MetaApplierNode {

	private String srcSchemaReg;

	private String srcTableReg;

	private static final String SRC_SCHEMA_REG = "SRC_SCHEMA_REG";
	private static final String SRC_TABLE_REG = "SRC_TABLE_REG";

	public SubMetaApplierNode() {
	}

	public SubMetaApplierNode(String jsonStr) {
		try {
			this.jsonStringToNodeSelf(jsonStr);
		} catch (JSONException e) {
			logger.error("new SubMetaApplierNode  paser applierData Error!", e);
		}
	}

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject)
			throws JSONException {
		super.specilizeAttributeToJsonObject(jsonObject);
		jsonObject.put(SRC_SCHEMA_REG, this.getSrcSchemaReg());
		jsonObject.put(SRC_TABLE_REG, this.getSrcTableReg());
	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject)
			throws JSONException {
		super.jsonObjectToSpecilizeAttribute(jsonObject);
		this.setSrcSchemaReg(StringUtil.defaultIfBlank(jsonObject
				.getString(SRC_SCHEMA_REG)));
		this.setSrcTableReg(StringUtil.defaultIfBlank(jsonObject
				.getString(SRC_TABLE_REG)));
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