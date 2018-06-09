package com.taobao.jingwei.core.kernel;

import com.alibaba.common.lang.StringUtil;
import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.common.JingWeiUtil;
import com.taobao.jingwei.common.config.ChildChangeListener;
import com.taobao.jingwei.common.config.ConfigManager;
import com.taobao.jingwei.common.config.SessionStateListener;
import com.taobao.jingwei.common.config.impl.zk.ZkConfigManager;
import com.taobao.jingwei.common.node.AlarmNode;
import com.taobao.jingwei.common.node.StatusNode;
import com.taobao.jingwei.common.node.StatusNode.StatusEnum;
import com.taobao.jingwei.common.node.applier.ApplierFilterNode;
import com.taobao.jingwei.common.node.applier.DataBaseApplierNode;
import com.taobao.jingwei.common.node.applier.MetaApplierNode;
import com.taobao.jingwei.common.node.applier.MultiMetaApplierNode;
import com.taobao.jingwei.common.node.extractor.BinLogExtractorNode;
import com.taobao.jingwei.common.node.extractor.DrcExtractorNode;
import com.taobao.jingwei.common.node.extractor.MetaExtractorNode;
import com.taobao.jingwei.common.node.extractor.OracleExtractorNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode;
import com.taobao.jingwei.common.node.tasks.SyncTaskNode.GroupingSetting;
import com.taobao.jingwei.common.node.type.ApplierType;
import com.taobao.jingwei.common.node.type.ExtractorType;
import com.taobao.jingwei.common.stats.StatsUnit;
import com.taobao.jingwei.core.internal.applier.*;
import com.taobao.jingwei.core.internal.extractor.BinLogExtractor;
import com.taobao.jingwei.core.internal.extractor.DrcExtractor;
import com.taobao.jingwei.core.internal.extractor.MetaExtractor;
import com.taobao.jingwei.core.internal.extractor.OracleExtractor;
import com.taobao.tddl.dbsync.Dbsync;
import com.taobao.tddl.dbsync.DbsyncContext;
import com.taobao.tddl.dbsync.DbsyncException;
import com.taobao.tddl.dbsync.applier.Applier;
import com.taobao.tddl.dbsync.extractor.Extractor;
import com.taobao.tddl.dbsync.filter.Filter;
import com.taobao.tddl.dbsync.filter.LoggingFilter;
import com.taobao.tddl.dbsync.pipeline.Pipeline;
import com.taobao.tddl.dbsync.pipeline.grouping.GroupingPipeline;
import com.taobao.tddl.dbsync.pipeline.impl.DefaultPipeline;
import com.taobao.tddl.venus.replicator.conf.ReplicatorConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.json.JSONException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * description: ����ľ������Ĺ����࣬�����Լ����ñ�Ҫ����init������Ҳ���� ͨ�����õ�loader����
 *
 * @author <a href="mailto:qihao@taobao.com">qihao</a>
 * @version 1.0
 * @{# AbstractJingWeiCore.java Create on Dec 19, 2011 7:29:41 PM
 * <p/>
 * Copyright (c) 2011 by qihao
 */
public abstract class AbstractJingWeiCore implements JingWeiConstants {
	protected static final Log logger = LogFactory.getLog(JingWeiCore.class);
	protected static final String HOST_NAME = JingWeiUtil.getLocalHostName();
	private final ReentrantLock startLock = new ReentrantLock();
	private volatile ExecutorService executor;
	private final TaskLockListener taskLockListener = new TaskLockListener();
	private final StatusNode taskStatus = new StatusNode();
	private volatile Boolean start = Boolean.FALSE;
	private final DBSwitchManager switchManager = new DBSwitchManager(this);

	/**
	 * ��ǰ�Ŷ�����·����ֻ��ע���Ŷ����ɹ���Ż���ֵ �����ڼ�������������ϣ���Ҫ�ó�����ʱ����õ� ��·������ɾ��ע������Ϣ��ɾ����ñ���ΪEMPTY
	 */
	private volatile String currentTaskLockPath;
	/**
	 * �����HOST���ڵ�/jingwei/tasks/**task/hosts/xxxhost
	 */
	private volatile String taskHostRootPath;
	/**
	 * �Ƿ�ʹ��ͳ�ƹ���Ĭ��Ϊtrue
	 */
	private boolean statProxyApplier = true;
	/**
	 * dbsyncʵ��
	 */
	private Dbsync dbsync;
	/**
	 * �����������ýڵ�
	 */
	protected volatile SyncTaskNode syncTaskNode;
	/**
	 * �������ɲ���
	 */
	protected volatile Extractor unWrapExtractor;
	/**
	 * �������Ѳ���
	 */
	private volatile Applier unWrapApplier;
	/**
	 * ���ݹ�����
	 */
	private Filter[] filters;
	/**
	 * dbsync�ĺ��Ĺ���
	 */
	private final JingWeiHook jingWeiHook = new JingWeiHook(this);
	private volatile String taskName;
	/**
	 * ���ù�������Ĭ��Ϊzk��ʵ��
	 */
	protected volatile ConfigManager configManager;
	/**
	 * ����ͳ�Ƶ�Ԫ
	 */
	protected volatile StatsUnit statsUnit;

	/**
	 *
	 */
	private String serverName;

	/**
	 * ����core��ʼ��֮��Ĵ�����Ҫ������ʵ�� ����������init֮������ر�Ĵ���
	 */
	public abstract void afterInitProcessor();

	/**
	 * ����core��ʼ��֮ǰ�Ĵ�����Ҫ������ʵ�� ����������init֮ǰ�����ر�Ĵ���
	 */
	public abstract void beforeInitProcessor();

	public void init(String taskName) throws Exception {
		this.taskName = taskName;
		this.init();
	}

	public void init() throws Exception {
		if (this.start) {
			throw new RuntimeException("jingweiCore already Start taskName: "
					+ StringUtil.defaultIfBlank(this.taskStatus.getName()));
		}
		// DCL���
		if (null == configManager) {
			startLock.lock();
			try {
				if (null == configManager) {
					this.configManager = new ZkConfigManager();
					this.configManager.init();
				}
			} finally {
				startLock.unlock();
			}
		}
		// DCL���
		startLock.lock();
		try {
			// ��������ǰ����
			beforeInitProcessor();
			// ��֤ͬһ��JVM��ͬһ��jingweiCore���󲻻ᱻ���init
			if (this.start) {
				String msg = "jingweiCore already Start taskName: "
						+ StringUtil.defaultIfBlank(this.taskStatus.getName());
				logger.error(msg);
				sendToAlarm(msg, null);
				throw new RuntimeException(msg);
			}
			this.executor = Executors.newSingleThreadExecutor();
			executor.execute(new Runnable() {
				@Override
				public void run() {
					// ��ʼ��syncTask���ýڵ�
					initSyncTaskNode();
					// taskHostRootPathΪ/jingwei-v2/tasks/��������/hosts/��������
					AbstractJingWeiCore.this.taskHostRootPath = SyncTaskNode.getHostTaskDataIdByName(
							AbstractJingWeiCore.this.syncTaskNode.getName(),
							StringUtil.isNotBlank(AbstractJingWeiCore.this.getServerName()) ? AbstractJingWeiCore.this
									.getServerName() : HOST_NAME);
					AbstractJingWeiCore.this.taskStatus.setName(AbstractJingWeiCore.this.syncTaskNode.getName());
					AbstractJingWeiCore.this.taskStatus.setOwnerDataIdOrPath(AbstractJingWeiCore.this.taskHostRootPath);
					// ���ͬ�����Ƿ���������ͬ������
					String statusStrData = AbstractJingWeiCore.this.configManager
							.getData(AbstractJingWeiCore.this.taskStatus.getDataIdOrNodePath());
					if (StringUtil.isNotBlank(statusStrData)) {
						// �����ȡ�������ݣ�˵����ǰ���������ͬ��������������
						String msg = "Has Same Task Running At HostName: " + HOST_NAME + " taskName: "
								+ AbstractJingWeiCore.this.syncTaskNode.getName();
						logger.error(msg);
						sendToAlarm(msg, null);
						AbstractJingWeiCore.this.changeTaskStatus(StatusEnum.STANDBY);
					}
					// ��ʼ��statUnit
					initStatUnit();
					try {
						if (syncTaskNode.isSingleTask()) {
							// ��ʼ��locks�ڵ�
							AbstractJingWeiCore.this.configManager.publishOrUpdateData(
									AbstractJingWeiCore.this.syncTaskNode.getDataIdOrNodePath() + ZK_PATH_SEP + "locks",
									StringUtil.EMPTY_STRING, AbstractJingWeiCore.this.syncTaskNode.isPersistent());
							// ����serverע�ᵽlocks��
							registerLocks(Boolean.TRUE);
						} else {
							startDbsync();
						}
						registerSessionStateListener();
						afterInitProcessor();
					} catch (Exception e) {
						logger.error("JingWeiCore init Thread Error", e);
					}
				}
			});
			this.start = Boolean.TRUE;
		} finally {
			startLock.unlock();
		}
	}

	/**
	 * ����DBSYNC,ͬʱdbsync�ڲ���Extractor ��Applier ���ʵ����AbstractPlugin ��destory����ͬʱҲ�ᱻ����
	 *
	 * @throws Exception
	 */
	protected synchronized void destoryDbsync() throws Exception {
		if (null != dbsync) {
			logger.warn("destory dbsync!");
			dbsync.shutdown();
			if (!dbsync.waitForDone(10000L)) {
				logger.error("stop Dbsync Timeout " + 10000L + " ms");
				throw new RuntimeException("stop Dbsync Timeout!");
			}
		}
	}

	/**
	 * ���ٵ�����CORE
	 */
	public void destory() {
		try {
			this.destoryDbsync();
			this.statsUnit.destroy();
			this.start = Boolean.FALSE;
		} catch (Exception e) {
		} finally {
			if (null != this.executor) {
				this.executor.shutdownNow();
			}
		}
	}

	/**
	 * ����ע������������·���һ���µ�seq�Ľڵ�
	 *
	 * @return String �����������ĸ��ڵ�·��
	 * @throws Exception
	 */
	protected String registerLocks(boolean addListener) throws Exception {
		StringBuffer lockPathBuffer = new StringBuffer(this.syncTaskNode.getDataIdOrNodePath());
		lockPathBuffer.append(ZK_PATH_SEP).append(JINGWEI_TASK_LOCKS_NODE_NAME);
		// zk����·��Ϊ:/jingwei-v2/tasks/��������/locks
		String locksParentPath = lockPathBuffer.toString();
		if (addListener) {
			AbstractJingWeiCore.this.configManager.addChildChangesListener(locksParentPath,
					AbstractJingWeiCore.this.taskLockListener);
		}
		// zk����·��Ϊ/jingwei-v2/tasks/��������locks/��������-���к�
		lockPathBuffer.append(ZK_PATH_SEP)
				.append(StringUtil.isNotBlank(this.getServerName()) ? this.getServerName() : HOST_NAME)
				.append(BAR_LINE);
		final String taskLockPath = lockPathBuffer.toString();
		// ����host��Ϣ��taskLocks��
		this.currentTaskLockPath = this.configManager.publishDataSequential(taskLockPath,
				JingWeiUtil.date2String(new Date()), false);
		logger.warn("[Register LockNode] HostName: " + HOST_NAME + " lockPath: " + taskLockPath);
		return locksParentPath;
	}

	/**
	 * ע��SessionState����������ZK��SESSION�仯���� ��Ӧ��Ŀǰ��������ZK SESSION��ʱ������������ط� �ǳ־�����
	 */
	private void registerSessionStateListener() {
		this.configManager.addSessionStateListener(new SessionStateListener() {
			@Override
			public void handleNewSession() throws Exception {
				if (AbstractJingWeiCore.this.syncTaskNode.isSingleTask()) {
					// ����serverע�ᵽlocks��
					registerLocks(Boolean.FALSE);
				}
				// �ط�taskStatus��Ϣ
				changeTaskStatus(AbstractJingWeiCore.this.taskStatus.getStatusEnum());
				logger.warn("RePublish status : "
						+ AbstractJingWeiCore.this.taskStatus.getStatusEnum().getStatusString());
			}

			@Override
			public void handleStateChanged(KeeperState state) throws Exception {
				// NO-OP ��������
			}
		});
	}

	protected void removeCurrentTaskLocks() {
		try {
			this.configManager.delete(this.currentTaskLockPath);
		} catch (Exception e) {
			logger.error("removeCurrentTaskLocks error! lockPath: " + currentTaskLockPath, e);
		}
	}

	/**
	 * �ı�taskStatus
	 *
	 * @param status
	 */
	protected void changeTaskStatus(StatusEnum status) {
		this.changeTaskStatus(status, Boolean.TRUE);
	}

	/**
	 * �ı�taskStatus
	 *
	 * @param status
	 */
	protected void changeTaskStatus(StatusEnum status, boolean doLog) {
		this.taskStatus.setStatusEnum(status);
		try {
			this.configManager.publishOrUpdateData(this.taskStatus.getDataIdOrNodePath(),
					this.taskStatus.toJSONString(), this.taskStatus.isPersistent());
			if (doLog) {
				StringBuilder sb = new StringBuilder("Change TaskStatus Status: ");
				sb.append(status.getStatusString());
				sb.append(" TaskName: ").append(this.syncTaskNode.getName());
				sb.append(" HostName: ").append(
						StringUtil.isNotBlank(this.getServerName()) ? this.getServerName() : HOST_NAME);
				logger.warn(sb.toString());
			}
		} catch (Exception e) {
			logger.error("Change TaskStatus Status: " + status.getStatusString() + " Error! ", e);
		}
	}

	/**
	 * �ı�taskStatus
	 */
	protected void resetTaskStatus() {
		try {
			this.configManager.delete(this.taskStatus.getDataIdOrNodePath());
			logger.warn("reset TaskStatus Status: " + this.taskStatus.getDataIdOrNodePath());
		} catch (Exception e) {
			logger.error("reset TaskStatus Status: " + this.taskStatus.getDataIdOrNodePath() + " Error! ", e);
		}
	}

	public void sendToAlarm(String msg, Throwable t) {
		AlarmNode alarmNode = new AlarmNode();
		alarmNode.setOwnerDataIdOrPath(this.taskHostRootPath);
		alarmNode.setName(this.syncTaskNode.getName());
		alarmNode.setMessage(msg);
		alarmNode.setThrowable(t);
		try {
			this.configManager.publishOrUpdateData(alarmNode.getDataIdOrNodePath(), alarmNode.toJSONString(),
					alarmNode.isPersistent());
		} catch (Exception e) {
			logger.error("sand Alarm Error!", e);
		}
	}

	/**
	 * �Ƚ�lockPath���б��ҵ�����С��Path����
	 *
	 * @param chlidLockNames
	 * @return
	 */
	private String findMinLockData(List<String> chlidLockNames) {
		String minLock = StringUtil.EMPTY_STRING;
		if (null == chlidLockNames || chlidLockNames.isEmpty()) {
			return minLock;
		}
		Collections.sort(chlidLockNames, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String o1Str = StringUtil.substringAfterLast(o1, BAR_LINE);
				String o2Str = StringUtil.substringAfterLast(o2, BAR_LINE);
				return Long.valueOf(o1Str).compareTo(Long.valueOf(o2Str));
			}
		});
		return chlidLockNames.get(0);
	}

	/**
	 * ����DBSync
	 *
	 * @throws DbsyncException
	 * @throws InterruptedException
	 */
	protected synchronized void startDbsync() throws DbsyncException, InterruptedException {
		logger.warn("======== launch dbsync start ========");
		DbsyncContext context = new DbsyncContext();
		Applier runTimeApplier = this.unWrapApplier;
		Extractor runTimeExtractor = this.unWrapExtractor;
		// ���û��������EXTRACTOR����extractor��������Ĭ�ϵ�
		if (null == runTimeExtractor) {
			runTimeExtractor = getInternalExtractor(this.syncTaskNode.getExtractorType(),
					this.syncTaskNode.getExtractorData());
		}
		// �����ʼ��������
		if (null == runTimeExtractor) {
			throw new DbsyncException("Extractor is Null!");
		}
		// ���û��������APPLIER����applier��������Ĭ�ϵ�
		if (null == runTimeApplier) {
			runTimeApplier = getInternalApplier(this.syncTaskNode.getApplierType(), this.syncTaskNode.getApplierData());
		}
		// �����ʼ��������
		if (null == runTimeApplier) {
			throw new DbsyncException("Applier is Null!");
		}

		if (StringUtil.isNotBlank(this.syncTaskNode.getApplierFilterData())) {
			ApplierFilterNode applierFilterNode = new ApplierFilterNode(this.syncTaskNode.getApplierFilterData());
			FilterApplier filterApplier = new FilterApplier(runTimeApplier);
			filterApplier.setApplierFilterNode(applierFilterNode);
			runTimeApplier = filterApplier;
		}
		// ����õ������õ�applier������stat��װ
		if (this.isStatProxyApplier()) {
			runTimeApplier = new StatProxyApplier(runTimeApplier, this.statsUnit);
		}
		Pipeline pipeline = null;
		if (this.syncTaskNode.isMultiThread() & !(runTimeExtractor instanceof MetaExtractor)) {
			// ����������߳���ʹ��GroupingPipeline
			GroupingPipeline groupingPipeline = new GroupingPipeline();
			groupingPipeline.setGrouping(this.syncTaskNode.getMaxThreadCount());
			groupingPipeline.setGroupQueueSize(this.syncTaskNode.getQueueCapacity());
			List<GroupingSetting> groupingSettings = this.syncTaskNode.getGroupingSettings();
			if (groupingSettings != null && !groupingSettings.isEmpty()) {
				MultiGroupingPolicy multiGroupingPolicy = new MultiGroupingPolicy();
				multiGroupingPolicy.init(groupingSettings);
				groupingPipeline.setGroupPolicy(multiGroupingPolicy);
			}
			pipeline = groupingPipeline;
		} else {
			pipeline = new DefaultPipeline();
		}
		context.setPipeline(pipeline);
		context.setApplier(runTimeApplier);
		context.setExtractor(runTimeExtractor);
		// ����filter
		List<Filter> filterList = new ArrayList<Filter>();
		if (null != this.getFilters()) {
			filterList.addAll(Arrays.asList(this.getFilters()));
		}
		filterList.add(new LoggingFilter());
		context.setFilters((Filter[]) filterList.toArray(new Filter[0]));
		// ����jingWeiHook ���ʱ�䴰�ڲ���
		this.jingWeiHook.setSummaryTimeInterval(this.syncTaskNode.getSummaryPeriod());
		this.jingWeiHook.setUpdateTimeInterval(this.syncTaskNode.getComitLogPeriod());
		this.jingWeiHook.setUpdateTxInterval(this.syncTaskNode.getComitLogCount());
		context.setMonitors(this.jingWeiHook);
		this.dbsync = Dbsync.getInstance();
		// ����pipeline
		this.dbsync.launch(this.syncTaskNode.getName(), context);
		logger.warn("======== launch dbsync end ========");
	}

	/**
	 * ����ApplierType ʹ�ò�ͬ��ApplierNode���� ������ȡ����Ӧ��Applier
	 *
	 * @param applierType
	 * @param applierData
	 * @throws DbsyncException
	 */
	Applier tmpApplier = null;

	private Applier getInternalApplier(ApplierType applierType, String applierData) throws DbsyncException {
		if (StringUtil.isBlank(applierData)) {
			String msg = "applier Data is Empty, taskName: " + this.syncTaskNode.getName();
			logger.error(msg);
			throw new DbsyncException(msg);
		}
		if (ApplierType.DATABASE_APPLIER == applierType) {
			DataBaseApplierNode dataBaseApplierNode = new DataBaseApplierNode(applierData);
			tmpApplier = new DataBaseApplier(dataBaseApplierNode);
		} else if (ApplierType.META_APPLIER == applierType) {
			MetaApplierNode metaApplierNode = new MetaApplierNode(applierData);
			tmpApplier = new MetaApplier(metaApplierNode);
		} else if (ApplierType.MULTI_META_APPLIER == applierType) {
			MultiMetaApplierNode multiMetaApplierNode = new MultiMetaApplierNode(applierData);
			tmpApplier = new MultiMetaApplier(multiMetaApplierNode);

		} else if (ApplierType.ANDOR_COMMAND_APPLIER == applierType) {
			// TODO ��ANDOR�㶨����˵
			// AndorCommandApplierNode andorCommandApplierNode = new AndorCommandApplierNode(applierData);
			// tmpApplier = new AndorCommandApplier(andorCommandApplierNode);
		}
		if (null == tmpApplier) {
			String msg = "load applier  is Null, taskName: " + this.syncTaskNode.getName();
			logger.error(msg);
			sendToAlarm(msg, null);
			throw new DbsyncException(msg);
		}
		return tmpApplier;
	}

	/**
	 * ����etractorType ʹ�ò�ͬ��ExtractorNode���� ������ȡ����Ӧ��Extractor
	 *
	 * @param etractorType
	 * @param extractorData
	 * @throws DbsyncException
	 */
	private Extractor getInternalExtractor(ExtractorType etractorType, String extractorData) throws DbsyncException {
		Extractor tmpExtractor = null;
		if (StringUtil.isBlank(extractorData)) {
			String msg = "DbsyncExtractor extractor Data is Empty, taskName: " + this.syncTaskNode.getName();
			logger.error(msg);
			throw new DbsyncException(msg);
		}
		try {
			if (ExtractorType.BINLOG_EXTRACTOR == etractorType) {
				BinLogExtractorNode binLogExtractorNode = new BinLogExtractorNode();
				binLogExtractorNode.jsonStringToNodeSelf(extractorData);
				Properties replicatorProp = binLogExtractorNode.getConf();
				replicatorProp.setProperty(ReplicatorConf.SOURCE_ID, this.syncTaskNode.getName());
				BinLogExtractor binlogExtractor = new BinLogExtractor(replicatorProp, this.switchManager);
				if (binLogExtractorNode.isAutoSwitch()) {
					String groupName = binLogExtractorNode.getGroupName();
					// �������ض�Ӧgroup�����ݿ�����
					String[] ipAndPorts = this.switchManager.loadAvailableDataBaseConf(groupName);
					// ��IP�б��ַ������õ�binlogExtractor
					binlogExtractor.setIps(ipAndPorts[0]);
					// ��Port�б��ַ������õ�binlogExtractor
					binlogExtractor.setPorts(ipAndPorts[1]);
					// ����Ĭ���л�����
					binlogExtractor.setSwitchPolicy(StringUtil.defaultIfBlank(binLogExtractorNode.getSwitchPolicy(),
							"NONE"));
				}
				tmpExtractor = binlogExtractor;
			} else if (ExtractorType.META_EXTRACTOR == etractorType) {
				MetaExtractorNode metaExtractorNode = new MetaExtractorNode();
				metaExtractorNode.jsonStringToNodeSelf(extractorData);
				tmpExtractor = new MetaExtractor(metaExtractorNode, this.syncTaskNode.isUseLastPosition());
			} else if (ExtractorType.ORACLE_EXTRACTOR == etractorType) {
				OracleExtractorNode oracleExtractorNode = new OracleExtractorNode(extractorData);
				tmpExtractor = new OracleExtractor(oracleExtractorNode);
			} else if (ExtractorType.DRC_EXTRACTOR == etractorType) {
				DrcExtractorNode drcExtractorNode = new DrcExtractorNode(extractorData);
				tmpExtractor = new DrcExtractor(drcExtractorNode);
			}
		} catch (JSONException e) {
			String msg = "DbsyncExtractor load extractor Data Error!, taskName: " + this.syncTaskNode.getName();
			logger.error(msg);
			sendToAlarm(msg, e);
			throw new DbsyncException(msg, e);
		}
		return tmpExtractor;
	}

	public void setExtractor(Extractor extractor) {
		this.unWrapExtractor = extractor;
	}

	public void setApplier(Applier applier) {
		this.unWrapApplier = applier;
	}

	public Filter[] getFilters() {
		return filters;
	}

	public void setFilters(Filter... filters) {
		this.filters = filters;
	}

	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}

	public void setSyncTaskNode(SyncTaskNode syncTaskNode) {
		this.syncTaskNode = syncTaskNode;
	}

	public void setStatProxyApplier(boolean statProxyApplier) {
		this.statProxyApplier = statProxyApplier;
	}

	public boolean isStatProxyApplier() {
		return statProxyApplier;
	}

	public String getTaskHostRootPath() {
		return taskHostRootPath;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = StringUtil.trim(taskName);
	}

	private void initStatUnit() {
		this.statsUnit = new StatsUnit(this.syncTaskNode.getName(), this.configManager);
		this.statsUnit.setStatsPeriod(this.syncTaskNode.getStatsPeriod());
		this.statsUnit.setHostName(StringUtil.isNotBlank(this.getServerName()) ? this.getServerName() : HOST_NAME);
		this.statsUnit.init();
	}

	private void initSyncTaskNode() {
		// ���û������syncTaskNode,����������taskName,���Ը������Ƶ�zk�л�ȡtask����
		if (null == this.syncTaskNode && StringUtil.isBlank(this.taskName)) {
			logger.error("taskConfig Empty Please set syncTaskNode Or config ZK taskNode");
			throw new RuntimeException("taskConfig Empty Please set syncTaskNode Or config ZK taskNode");
		}
		if (null != this.syncTaskNode) {
			this.taskName = this.syncTaskNode.getName();
		} else {
			if (StringUtil.isNotBlank(this.taskName)) {
				this.syncTaskNode = new SyncTaskNode();
				this.syncTaskNode.setName(this.taskName);
				String syncTaskData = this.configManager.getData(this.syncTaskNode.getDataIdOrNodePath());
				if (StringUtil.isNotBlank(syncTaskData)) {
					try {
						this.syncTaskNode.jsonStringToNodeSelf(syncTaskData);
					} catch (Exception e) {
						logger.error("load taskNode config Error", e);
						throw new RuntimeException("load taskNode config Error", e);
					}
				}
			}
		}
	}

	class TaskLockListener extends ChildChangeListener {
		@Override
		public void handleChild(String parentPath, List<String> currentChilds) {
			// �ҵ������С��
			String miniHostLok = findMinLockData(currentChilds);
			// ���������ƻ�ȡ������
			String lockHostName = StringUtil.substringBeforeLast(miniHostLok, BAR_LINE);
			if (StringUtil.equals(
					lockHostName,
					StringUtil.isNotBlank(AbstractJingWeiCore.this.getServerName()) ? AbstractJingWeiCore.this
							.getServerName() : HOST_NAME)) {
				// �����ǰ���ѡƱ�����Լ�
				try {
					if (StatusNode.StatusEnum.RUNNING != AbstractJingWeiCore.this.taskStatus.getStatusEnum()) {
						// ��ǰdbsyncû��������������
						startDbsync();
					} else {
						AbstractJingWeiCore.this.changeTaskStatus(StatusNode.StatusEnum.RUNNING);
					}
				} catch (Exception e) {
					/**
					 * ��������ʧ�ܣ�Ĭ�������ٵ�dbsync���� ����һ��ʱ�������ע����������ѯ������ ��¼��־
					 */
					String msg = "[New Master Running Fail] HostName: " + HOST_NAME + " TaskName: "
							+ AbstractJingWeiCore.this.syncTaskNode.getName();
					logger.error(msg, e);
					sendToAlarm(msg, e);
					/* ��ȡ��Ҫɾ���ǳ־����ݵ�path����·��Ϊ
					* jingwei-v2/tasks/��������locks/��������-���к�
					*/
					String chlidLockFullPath = parentPath + ZK_PATH_SEP + miniHostLok;
					// �������٣���������ע������status
					destoryAndRetryRegister(chlidLockFullPath);
				}
			} else {
				// �����ǰ��ȡ��ѡƱ�����Լ�
				if (StatusNode.StatusEnum.RUNNING == AbstractJingWeiCore.this.taskStatus.getStatusEnum()) {
					// ���֮ǰ�Լ�����������Running�Ļ������Լ����ٵ������ó�STADNBY
					destoryAndRetryRegister(AbstractJingWeiCore.this.currentTaskLockPath);
					logger.warn("[Old Master StandBy] HostName: " + HOST_NAME + " TaskName: "
							+ AbstractJingWeiCore.this.syncTaskNode.getName());
				} else {
					if (StatusNode.StatusEnum.STANDBY != AbstractJingWeiCore.this.taskStatus.getStatusEnum()) {
						// ���ѡƱ�Ĳ����Լ��������Լ���ǰҲû�����У������Լ�״̬ΪSTANDBY
						changeTaskStatus(StatusEnum.STANDBY);
					}
				}
			}

		}

		private void destoryAndRetryRegister(String chlidLockFullPath) {
			try {
				// �������ʧ��,��������
				destoryDbsync();
			} catch (Exception e1) {
				// FIXME ���Dbsync����ʧ�ܣ��ô�û����
				String msg = "destory DbSync Error";
				logger.error(msg, e1);
				sendToAlarm(msg, e1);
			} finally {
				try {
					AbstractJingWeiCore.this.configManager.delete(chlidLockFullPath);
					Thread.sleep(10000);
					// ����ע����
					AbstractJingWeiCore.this.registerLocks(Boolean.FALSE);
					changeTaskStatus(StatusEnum.STANDBY);
				} catch (Exception e2) {
					/**
					 * ���������zkɾ��ʧ�ܣ��ܴ��������������ʱ������ZK�ҵ� ������Ƿǳ־����ݣ�������������·ǳ־����ݻ������ʧ���� ����
					 */
					String msg = "unRegister TaskLock Error lockPath: " + chlidLockFullPath;
					logger.error(msg, e2);
					sendToAlarm(msg, e2);
				}
			}
		}
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public boolean isStart() {
		return start;
	}

	public StatusNode getTaskStatus() {
		return taskStatus;
	}
}