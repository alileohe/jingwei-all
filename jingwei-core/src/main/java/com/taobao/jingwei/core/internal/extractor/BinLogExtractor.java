package com.taobao.jingwei.core.internal.extractor;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.core.kernel.DBSwitchManager;
import com.taobao.tddl.dbsync.extractor.Extractor;
import com.taobao.tddl.dbsync.extractor.ExtractorException;
import com.taobao.tddl.dbsync.extractor.Transferer;
import com.taobao.tddl.dbsync.plugin.AbstractPlugin;
import com.taobao.tddl.dbsync.plugin.PluginContext;
import com.taobao.tddl.dbsync.plugin.PluginException;
import com.taobao.tddl.venus.replicator.conf.ReplicatorConf;
import com.taobao.tddl.venus.replicator.dbsync.DbsyncExtractor;

import java.util.Properties;

/** ��binlogExtractor�ļ򵥰�װ�������Ժ��滻�ڲ���DbsyncExtractor
 * 
 * <p>description:<p> 
 *
 * @{#} BinLogExtractor.java Create on Dec 14, 2011 11:16:54 AM   
 *   
 * Copyright (c) 2011 by qihao.
 *
 *@author <a href="mailto:qihao@taobao.com">qihao</a> 
 *@version 1.0   
 */
public class BinLogExtractor extends AbstractPlugin implements Extractor {

	private Properties replicatorProp;

	private DbsyncExtractor dbsyncExtractor;

	private DBSwitchManager switchManager;

	public BinLogExtractor(Properties replicatorProp, DBSwitchManager switchManager) {
		this.replicatorProp = replicatorProp;
		this.switchManager = switchManager;
	}

	@Override
	public void extract(Transferer transferer) throws ExtractorException, InterruptedException {
		this.dbsyncExtractor.extract(transferer);
	}

	public void init(String name, PluginContext context) throws PluginException, InterruptedException {
		this.dbsyncExtractor = new DbsyncExtractor();
		String replicatorVersion = StringUtil.defaultIfBlank(
				this.replicatorProp.getProperty(ReplicatorConf.REPLICATOR_VERSION), "2.1.1");
		boolean initListener = true;

		if (StringUtil.equals(replicatorVersion, "2.1.1")) {
			//ɾ�����ϰ汾�Ѿ�ȥ����key
			replicatorProp.remove("replicator.plugin.directRelay.useThreadPool");
			replicatorProp.remove("replicator.plugin.directRelay.maximumPoolSize");
			replicatorProp.remove("replicator.plugin.directRelay.corePoolSize");
			replicatorProp.remove("replicator.extractor.mysql.host");
			replicatorProp.remove("replicator.extractor.mysql.port");
			replicatorProp.remove("replicator.extractor.mysql.user");
			replicatorProp.remove("replicator.extractor.mysql.password");

			//ת���ϰ汾��keyΪ�°汾
			String parseStatementKey = "replicator.plugin.directRelay.parseStatements";
			String parseStatement = replicatorProp.getProperty(parseStatementKey);
			replicatorProp.remove(parseStatementKey);
			if (null != parseStatement) {
				replicatorProp.put("replicator.extractor.mysql.parseStatements", parseStatement);
			}
			String charsetKey = "replicator.plugin.directRelay.charset";
			String charset = replicatorProp.getProperty(charsetKey);
			replicatorProp.remove(charsetKey);
			replicatorProp.put("replicator.extractor.mysql.charset", StringUtil.defaultIfBlank(charset, "GBK"));
			//���Զ��л�ģʽ��������ע��listener
			initListener = false;
		}
        //�滻��DisruptorStage
        //replicatorProp.put("replicator.stage.mysql-to-dbsync","com.taobao.tddl.venus.replicator.pipeline.DisruptorStageTask");
		this.dbsyncExtractor.setProperties(this.replicatorProp);
        StringBuffer sb=new StringBuffer();
        for(Object key: this.replicatorProp.keySet()){
            String skey=(String)key;
            String svalue=StringUtil.defaultIfEmpty(this.replicatorProp.getProperty(skey));
            sb.append(skey).append("=").append(svalue).append(JingWeiConstants.LINE_SEP);
        }
        logger.warn("Extractor Data: =====" +sb.toString());
		this.dbsyncExtractor.init(name, context);
		if (initListener) {
			this.switchManager.listenerDataBaseConf();
		}
	}

	public void destory() throws PluginException, InterruptedException {
		this.dbsyncExtractor.destory();
	}

	public void setIps(String ips) {
		this.replicatorProp.put(JingWeiConstants.DB_SYNC_IP_KEY, ips);
	}

	public void setPorts(String ports) {
		this.replicatorProp.put(JingWeiConstants.DB_SYNC_PORT_KEY, ports);
	}

	public void setSwitchPolicy(String switchPolicy) {
		this.replicatorProp.put(JingWeiConstants.SWITCH_POLICY_KEY, switchPolicy);
	}
}