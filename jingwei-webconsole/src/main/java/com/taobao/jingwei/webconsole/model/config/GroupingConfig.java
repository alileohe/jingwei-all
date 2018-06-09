package com.taobao.jingwei.webconsole.model.config;

import java.io.Serializable;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 8, 2013 12:05:29 PM
 */

public class GroupingConfig implements Serializable {

	private static final long serialVersionUID = 1L;
	private String schemaReg;
	private String tableReg;
	/** �ֿ�� ������ʹ�ö��ŷָ� */
	private String fields;

	public String getFields() {
		return fields;
	}

	public void setFields(String fields) {
		this.fields = fields;
	}

	public String getSchemaReg() {
		return schemaReg;
	}

	public void setSchemaReg(String schemaReg) {
		this.schemaReg = schemaReg;
	}

	public String getTableReg() {
		return tableReg;
	}

	public void setTableReg(String tableReg) {
		this.tableReg = tableReg;
	}
}