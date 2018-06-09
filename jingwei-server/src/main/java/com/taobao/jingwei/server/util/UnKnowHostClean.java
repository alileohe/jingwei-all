package com.taobao.jingwei.server.util;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;

import java.util.List;

/**
 * 清楚掉无用的HOST
 * User: <a href="mailto:qihao@taobao.com">qihao</a>
 * Date: 13-4-25
 * Time: 上午10:53
 * version 1.0
 */

public class UnKnowHostClean {

    public static void main(String[] args) throws Exception {
        String zkHost = args.length > 0 ? args[0] : null;
        String taskRootPath = "/jingwei-v2/tasks";

        ZkConfigManager zkConfigManager = new ZkConfigManager();

        if (StringUtil.isNotBlank(zkHost)) {
            ZkConfig zkConfig = new ZkConfig(zkHost);
            zkConfigManager.setZkConfig(zkConfig);
        }
        zkConfigManager.init();

        List<String> taskNames = zkConfigManager.getZkClient().getChildren(taskRootPath);
        for (String taskName : taskNames) {
            String hostRootPath = taskRootPath + "/" + taskName + "/" + "hosts";
            if (zkConfigManager.getZkClient().exists(hostRootPath)) {
                List<String> hosts = zkConfigManager.getZkClient().getChildren(hostRootPath);
                for (String host : hosts) {
                    String hostPath = hostRootPath + "/" + host;
                    if (zkConfigManager.getZkClient().exists(hostPath)) {
                        String hostStatusPath = hostPath + "/" + "status";
                        boolean exists = zkConfigManager.getZkClient().exists(hostStatusPath);
                        if (!exists) {
                            zkConfigManager.delete(hostPath);
                            System.out.println("delete path: " + hostPath);
                        }
                    }
                }
            }
        }
        Runtime.getRuntime().halt(0);
    }
}