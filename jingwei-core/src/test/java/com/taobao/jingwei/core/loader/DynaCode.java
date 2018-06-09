package com.taobao.jingwei.core.loader;

import com.taobao.tddl.dbsync.dbms.DBMSRowChange;

public class DynaCode {

	private static final int FROM_GROUP_B2B = 1;

	public DBMSRowChange convert(DBMSRowChange event) throws Exception {

		DBMSRowChange changeEvent = (DBMSRowChange) event;

		int rowCount = changeEvent.getRowSize();
		for (int i = 1; i < rowCount + 1; i++) {
			int from_group = (Integer) (changeEvent.getRowValue(i, "from_group"));
			if (from_group != FROM_GROUP_B2B) {
				changeEvent.removeRowData(i);
			}
		}

		if (changeEvent.getRowSize() == 0) {
			return null;
		} else {
			return changeEvent;
		}
	}
}