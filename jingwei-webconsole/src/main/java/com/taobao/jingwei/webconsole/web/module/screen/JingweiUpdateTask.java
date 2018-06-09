package com.taobao.jingwei.webconsole.web.module.screen;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.node.PositionNode;
import com.taobao.jingwei.common.node.applier.*;
import com.taobao.jingwei.common.node.applier.EventFilterNode.ColumnFilterConditionNode;
import com.taobao.jingwei.common.node.extractor.BinLogExtractorNode;
import com.taobao.jingwei.common.node.extractor.DrcExtractorNode;
import com.taobao.jingwei.common.node.extractor.MetaExtractorNode;
import com.taobao.jingwei.common.node.extractor.OracleExtractorNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode.GroupingSetting;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.webconsole.biz.ao.JingweiTaskAO;
import com.taobao.jingwei.webconsole.biz.manager.JingweiZkConfigManager;
import com.taobao.jingwei.webconsole.common.JingweiTypeHelper;
import com.taobao.jingwei.webconsole.common.JingweiWebConsoleConstance;
import com.taobao.jingwei.webconsole.model.JingweiApplierMeta;
import com.taobao.jingwei.webconsole.model.JingweiApplierMultiMeta;
import com.taobao.jingwei.webconsole.model.JingweiBinlogExtractor;
import com.taobao.jingwei.webconsole.model.JingweiModelHelper;
import com.taobao.jingwei.webconsole.util.DataUtil;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class JingweiUpdateTask {
    @Autowired
    private JingweiTaskAO jwTaskAO;

    public void execute(Context context, @Param(name = "taskId") String taskId,
                        @Param(name = "host") String host) {
        if (StringUtil.isNotBlank(taskId)) {
            SyncTaskNode task = jwTaskAO.getTaskInfo(taskId, host);
            // multi grouping 设置
            List<GroupingSetting> groupingSettings = task.getGroupingSettings();
            if (groupingSettings != null) {
                context.put("groupingSettings", groupingSettings);
            }
            if (task.getExtractorType() == ExtractorType.META_EXTRACTOR) {
                MetaExtractorNode metaExtractorNode = new MetaExtractorNode();
                try {
                    metaExtractorNode.jsonStringToNodeSelf(task
                            .getExtractorData());
                } catch (JSONException e) {
                    // 忽略异常
                }
                context.put("metaExtractorNode", metaExtractorNode);
            } else if (task.getExtractorType() == ExtractorType.BINLOG_EXTRACTOR) {
                try {
                    BinLogExtractorNode binlog = new BinLogExtractorNode();
                    binlog.jsonStringToNodeSelf(task.getExtractorData());

                    context.put("binlogProperties", binlog.getConf());
                    context.put("binlogData", binlog);
                    StringBuilder sb = new StringBuilder();
                    Set<Object> set = binlog.getConf().keySet();
                    List<String> list = new ArrayList<String>();
                    for (Object key : set) {
                        list.add(key.toString());
                    }
                    Collections.sort(list);
                    for (String key : list) {
                        sb.append(key + "=" + binlog.getConf().getProperty(key));
                        sb.append(System.getProperty("line.separator"));
                    }
                    if (binlog.isAutoSwitch()) {
                        context.put("newBinData", sb.toString());
                        context.put("binData",
                                JingweiBinlogExtractor.getTemplate());
                    } else {
                        context.put("newBinData",
                                JingweiBinlogExtractor.getNewTemplate());
                        context.put("binData", sb.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // TODO: 怎么处理这个异常？忽略掉先
                }
            } else if (task.getExtractorType() == ExtractorType.ORACLE_EXTRACTOR) {
                OracleExtractorNode oracle = new OracleExtractorNode();
                try {
                    oracle.jsonStringToNodeSelf(task.getExtractorData());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                context.put("propBinData",
                        DataUtil.propertesToString(oracle.getConf()));
            } else if (task.getExtractorType() == ExtractorType.DRC_EXTRACTOR) {
                DrcExtractorNode drc = new DrcExtractorNode();
                try {
                    drc.jsonStringToNodeSelf(task.getExtractorData());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                context.put("propBinData",
                        DataUtil.propertesToString(drc.getConf()));
            }
            JingweiApplierMeta metaApplierNode = new JingweiApplierMeta();
            if (ApplierType.DATABASE_APPLIER == task.getApplierType()) {
                DataBaseApplierNode dban = new DataBaseApplierNode(
                        task.getApplierData());
                context.put("dban", dban);
                if (dban.getSchemaNames() != null
                        && !dban.getSchemaNames().isEmpty()) {
                    context.put("schemas", JingweiTypeHelper.listToString(dban
                            .getSchemaNames()));
                }
                context.put("sApplierType", task.getApplierType().getType());
            } else if (task.getApplierType() == ApplierType.META_APPLIER) {
                MetaApplierNode meta = new MetaApplierNode(
                        task.getApplierData());
                metaApplierNode.setAppEnableColumnFilter(meta
                        .isEnableColumnfilter());
                metaApplierNode.setAppMaxEventSize(meta.getMaxEventSize());
                metaApplierNode.setAppMetaTopic(meta.getMetaTopic());
                metaApplierNode.setAppSendTimeOut(meta.getSendTimeOut());
                metaApplierNode.setAppShardColumn(meta.getShardColumn());
                metaApplierNode.setAppSplitTxEvent(meta.isSplitTxEvent());
                metaApplierNode
                        .setAppCompressionType(meta.getCompressionType());
                EventFilterNode filter = new EventFilterNode(
                        meta.getEventFilterData());
                metaApplierNode.setAppInsert(filter.getIncludeInsert());
                metaApplierNode.setAppUpdate(filter.getIncludeUpdate());
                metaApplierNode.setAppDelete(filter.getIncludeDelete());
                if (metaApplierNode.isAppEnableColumnFilter()) {
                    metaApplierNode.setAppColumnFilterString(JingweiModelHelper
                            .parseColumnFilter(meta.getEventFilterData()));
                }
            } else if (task.getApplierType() == ApplierType.MULTI_META_APPLIER) {
                MultiMetaApplierNode mman = new MultiMetaApplierNode(
                        task.getApplierData());
                List<SubMetaApplierNode> subs = mman.getSubMetaApplierNodes();
                List<JingweiApplierMultiMeta> multiMetaApplier = new ArrayList<JingweiApplierMultiMeta>(
                        subs.size());
                for (SubMetaApplierNode sub : subs) {
                    JingweiApplierMultiMeta meta = new JingweiApplierMultiMeta();
                    meta.setMultiEnableColumnFilter(sub.isEnableColumnfilter()
                            + "");
                    meta.setMultiMaxEventSize(sub.getMaxEventSize());
                    meta.setMultiMetaTopic(sub.getMetaTopic());
                    meta.setMultiSendTimeOut(sub.getSendTimeOut());
                    meta.setMultiShardColumn(sub.getShardColumn());
                    meta.setMultiSplitTxEvent(sub.isSplitTxEvent() + "");
                    meta.setMultiSrcSchemaReg(sub.getSrcSchemaReg());
                    meta.setMultiSrcTableReg(sub.getSrcTableReg());
                    meta.setMultiCompressionType(sub.getCompressionType());
                    EventFilterNode filter = new EventFilterNode(
                            sub.getEventFilterData());
                    meta.setMultiInsert(filter.getIncludeInsert());
                    meta.setMultiUpdate(filter.getIncludeUpdate());
                    meta.setMultiDelete(filter.getIncludeDelete());
                    if (sub.isEnableColumnfilter()
                            && StringUtil.isNotBlank(sub.getEventFilterData())) {
                        if (StringUtil.isNotBlank(filter.getSourceCode())) {
                            meta.setMultiColumnFilterAdvEnabled(true);
                            meta.setMultiColumnFilterAdv(filter.getSourceCode());
                        } else {
                            meta.setMultiColumnFilterString(JingweiModelHelper
                                    .parseColumnFilter(sub.getEventFilterData()));
                        }
                    }

                    multiMetaApplier.add(meta);
                }
                context.put("multiMetaApplier", multiMetaApplier);
            } else if (task.getApplierType() == ApplierType.ANDOR_COMMAND_APPLIER) {
                AndorCommandApplierNode andorCommandApplierNode = new AndorCommandApplierNode(
                        task.getApplierData());
                context.put("andorCommandApplierNode", andorCommandApplierNode);

            }

            if (StringUtil.isNotBlank(task.getApplierFilterData())) {
                ApplierFilterNode filter = new ApplierFilterNode(
                        task.getApplierFilterData());
                boolean hasColumnFilter = filter.getEventFilterNode() != null
                        && filter.getEventFilterNode().getConditions() != null
                        && !filter.getEventFilterNode().getConditions()
                        .isEmpty();
                if (hasColumnFilter) {
                    try {
                        Iterator<Entry<String, HashMap<String, ColumnFilterConditionNode>>> iter = filter
                                .getEventFilterNode().getConditions()
                                .entrySet().iterator();
                        ColumnFilterConditionNode cFilter = null;
                        if (iter.hasNext()) {
                            Iterator<Entry<String, ColumnFilterConditionNode>> iter2 = iter
                                    .next().getValue().entrySet().iterator();
                            cFilter = iter2.next().getValue();
                        } else {
                            cFilter = new ColumnFilterConditionNode();
                        }
                        context.put("appGlobalColumnEnalbed", !cFilter
                                .getExcludeColumns().isEmpty()
                                && !cFilter.getIncludeColumns().isEmpty());
                        context.put("appGlobalColumnIsExclude", !cFilter
                                .getExcludeColumns().isEmpty());
                        context.put("appGlobalColumnFilters",
                                JingweiModelHelper.parseColumnFilter(filter
                                        .getEventFilterNode().toJSONString()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                context.put("isAppGlobalFilterAdvMode", !StringUtil
                        .isBlank(filter.getEventFilterNode().getSourceCode()));
                context.put("hasColumnFilter", hasColumnFilter);
                context.put("appGlobalFilter", filter);
            } else {
                context.put("appGlobalColumnEnalbed", "none");
            }

            context.put("metaApplierNode", metaApplierNode);

            context.put("taskNode", task);
            context.put("sExtractorType",
                    task.getExtractorType() != null ? task.getExtractorType()
                            .getType() : 1);
            context.put("sApplierType", task.getApplierType() != null ? task
                    .getApplierType().getType() : 1);
        }
        PositionNode position = jwTaskAO.getLastCommit(taskId, host);
        List<PositionNode> posList = jwTaskAO.getLastCommits(taskId, host);
        if (!posList.isEmpty()) {
            // FIXME:如果需要插入空值需在此处将position的timestamp设置为空
            posList.add(0, position);
        }
        boolean running = jwTaskAO.hasRunningHost(taskId, host);

        context.put("switchPolicyType",
                JingweiTypeHelper.getDBSyncSwitchPolicyType());
        context.put("dateFormator", new SimpleDateFormat(
                PositionNode.DEFAULT_DATE_FORMAT));
        context.put("running", running);
        context.put("position", position);
        context.put("posList", posList);
        context.put("dbType", JingweiTypeHelper.getDbType());
        context.put("warnMessage", JingweiWebConsoleConstance.NOT_JINGWEI_TASK);
        context.put("taskId", taskId);
        context.put("extractorType", JingweiTypeHelper.getExtractorType());
        context.put("applierType", JingweiTypeHelper.getApplierType());
        context.put("hosts", JingweiZkConfigManager.getKeys());
        context.put("host", host);
        context.put("compressionType", JingweiTypeHelper.getCompressionType());
    }
}
