package com.taobao.jingwei.common.node.type;

/**
 * Class ApplierType
 * <p/>
 * Applier ¿‡–Õ√∂æŸ
 *
 * @author qihao <dqylyln@gmail.com>
 * @since 11-11-15
 */
public enum ApplierType {

    DATABASE_APPLIER(1),

    META_APPLIER(2),

    CUSTOM_APPLIER(3),
    
    MULTI_META_APPLIER(4),
    
    ANDOR_COMMAND_APPLIER(5);

    private int type;

    ApplierType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static ApplierType getEnumByType(int type) {
        ApplierType applierType = null;

        for (ApplierType at : ApplierType.values()) {
            if (at.getType() == type) {
                applierType = at;
                break;
            }
        }
        return applierType;
    }
}
