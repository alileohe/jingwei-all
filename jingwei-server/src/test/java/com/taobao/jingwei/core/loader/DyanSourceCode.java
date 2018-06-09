package com.taobao.jingwei.core.loader;

import com.taobao.tddl.dbsync.dbms.DBMSRowChange;

public class DyanSourceCode {

	public DBMSRowChange convert(DBMSRowChange event) throws Exception {
		DBMSRowChange changeEvent = (DBMSRowChange) event;

		int rowCount = changeEvent.getRowSize();
		for (int i = 1; i < rowCount + 1; i++) {
			String gift_receiver_id = (String) (changeEvent.getRowValue(i , "gift_receiver_id"));
			if (gift_receiver_id.hashCode() % 5 != 0) {
				changeEvent.removeRowData(i + 1);
			}
		}

		if (changeEvent.getRowSize() == 0) {
			return null;
		} else {
			return changeEvent;
		}
	}
}