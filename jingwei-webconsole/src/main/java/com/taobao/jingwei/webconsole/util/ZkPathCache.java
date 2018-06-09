package com.taobao.jingwei.webconsole.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZkPathCache {

	private Map<String/**pathKey**/, List<String>/**childPathList**/> pathCache = new ConcurrentHashMap<String, List<String>>();
	
	
	public List<String> get(String key) {
		return pathCache.get(key);

	}
	
	public void put(String key, List<String> childList) {
		pathCache.put(key, childList);
	}
	
	public boolean contains(String key) {
		return pathCache.containsKey(key);

	}
	
	public void remove(String key) {
		pathCache.remove(key);

	}

	public List<String> get(String key, PageFilter filter) {
		
		List<String> listFiltered = new ArrayList<String>();
		for(String t : this.get(key)){
			if(filter.filter(t)){
				listFiltered.add(t);
			}
		}
		return listFiltered;
	}
	
	
}
