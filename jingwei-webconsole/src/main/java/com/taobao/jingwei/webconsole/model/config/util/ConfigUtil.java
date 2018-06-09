package com.taobao.jingwei.webconsole.model.config.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.common.lang.StringUtil;

/**
 * @desc
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 8, 2013 8:48:55 AM
 */

final public class ConfigUtil {

	private ConfigUtil() {
	}

	public static Set<String> commaSepString2Set(final String commaSepStr) {
		if (commaSepStr == null || commaSepStr.isEmpty()) {
			return Collections.emptySet();
		}
		Set<String> set = new HashSet<String>();
		String[] columns = commaSepStr.split(",");

		for (String column : columns) {
			set.add(column);
		}

		return set;
	}

	public static Set<String> commaSepString2Set(final String commaSepStr, boolean toLowCase) {
		if (commaSepStr == null || commaSepStr.isEmpty()) {
			return Collections.emptySet();
		}
		Set<String> set = new HashSet<String>();
		String[] columns = commaSepStr.split(",");

		for (String column : columns) {

			if (toLowCase) {
				set.add(column.toLowerCase());
			} else {
				set.add(column);
			}

		}

		return set;
	}

	public static List<String> commaSepString2List(final String commaSepStr) {
		if (commaSepStr == null || commaSepStr.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> list = new ArrayList<String>();
		String[] strs = commaSepStr.split(",");

		for (String str : strs) {
			list.add(str);
		}

		return list;
	}

	public static List<Integer> commaSepString2IntList(final String commaSepStr) {
		if (commaSepStr == null || commaSepStr.isEmpty()) {
			return Collections.emptyList();
		}
		List<Integer> list = new ArrayList<Integer>();
		String[] strs = commaSepStr.split(",");

		for (String str : strs) {
			list.add(Integer.valueOf(str));
		}

		return list;
	}

	public static String collection2CommaSepStr(final Collection<String> strList) {

		if (strList == null || strList.isEmpty()) {
			return StringUtil.EMPTY_STRING;
		}

		StringBuilder sb = new StringBuilder();
		for (String str : strList) {
			sb.append(str).append(",");
		}

		return sb.toString();
	}

	public static InputStream string2InputStream(String str) {
		ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes());
		return stream;
	}

	/**
	 * 
	 * @param mapStr colSource->colTarget,......
	 * @return
	 */
	public static Map<String, String> getTableMap(String mapStr) {
		Map<String, String> map = new HashMap<String, String>();

		List<String> entrys = commaSepString2List(mapStr);

		for (String str : entrys) {
			String[] en = str.split("->");
			map.put(en[0], en[1]);
		}

		return map;
	}

	/**
	 * 
	 * @return
	 */
	public static List<String> map2List(Map<String, String> map, String deli) {

		if (map.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> list = new ArrayList<String>();
		for (Map.Entry<String, String> en : map.entrySet()) {
			list.add(new StringBuilder(en.getKey()).append(deli).append(en.getValue()).toString());
		}

		return list;
	}

	/**
	 * 
	 * @param map
	 * @return
	 */
	public static List<String> map2List(Map<String, String> map) {
		return map2List(map, ":");
	}

	/**
	 * @param request
	 * @param controlName
	 * @return
	 */
	public static Boolean isChecked(HttpServletRequest request, String controlName) {
		return "on".equalsIgnoreCase(request.getParameter(controlName));
	}

	public static String getMysqlBinlogPosition(String fileName, Long binlogOffset, Long masterId) {
		String binlogSuffix = fileName.substring(fileName.lastIndexOf('.') + 1);

		StringBuilder buf = new StringBuilder();
		buf.append(binlogSuffix);
		buf.append(':');
		buf.append(binlogOffset);
		if (masterId != 0) {
			buf.append('#');
			buf.append(((long) masterId & 0xffffffffL));
		}

		return buf.toString();
	}
}
