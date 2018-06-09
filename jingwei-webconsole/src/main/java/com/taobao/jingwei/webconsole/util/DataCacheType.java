package com.taobao.jingwei.webconsole.util;


public enum DataCacheType {

	JingweiAssembledTask(1),

	JingweiAssembledServer(2),

	JingweiAssembledMonitor(3),
	
	JingweiAssembledGroup(4),
    
	SyncTaskNode(5);

    private int type;

    DataCacheType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static DataCacheType getEnumByType(int type) {
    	DataCacheType dataType = null;

        for (DataCacheType at : DataCacheType.values()) {
            if (at.getType() == type) {
            	dataType = at;
                break;
            }
        }
        return dataType;
    }
}
