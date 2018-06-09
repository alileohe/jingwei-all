package com.taobao.jingwei.core.kernel;

import java.util.HashMap;

/**
 * @desc �̲߳���ȫ����֧�ֶ��߳�
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Aug 24, 2012 3:26:00 PM
 */

public class GroupingCache {
	public static final String SCHEMA_TABLE_SEP = "..";

	/** �����Ѿ�ƥ�䵽�� ��ʵ��������ʵ����  */
	private HashMap<String, String> cache = new HashMap<String, String>();

	/**
	* 
	* @param schema ʵ�ʿ���
	* @param table ʵ�ʱ���
	* @return <code>null</code> ������
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
