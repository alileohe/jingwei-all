package com.taobao.jingwei.common.node.applier;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AndorCommandApplierNode extends AbstractApplierNode {

	/**
	 * andor appName,andorClient ��ʼ��ʹ��
	 */
	private String appName;

	/**
	 * ��Ҫ�������µ�ӳ���ϵ
	 */
	private Map<String/**tableName**/
	, Map<String/**indexName**/
	, List<String>/**columnNameList**/
	>> cascadeIndexNameMap;

	/**
	 * json�洢����key����
	 */
	private final static String APP_NAME_KEY = "appName";
	private final static String CASACADE_INDEX_NAME_MAP_KEY = "cascadeIndexNameMap";
	private final static String APPLIER_DATA_KEY = "applierData";
	private final static String REPLACE_KEY = "replace";
	private final static String FAILCONTINUE_KEY = "failContinue";

	public AndorCommandApplierNode() {
	}

	public AndorCommandApplierNode(String applierData) {
		try {
			this.jsonStringToNodeSelf(applierData);
		} catch (JSONException e) {
			logger.error("new AndorCommandApplierNode  paser applierData Error!", e);
		}
	}

	@Override
	protected void specilizeAttributeToJsonObject(JSONObject jsonObject) throws JSONException {
		jsonObject.put(APP_NAME_KEY, StringUtil.defaultIfBlank(this.getAppName()));
		JSONObject jsonIndexMap = new JSONObject();
		if (null != this.cascadeIndexNameMap) {
			for (Map.Entry<String, Map<String, List<String>>> tableEntry : this.cascadeIndexNameMap.entrySet()) {
				String tableName = tableEntry.getKey();
				Map<String, List<String>> indexMap = tableEntry.getValue();
				JSONObject jsonIndexMapping = new JSONObject();
				for (Map.Entry<String, List<String>> columnEntry : indexMap.entrySet()) {
					String indexName = columnEntry.getKey();
					JSONArray columnArray = new JSONArray();
					for (String column : columnEntry.getValue()) {
						columnArray.put(column);
					}
					jsonIndexMapping.put(indexName, columnArray);
				}
				jsonIndexMap.put(tableName, jsonIndexMapping);
			}
		}
		jsonObject.put(CASACADE_INDEX_NAME_MAP_KEY, jsonIndexMap);
		jsonObject.put(APPLIER_DATA_KEY, StringUtil.defaultIfBlank(this.getApplierData()));
		jsonObject.put(REPLACE_KEY, this.isReplace());
		jsonObject.put(FAILCONTINUE_KEY, this.isFailContinue());

	}

	@SuppressWarnings("unchecked")
	protected void jsonObjectToSpecilizeAttribute(JSONObject jsonObject) throws JSONException {
		this.setAppName(jsonObject.getString(APP_NAME_KEY));
		this.setApplierData(jsonObject.getString(APPLIER_DATA_KEY));
		this.setFailContinue(jsonObject.getBoolean(FAILCONTINUE_KEY));
		this.setReplace(jsonObject.getBoolean(REPLACE_KEY));
		JSONObject cascadeTableIndexObject = jsonObject.getJSONObject(CASACADE_INDEX_NAME_MAP_KEY);
		if (null != cascadeTableIndexObject) {
			Map<String, Map<String, List<String>>> cascadeMap = new HashMap<String, Map<String, List<String>>>(
					cascadeTableIndexObject.length());
			Iterator<String> tableIt = cascadeTableIndexObject.keys();
			for (; tableIt.hasNext();) {
				String tableName = tableIt.next();
				JSONObject indexObject = cascadeTableIndexObject.getJSONObject(tableName);
				Map<String, List<String>> indexMap = new HashMap<String, List<String>>(indexObject.length());
				Iterator<String> indexIt = indexObject.keys();
				for (; indexIt.hasNext();) {
					String indexName = indexIt.next();
					indexMap.put(indexName, JingWeiUtil.<String> jsonArray2List(indexObject.getJSONArray(indexName)));
				}
				cascadeMap.put(tableName, indexMap);
			}
			this.cascadeIndexNameMap = cascadeMap;
		}

	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Map<String, Map<String, List<String>>> getCascadeIndexNameMap() {
		return cascadeIndexNameMap;
	}

	public void setCascadeIndexNameMap(Map<String, Map<String, List<String>>> cascadeIndexNameMap) {
		this.cascadeIndexNameMap = cascadeIndexNameMap;
	}
}
