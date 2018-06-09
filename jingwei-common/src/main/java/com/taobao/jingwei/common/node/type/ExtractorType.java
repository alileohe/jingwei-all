package com.taobao.jingwei.common.node.type;

/**
 * Class ExtractorType
 * <p/>
 * Extractor的类型枚举
 *
 * @author qihao <dqylyln@gmail.com>
 * @since 11-11-15
 */
public enum ExtractorType {

    BINLOG_EXTRACTOR(1), META_EXTRACTOR(2), CUSTOM_EXTRACTOR(3), ORACLE_EXTRACTOR(4), DRC_EXTRACTOR(5);

    private int type;

    ExtractorType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static ExtractorType getEnumByType(int type) {
        ExtractorType extractorType = null;
        for (ExtractorType et : ExtractorType.values()) {
            if (et.getType() == type) {
                extractorType = et;
                break;
            }
        }
        return extractorType;
    }
}
