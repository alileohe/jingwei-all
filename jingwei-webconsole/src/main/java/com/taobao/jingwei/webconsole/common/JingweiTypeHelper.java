package com.taobao.jingwei.webconsole.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.taobao.jingwei.common.node.StatusNode.StatusEnum;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.CompressionType;
import com.taobao.jingwei.common.node.type.DBType;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.webconsole.model.DBSyncSwitchPolicy;
import com.taobao.jingwei.webconsole.model.config.JingweiBatch.BatchPolicy;

public class JingweiTypeHelper {
	private static Map<String, Integer> extractorType = new LinkedHashMap<String, Integer>(
			ExtractorType.values().length);
	private static Map<String, Integer> applierType = new LinkedHashMap<String, Integer>(ApplierType.values().length);
	private static Map<String, String> statusType = new LinkedHashMap<String, String>(StatusEnum.values().length);
	private static Map<String, Integer> dbType = new LinkedHashMap<String, Integer>(DBType.values().length);
	private static List<String> dbsyncSwithPolicyType = new LinkedList<String>();
	private static List<String> interval = new LinkedList<String>();
	private static List<String> batchCreateType = new ArrayList<String>();

	private static List<String> compressionType = new LinkedList<String>();

	public static Map<String, Integer> getDbType() {
		if (dbType.isEmpty()) {
			synchronized (dbType) {
				dbType.clear();
				for (DBType db : DBType.values()) {
					dbType.put(db.name(), db.getType());
				}
			}
		}
		return dbType;
	}

	public static List<String> getCompressionType() {
		if (compressionType.isEmpty()) {
			synchronized (compressionType) {
				compressionType.clear();
				for (CompressionType ct : CompressionType.values()) {
					compressionType.add(ct.name());
				}
			}
		}
		return compressionType;
	}

	public static List<String> getDBSyncSwitchPolicyType() {
		if (dbsyncSwithPolicyType.isEmpty()) {
			synchronized (dbsyncSwithPolicyType) {
				dbsyncSwithPolicyType.clear();
				for (DBSyncSwitchPolicy policy : DBSyncSwitchPolicy.values()) {
					dbsyncSwithPolicyType.add(policy.name());
				}
			}
		}
		return dbsyncSwithPolicyType;
	}

	public static Map<String, Integer> getExtractorType() {
		if (extractorType.isEmpty()) {
			synchronized (extractorType) {
				extractorType.clear();
				for (ExtractorType et : ExtractorType.values()) {
					extractorType.put(et.name(), et.getType());
				}
			}
		}
		return extractorType;
	}

	public static Map<String, Integer> getApplierType() {
		if (applierType.isEmpty()) {
			synchronized (applierType) {
				applierType.clear();
				for (ApplierType at : ApplierType.values()) {
					applierType.put(at.name(), at.getType());
				}
				
				// 去掉meta applier，multi meta 已经包含了
				applierType.remove(ApplierType.META_APPLIER.name());
			}
		}
		return applierType;
	}

	public static Map<String, String> getStatusType() {
		if (statusType.isEmpty()) {
			synchronized (statusType) {
				statusType.clear();
				for (StatusEnum se : StatusEnum.values()) {
					statusType.put(se.name(), se.getStatusString());
				}
			}
		}
		return statusType;
	}

	public static String listToString(List<String> list) {
		if (list.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String s : list) {
			sb.append(",");
			sb.append(s);
		}
		return sb.substring(1);
	}

	public static List<String> getInterval() {
		if (interval.isEmpty()) {
			synchronized (interval) {
				interval.add("10s");
				interval.add("30s");
				interval.add("1m");
				interval.add("5m");
				interval.add("30m");
				interval.add("1h");
				interval.add("5h");
				interval.add("12h");
				interval.add("20h");
				interval.add("1d");
			}
		}
		return interval;
	}

	public static List<String> getBatchCreateType() {
		synchronized (batchCreateType) {
			if (batchCreateType.isEmpty()) {

				for (BatchPolicy p : BatchPolicy.values()) {
					batchCreateType.add(p.toString());
				}
			}
		}

		return batchCreateType;
	}
}
