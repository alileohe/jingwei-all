package com.taobao.jingwei.webconsole.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taobao.jingwei.common.node.applier.AndorCommandApplierNode;

public class JingweiApplierAndorCommandType {
	
	private String andorTableMapping;
	private String appName;

	public String getAndorTableMapping() {
		return andorTableMapping;
	}
	public void setAndorTableMapping(String andorTableMapping) {
		this.andorTableMapping = andorTableMapping;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	public AndorCommandApplierNode getAndorCommandApplierNode() throws Exception{
		
		AndorCommandApplierNode node = new AndorCommandApplierNode();
		node.setAppName(this.getAppName());
		
		Map<String, Map<String, List<String>>> cascadeMap = new HashMap<String, Map<String,List<String>>>();
		String [] tableArr = andorTableMapping.split("\\?");
		for(String tableItem:tableArr){
			if(tableItem.split("\\|").length!=2){
				throw new Exception("配置信息不匹配，必须包含表明和indexName");
			}
			String tableName = tableItem.split("\\|")[0].trim();
			String [] indexList = tableItem.split("\\|")[1].split(";");
			Map<String, List<String>> indexMap = new HashMap<String, List<String>>();
			for(String indexItem:indexList){
				String[] itemArr =indexItem.split("->");
				String indexName;
				List<String> colList = new ArrayList<String>();
				if(itemArr.length==2){
					indexName = itemArr[0].trim();
					for(String col:itemArr[1].split(",")){
						colList.add(col.trim());
					}
				}else if(itemArr.length==1){
					indexName = itemArr[0];
				}else{
					throw new Exception("andorTableMap does't match");
				}
				indexMap.put(indexName, colList);
			}
			cascadeMap.put(tableName, indexMap);
		}
		
		node.setCascadeIndexNameMap(cascadeMap);
		
		return node;
	}
	

}
