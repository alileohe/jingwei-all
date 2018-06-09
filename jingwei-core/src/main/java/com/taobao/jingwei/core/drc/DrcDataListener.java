package com.taobao.jingwei.core.drc;

import com.taobao.drc.sdk.message.DataMessage;

/**
 * User: <a href="mailto:qihao@taobao.com">qihao</a>
 * Date: 13-2-14
 * Time: ����8:07
 * version 1.0
 */
public interface DrcDataListener {
    public void onDataChange(DataMessage dataMessage,String dbName) throws Exception;
}
