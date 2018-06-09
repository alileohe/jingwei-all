package com.taobao.jingwei.common.node.type;

/**
 * Class DBType
 *
 * DB Applier的数据库类型枚举
 *
 * @author qihao <dqylyln@gmail.com>
 * @since 11-11-15
 */
public enum DBType {
	MYSQL(1),

	ORACLE(2),

	ANDOR(3);

	private int type;

	DBType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public static DBType getEnumByType(int type) {
		DBType dbType = null;
		for (DBType dt : DBType.values()) {
			if (dt.getType() == type) {
				dbType = dt;
				break;
			}
		}
		return dbType;
	}
}
