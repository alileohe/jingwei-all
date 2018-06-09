package com.taobao.jingwei.core.util;

import com.taobao.tddl.dbsync.dbms.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Apr 18, 2012 2:00:41 PM
 */

public class DBMSRowChangeHelper implements ColumnFilterTestConstant {
	private static DBMSColumn createDBMSColumn(String column) {
		String name = column;
		int ordinalIndex = 0;
		int sqlType = 1;
		boolean signed = false;
		boolean nullable = false;
		boolean primaryKey = true;

		DBMSColumn newColumn = new DefaultColumn(name, ordinalIndex, sqlType, signed, nullable, primaryKey);

		return newColumn;
	}

	private static List<DBMSColumn> getDBMSColumns() {
		List<DBMSColumn> cloumns = new ArrayList<DBMSColumn>();

		cloumns.add(DBMSRowChangeHelper.createDBMSColumn(INCLUDE_COLUMN_1));
		cloumns.add(DBMSRowChangeHelper.createDBMSColumn(INCLUDE_COLUMN_2));

		return cloumns;
	}

	private static DefaultColumnSet getDefaultColumnSet() {
		return new DefaultColumnSet(DBMSRowChangeHelper.getDBMSColumns());
	}

	public static DefaultRowChange getDefaultRowChange(DBMSAction action) {
		DefaultRowChange defaultRowChange = new DefaultRowChange(action, SCHEMA_1, TABLE_1,
				DBMSRowChangeHelper.getDefaultColumnSet());

		for (int row = 1; row <= 2; row++) {
			for (int col = 1; col <= 2; col++) {
				defaultRowChange.setRowValue(row, col, row + col);
			}
		}

		if (action == DBMSAction.UPDATE) {
			for (int row = 1; row <= 2; row++) {
				for (int col = 1; col <= 2; col++) {
					defaultRowChange.setChangeValue(row, col, row * col);
				}
			}
		}

		return defaultRowChange;
	}
}
