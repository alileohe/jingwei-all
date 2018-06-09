package com.taobao.jingwei.webconsole.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.taobao.jingwei.common.node.tasks.SyncTaskNode.GroupingSetting;

/**
 * @desc 
 * 
 * @author 朔海 <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Aug 23, 2012 8:35:22 PM
 */

public class GroupingEntry {
	private String[] schemaReg;
	private String[] tableReg;
	/** 分库键 如果多个使用逗号分隔 */
	private String[] fields;

	public String[] getSchemaReg() {
		return schemaReg;
	}

	public void setSchemaReg(String[] schemaReg) {
		this.schemaReg = schemaReg;
	}

	public String[] getTableReg() {
		return tableReg;
	}

	public void setTableReg(String[] tableReg) {
		this.tableReg = tableReg;
	}

	public String[] getFields() {
		return fields;
	}

	public void setFields(String[] fields) {
		this.fields = fields;
	}

	public List<GroupingSetting> getGroupingSetting() {
		if (schemaReg == null || schemaReg.length == 0) {
			return Collections.emptyList();
		}
		int length = schemaReg.length;
		List<GroupingSetting> list = new ArrayList<GroupingSetting>();

		for (int i = 0; i < length; i++) {
			GroupingSetting setting = new GroupingSetting();
			setting.setSchemaReg(schemaReg[i]);
			setting.setTableReg(tableReg[i]);
			setting.setFields(fields[i]);

			list.add(setting);
		}

		return list;
	}
}
