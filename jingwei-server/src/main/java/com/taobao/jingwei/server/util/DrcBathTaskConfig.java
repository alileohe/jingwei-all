package com.taobao.jingwei.server.util;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.config.impl.zk.ZkConfig;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.node.extractor.DrcExtractorNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.ExtractorType;

import java.util.*;

/**
 * User: <a href="mailto:qihao@taobao.com">qihao</a>
 * Date: 13-4-7
 * Time: 下午5:00
 * version 1.0
 */
public class DrcBathTaskConfig {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Please Set ConfigFile and TemplateFile !");
            return;
        }
        String configFile = args[0];
        //从文件中后去PROP对象
        Properties confProp = JingWeiUtil.getPropFromFile(configFile);
        if (null == confProp) {
            System.out.println("configFile not exit Or configFile is Empty!");
            return;
        }

        //搞出来DRC模板的PROP文件
        Properties drcProp = new Properties();
        Set<Map.Entry<Object, Object>> entrySet = confProp.entrySet();
        for (Map.Entry<Object, Object> entry : entrySet) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.startsWith("te_")) {
                String drcKey = StringUtil.substringAfter(key, "te_");
                drcProp.put(drcKey, value);
            }
        }

        String[] dbLists = StringUtil.split(confProp.getProperty("taskDbList"), ",");
        int startIndex = Integer.valueOf(confProp.getProperty("taskStartIndex"));
        int taskSetp = Integer.valueOf(confProp.getProperty("taskSetp"));
        String taskPrefix = confProp.getProperty("taskPrefix");
        String taskDesc = confProp.getProperty("taskDesc");
        String taskJavaOpt = confProp.getProperty("taskJavaOpt");
        int resetPosition = Integer.valueOf(confProp.getProperty("resetPosition", "0"));

        List<SyncTaskNode> taskNodeList = new ArrayList<SyncTaskNode>(dbLists.length);
        for (int i = 0; i < dbLists.length; i++) {
            String taskName = taskPrefix + "-" + startIndex;
            drcProp.put("dbName", dbLists[i]);
            SyncTaskNode taskNode = new SyncTaskNode();
            taskNode.setName(taskName);
            taskNode.setExtractorType(ExtractorType.DRC_EXTRACTOR);
            DrcExtractorNode drcNode = new DrcExtractorNode(drcProp);
            taskNode.setExtractorData(drcNode.toJSONString());
            taskNode.setApplierType(ApplierType.CUSTOM_APPLIER);

            if (StringUtil.isNotBlank(taskDesc)) {
                taskNode.setDesc(taskDesc);
            }
            if (StringUtil.isNotBlank(taskJavaOpt)) {
                taskNode.setJavaOpt(taskJavaOpt);
            }
            taskNodeList.add(taskNode);
            startIndex = startIndex + taskSetp;
        }
        //初始化ZK
        String zkHost = confProp.getProperty("zkHost");
        ZkConfig zkConfig = new ZkConfig(zkHost);
        ZkConfigManager zkConfigManager = new ZkConfigManager();
        zkConfigManager.setZkConfig(zkConfig);
        zkConfigManager.init();

        for (SyncTaskNode taskNode : taskNodeList) {
            System.out.println("publish Task: " + taskNode.getName());
            zkConfigManager.publishOrUpdateData(taskNode.getDataIdOrNodePath(), taskNode.toJSONString(), taskNode.isPersistent());
            if (resetPosition > 0) {
                String positionPath = taskNode.getDataIdOrNodePath() + "/lastComit";
                System.out.println("reset Position: " + positionPath);
                zkConfigManager.delete(positionPath);
            }
        }
    }
}
