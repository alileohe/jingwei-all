package com.taobao.jingwei.core.loader;

import com.taobao.jingwei.core.util.RowChangeBuilder;
import com.taobao.tddl.dbsync.dbms.DBMSAction;
import com.taobao.tddl.dbsync.dbms.DBMSColumn;
import com.taobao.tddl.dbsync.dbms.DBMSRowChange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DyanSourceCode {

	public DBMSRowChange convert(DBMSRowChange event) throws Exception {
		DBMSAction action = event.getAction();
		RowChangeBuilder builder = RowChangeBuilder.createBuilder("test_schema", "test_table", action);

		DBMSColumn c1 = event.getColumnSet().findColumn("gift_receiver_id");
		DBMSColumn c2 = event.getColumnSet().findColumn("postscript");
		DBMSColumn c3 = event.getColumnSet().findColumn("creator");
		DBMSColumn c4 = event.getColumnSet().findColumn("gmt_create");
		DBMSColumn c5 = event.getColumnSet().findColumn("text_1");

		List<DBMSColumn> metaColumnList = new ArrayList<DBMSColumn>(5);
		metaColumnList.add(c1);
		metaColumnList.add(c2);
		metaColumnList.add(c3);
		metaColumnList.add(c4);
		metaColumnList.add(c5);
		builder.addMetaColumns(metaColumnList);

		int rowSize = event.getRowSize();
		List<? extends DBMSColumn> changeColumns = event.getChangeColumns();
		if (DBMSAction.UPDATE == action) {
			boolean hasUpdate = false;
			for (DBMSColumn changeColumn : metaColumnList) {
				if (changeColumns.contains(changeColumn)) {
					hasUpdate = true;
					break;
				}
			}
			if (!hasUpdate) {
				return null;
			}
		}
		for (int i = 1; i <= rowSize; i++) {
			if (DBMSAction.UPDATE == action) {
				Map<String, Serializable> changeRowData = new HashMap<String, Serializable>(changeColumns.size());
				if (changeColumns.contains(c1)) {
					changeRowData.put(c1.getName(), event.getChangeValue(i, c1));
				}
				if (changeColumns.contains(c2)) {
					changeRowData.put(c2.getName(), event.getChangeValue(i, c2));
				}
				if (changeColumns.contains(c3)) {
					changeRowData.put(c3.getName(), event.getChangeValue(i, c3));
				}
				if (changeColumns.contains(c1)) {
					changeRowData.put(c4.getName(), event.getChangeValue(i, c4));
				}
				if (changeColumns.contains(c5)) {
					changeRowData.put(c5.getName(), event.getChangeValue(i, c5));
				}
				builder.addChangeRowData(changeRowData);
			}
			Map<String, Serializable> rowData = new HashMap<String, Serializable>(5);
			rowData.put(c1.getName(), event.getRowValue(i, c1));
			rowData.put(c2.getName(), event.getRowValue(i, c2));
			rowData.put(c3.getName(), event.getRowValue(i, c3));
			rowData.put(c4.getName(), event.getRowValue(i, c4));
			rowData.put(c5.getName(), event.getRowValue(i, c5));
			builder.addRowData(rowData);
		}
		return builder.build();
	}
}