package com.taobao.jingwei.webconsole.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.alibaba.common.lang.StringUtil;

public class EnvDataCache {

	private Map<String/**zk environment**/,ZkPathCache> zkPathCacheMap;
	
	private List<String> keyList;
	
	public EnvDataCache(String keyHosts) {
		if (keyHosts == null) {
			throw new NullPointerException("Missing config of 'zk.server.hosts'. ZK Host can not be null");
		}
		keyList = new ArrayList<String>();
		zkPathCacheMap = new HashMap<String, ZkPathCache>();
		
		String[] keyString = StringUtil.split(keyHosts, ";");
		for (int i = 0; i < keyString.length; i++) {
			keyList.add(keyString[i].split("\\|")[0]);
			zkPathCacheMap.put(keyString[i].split("\\|")[0], new ZkPathCache());
//			cacheMap.put(String.valueOf(i), new DataCache());
		}
		
	}
	

	public ZkPathCache getZkPathCache(String envKey){
		if(StringUtils.isBlank(envKey)){
			return this.zkPathCacheMap.get(keyList.get(0));
		}else{
			if (NumberUtils.isDigits(envKey.toString())) {
				return this.zkPathCacheMap.get(keyList.get(Integer.parseInt(envKey)-1));
			}
			return this.zkPathCacheMap.get(envKey);
		}
	}

}
