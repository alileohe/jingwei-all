package com.taobao.jingwei.webconsole.util;

import java.util.Properties;

public class DataUtil {
	public static String propertesToString(Properties pro) {
		if (pro != null) {
			StringBuilder sb = new StringBuilder();
			for (String name : pro.stringPropertyNames()) {
				sb.append(name);
				sb.append("=");
				sb.append(pro.getProperty(name));
				sb.append(System.getProperty("line.separator"));
			}
			return sb.toString();
		}
		return null;
	}
}
