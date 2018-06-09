package com.taobao.jingwei.core.util;

import com.taobao.tddl.jdbc.group.TGroupDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataCheck {

	@SuppressWarnings("unchecked")
	public static void dataCheck(String srcTableName, JdbcTemplate srcJt, String targetTableNme, JdbcTemplate targetJt,
			String pkName) {
		System.out.println("============DataCheck srcTable: " + srcTableName + " targetName: " + targetTableNme
				+ " Start ===========");

		RowMapper srcRowMapper = new RowMapper() {
			ResultSetMetaData rsm = null;
			public Object mapRow(final ResultSet rs, final int rowNum) throws SQLException {
				Map<String, Object> rowData = null;
				if (null == rsm) {
					rsm = rs.getMetaData();
				}
				final int columnCount = rsm.getColumnCount();
				rowData = new HashMap<String, Object>(columnCount);
				for (int i = 1; i <= columnCount; i++) {
					final String columnName = rsm.getColumnName(i);
					final Object value = rs.getObject(i);
					rowData.put(columnName, value);
				}
				return rowData;
			}
		};
		
		RowMapper targetRowMapper = new RowMapper() {
			ResultSetMetaData rsm = null;
			public Object mapRow(final ResultSet rs, final int rowNum) throws SQLException {
				Map<String, Object> rowData = null;
				if (null == rsm) {
					rsm = rs.getMetaData();
				}
				final int columnCount = rsm.getColumnCount();
				rowData = new HashMap<String, Object>(columnCount);
				for (int i = 1; i <= columnCount; i++) {
					final String columnName = rsm.getColumnName(i);
					final Object value = rs.getObject(i);
					rowData.put(columnName, value);
				}
				return rowData;
			}
		};

		List<Map<String, Object>> srcData = srcJt.query("select * from " + srcTableName, srcRowMapper);
		for (Map<String, Object> srow : srcData) {
			Object pkValue = srow.get(pkName);
			List<Map<String, Object>> targetData = targetJt.query("select * from " + targetTableNme + " where "
					+ pkName + "=?", new Object[] { pkValue }, targetRowMapper);

			if (null == targetData || targetData.isEmpty()) {
				System.out.println("pk: " + pkValue + " not found");
				continue;
			}

			for (Map.Entry<String, Object> entry : srow.entrySet()) {
				String name = entry.getKey();
				Object srcValue = entry.getValue();
				Object targetValue = targetData.get(0).get(name);

				if (null == srcValue) {
					if (null != targetValue) {
						System.out.println("pk: " + pkValue + " ColumnName: " + name + " isNot Null");
						continue;
					}
				} else {
					if (!srcValue.equals(targetValue)) {
						System.out.println("pk: " + pkValue + " ColumnName: " + name + " not  Same SrcValue: "
								+ srcValue + " targetValue: " + targetValue);
					}
				}
			}
		}
		System.out.println("============DataCheck srcTable: " + srcTableName + " targetName: " + targetTableNme
				+ " END =========== checkCount: " + srcData.size());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TGroupDataSource dataSource = new TGroupDataSource("JW_ORACLE_TEST_GROUP", "JW_ORACLE_TEST");
		dataSource.init();
		JdbcTemplate jt = new JdbcTemplate(dataSource);

		dataCheck("T_EROSA_MUTI_NUMCHAR", jt, "TARGET_EROSA_MUTI_NUMCHARDATE", jt, "ID");
		dataCheck("T_EROSA_MUTI_CHAR2", jt, "TARGET_EROSA_MUTI_CHAR2", jt, "ID");
		Runtime.getRuntime().halt(0);
	}
}
