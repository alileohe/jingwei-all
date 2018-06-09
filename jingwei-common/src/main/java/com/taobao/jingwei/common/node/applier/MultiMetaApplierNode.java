package com.taobao.jingwei.common.node.applier;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @desc �����Ķ��meta applier
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Mar 20, 2012 3:19:11 PM
 */

public class MultiMetaApplierNode extends AbstractApplierNode {

	/** �����Ķ��ٸ�meta applier */
	private volatile List<SubMetaApplierNode> subMetaApplierNodes = Collections
			.emptyList();

	private static final String META_APPLIERS = "META_APPLIERS";

	public MultiMetaApplierNode() {
	}

	public MultiMetaApplierNode(String jsonStr) {
		try {
			this.jsonStringToNodeSelf(jsonStr);
		} catch (JSONException e) {
			logger.error("new MultiMetaApplierNode  paser applierData Error!",
					e);
		}
	}

	@Override
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject)
			throws JSONException {
		JSONArray array = jsonObject.getJSONArray(META_APPLIERS);
		int length = array.length();
		if (0 == length) {
			return;
		}
		List<SubMetaApplierNode> metaApplierNodes = new ArrayList<SubMetaApplierNode>(
				length);
		for (int i = 0; i < length; i++) {
			SubMetaApplierNode subMetaApplierNode = new SubMetaApplierNode(
					array.getString(i));
			metaApplierNodes.add(subMetaApplierNode);
		}
		this.setSubMetaApplierNodes(metaApplierNodes);
	}

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject)
			throws JSONException {
		JSONArray array = new JSONArray();
		for (SubMetaApplierNode metaApplierNode : subMetaApplierNodes) {
			array.put(metaApplierNode.toJSONString());
		}
		jsonObject.put(META_APPLIERS, array);
	}

	public List<SubMetaApplierNode> getSubMetaApplierNodes() {
		return subMetaApplierNodes;
	}

	public void setSubMetaApplierNodes(
			List<SubMetaApplierNode> subMetaApplierNodes) {
		this.subMetaApplierNodes = subMetaApplierNodes;
	}
}
