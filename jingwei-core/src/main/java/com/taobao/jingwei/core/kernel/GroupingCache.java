package com.taobao.jingwei.core.kernel;

import java.util.HashMap;

/**
 * @desc 线程不安全，不支持多线程
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Aug 24, 2012 3:26:00 PM
 */

public class GroupingCache {
	public static final String SCHEMA_TABLE_SEP = "..";

	/** 缓存已经匹配到的 真实库名、真实表名  */
	private HashMap<String, String> cache = new HashMap<String, String>();

	/**
	* 
	* @param schema 实际库名
	* @param table 实际表名
	* @return <code>null</code> 不存在
	*/
	public String getGroupingCols(String schema, String table) {
		String key = this.getKey(schema, table);
		if (!this.cache.containsKey(key)) {
			return null;
		}

		return this.cache.get(key);
	}

	public void setGroupingCols(String schema, String table, String groupingCols) {
		String key = this.getKey(schema, table);
		this.cache.put(key, groupingCols);
	}

	private String getKey(String schema, String table) {
		String key = new StringBuilder(schema).append(SCHEMA_TABLE_SEP).append(table).toString();
		return key;
	}
}
