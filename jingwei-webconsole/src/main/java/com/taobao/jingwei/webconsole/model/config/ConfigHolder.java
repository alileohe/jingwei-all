package com.taobao.jingwei.webconsole.model.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @desc  配置信息的模板
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 8, 2013 10:18:05 AM
 */

public class ConfigHolder {

	private String commonConfig;
	private String commonFilterConfig;

	private String metaApplierConfig;
	private String multiMetaApplierConfig;
	private String databaseApplierConfig;

	private Map<String, String> extractors = new HashMap<String, String>();
	private Map<String, String> appliers = new HashMap<String, String>();

	private String metaExtractorConfig;
	private String mysqlExtractorConfig;
	private String oracleExtractorConfig;

	private String mysqlExtractorProps;

	// 默认批量配置
	private String defaultBatchConfig;

	// 分区grouping
	private String groupingSetting;

	// table map
	private String tableMap;
	
	// 列过滤条件
	private String columnFilter;


	private static String COMMON_CONFIG_TEMPLATE = "common-config.xml";
	private static String COMMON_FILTER_CONFIG_TEMPLATE = "common-filter-config.xml";
	private static String META_APPLIER_CONFIG_TEMPLATE = "meta-applier-config.xml";
	private static String MULTI_META_APPLIER_CONFIG_TEMPLATE = "multi-meta-applier-config.xml";
	private static String DATABASE_APPLIER_CONFIG_TEMPLATE = "database-applier-config.xml";
	private static String META_EXTRACTOR_CONFIG_TEMPLATE = "meta-extractor-config.xml";
	private static String MYSQL_EXTRACTOR_CONFIG_TEMPLATE = "mysql-extractor-config.xml";
	private static String ORACLE_EXTRACTOR_CONFIG_TEMPLATE = "oracle-extractor-config.xml";
	private static String MYSQL_EXTRACTOR_PROPS = "replicator-new.properties";
	private static String COLUMN_FILTER_PROPS = "column_filter.ini";

	// 批量配置文件
	private static String DEFAULT_BATCH_CONFIG_TEMPLATE = "default-batch-factory-config.xml";

	// 分区grouping
	private static String GROUP_SETTING_TEMPKLATE = "grouping.ini";

	// database 的table map
	private static String TABLE_MAP_TEMPLATE = "tablemap.ini";

	private static ConfigHolder configHolder;

	public static ConfigHolder getInstance() {
		if (configHolder == null) {
			configHolder = new ConfigHolder();
			configHolder.init();
		}

		return configHolder;
	}

	public void init() {
		if (commonConfig == null) {
			commonConfig = readTemplateString(COMMON_CONFIG_TEMPLATE);
		}
		if (commonFilterConfig == null) {
			commonFilterConfig = readTemplateString(COMMON_FILTER_CONFIG_TEMPLATE);
		}
		if (metaApplierConfig == null) {
			metaApplierConfig = readTemplateString(META_APPLIER_CONFIG_TEMPLATE);
		}
		if (multiMetaApplierConfig == null) {
			multiMetaApplierConfig = readTemplateString(MULTI_META_APPLIER_CONFIG_TEMPLATE);
		}
		if (databaseApplierConfig == null) {
			databaseApplierConfig = readTemplateString(DATABASE_APPLIER_CONFIG_TEMPLATE);
		}
		if (metaExtractorConfig == null) {
			metaExtractorConfig = readTemplateString(META_EXTRACTOR_CONFIG_TEMPLATE);
		}
		if (mysqlExtractorConfig == null) {
			mysqlExtractorConfig = readTemplateString(MYSQL_EXTRACTOR_CONFIG_TEMPLATE);
		}
		if (oracleExtractorConfig == null) {
			oracleExtractorConfig = readTemplateString(ORACLE_EXTRACTOR_CONFIG_TEMPLATE);
		}
		if (this.mysqlExtractorProps == null) {
			this.mysqlExtractorProps = readTemplateString(MYSQL_EXTRACTOR_PROPS);
		}
		// 默认批量配置
		if (this.defaultBatchConfig == null) {
			this.defaultBatchConfig = readTemplateString(DEFAULT_BATCH_CONFIG_TEMPLATE);
		}

		// 分组
		if (this.groupingSetting == null) {
			this.groupingSetting = readTemplateString(GROUP_SETTING_TEMPKLATE);
		}

		// table map
		if (this.tableMap == null) {
			this.tableMap = readTemplateString(TABLE_MAP_TEMPLATE);
		}
		
		if (this.columnFilter == null ) {
			this.columnFilter = readTemplateString(COLUMN_FILTER_PROPS);
		}

		this.appliers.put(META_APPLIER_CONFIG_TEMPLATE, this.getMetaApplierConfig());
		this.appliers.put(MULTI_META_APPLIER_CONFIG_TEMPLATE, this.getMultiMetaApplierConfig());
		this.appliers.put(DATABASE_APPLIER_CONFIG_TEMPLATE, this.getDatabaseApplierConfig());

		this.extractors.put(META_EXTRACTOR_CONFIG_TEMPLATE, this.getMetaExtractorConfig());
		this.extractors.put(MYSQL_EXTRACTOR_CONFIG_TEMPLATE, this.getMysqlExtractorConfig());
		this.extractors.put(ORACLE_EXTRACTOR_CONFIG_TEMPLATE, this.getOracleExtractorConfig());
	}

	public Map<String, String> getExtractors() {
		return extractors;
	}

	public void setExtractors(Map<String, String> extractors) {
		this.extractors = extractors;
	}

	public Map<String, String> getAppliers() {
		return appliers;
	}

	public void setAppliers(Map<String, String> appliers) {
		this.appliers = appliers;
	}

	public String getCommonConfig() {

		return commonConfig;
	}

	public String getCommonFilterConfig() {

		return commonFilterConfig;
	}

	public String getMetaApplierConfig() {

		return metaApplierConfig;
	}

	public String getMultiMetaApplierConfig() {

		return multiMetaApplierConfig;
	}

	public String getDatabaseApplierConfig() {

		return databaseApplierConfig;
	}

	public String getMetaExtractorConfig() {

		return metaExtractorConfig;
	}

	public String getMysqlExtractorConfig() {

		return mysqlExtractorConfig;
	}

	public String getOracleExtractorConfig() {

		return oracleExtractorConfig;
	}

	public String getMysqlExtractorProps() {
		return mysqlExtractorProps;
	}

	public void setMysqlExtractorProps(String mysqlExtractorProps) {
		this.mysqlExtractorProps = mysqlExtractorProps;
	}

	public String getDefaultBatchConfig() {
		return defaultBatchConfig;
	}

	public void setDefaultBatchConfig(String defaultBatchConfig) {
		this.defaultBatchConfig = defaultBatchConfig;
	}

	public String getTableMap() {
		return tableMap;
	}

	public void setTableMap(String tableMap) {
		this.tableMap = tableMap;
	}

	public String getGroupingSetting() {
		return groupingSetting;
	}

	public void setGroupingSetting(String groupingSetting) {
		this.groupingSetting = groupingSetting;
	}
	
	public String getColumnFilter() {
		return columnFilter;
	}

	public void setColumnFilter(String columnFilter) {
		this.columnFilter = columnFilter;
	}

	private static String readTemplateString(String filename) {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(ConfigHolder.class.getClassLoader().getResource(filename)
					.getFile()));
			String tmp = null;
			while ((tmp = reader.readLine()) != null) {
				sb.append(tmp);
				sb.append(System.getProperty("line.separator"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
		return sb.toString().trim();
	}
}
