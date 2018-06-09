package com.taobao.jingwei.server.plugin;

import com.taobao.jingwei.common.JingWeiConstants;
import com.taobao.jingwei.server.core.ServerCoreThread;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @desc ɨ��plugin��֪ͨ
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">shuohailhl</a>
 * 
 * @date 2011-12-7����5:53:44
 */
public class PluginScanner implements Runnable {

	private static Log log = LogFactory.getLog(PluginScanner.class);

	/** key��ӦtargetĿ¼�� value����޸�ʱ�� */
	private final Map<File, Long> targetFiles = new ConcurrentHashMap<File, Long>();

	/** key ��task name, value �� lib��conf���޸�ʱ�� ,���Ǵ�work��JINGWEI.MF�ļ��л�ȡ����Ϣ */
	private final Map<File, LibConfLastModified> workDirs = new ConcurrentHashMap<File, LibConfLastModified>();

	static class LibConfLastModified {
		private long libLastModified = 0L;
		private long confLastModified = 0L;

		public long getLibLastModified() {
			return libLastModified;
		}

		public void setLibLastModified(long libLastModified) {
			this.libLastModified = libLastModified;
		}

		public long getConfLastModified() {
			return confLastModified;
		}

		public void setConfLastModified(long confLastModified) {
			this.confLastModified = confLastModified;
		}
	}

	/** ɨ��plugin������ */
	private final PluginScannerHelper pluginScannerHelper;

	/** ��plugin��target��workĿ¼����Ȥ��֪ͨ���� */
	private IPluginNotifier notifier;

	public PluginScanner(ServerCoreThread serverCoreThread) {
		this.pluginScannerHelper = new PluginScannerHelper(serverCoreThread);
	}

	@Override
	public void run() {
		this.scan();
	}

	/**
	 * ��ʼ��plugin��target��workĿ¼
	 */
	public void init() {
		File[] targets = this.pluginScannerHelper.getPluginTargets();
		File[] works = this.pluginScannerHelper.getPluginWorkTaskDirs();

		for (File file : targets) {
			this.targetFiles.put(file, file.lastModified());
		}

		for (File file : works) {
			this.putNewLibConfLastModified(file);
		}

		log.warn("[jingwei server] initial pluginscanner, load targets : " + Arrays.toString(targets)
				+ "; load works : " + Arrays.toString(works));

	}

	private void putNewLibConfLastModified(File file) {
		File lib = this.getLib(file);
		File conf = this.getConf(file);

		LibConfLastModified libConfLastModified = new LibConfLastModified();

		try {
			libConfLastModified.setLibLastModified(lib.lastModified());
		} catch (Exception e) {
			log.error("[jingwei server] work dir exist bur lib delete", e);
		}

		try {
			libConfLastModified.setConfLastModified(conf.lastModified());
		} catch (Exception e) {
			log.error("[jingwei server] work dir exist bur lib delete", e);
		}

		this.workDirs.put(file, libConfLastModified);
	}

	private File getLib(File work) {
		StringBuilder sb = new StringBuilder(work.getAbsolutePath());
		sb.append(JingWeiConstants.FILE_SEP).append(PluginScannerHelper.JINGWEI_PLUGIN_LIB_PATH);

		File lib = new File(sb.toString());
		return lib;
	}

	private File getConf(File work) {
		StringBuilder sbConf = new StringBuilder(work.getAbsolutePath());
		sbConf.append(JingWeiConstants.FILE_SEP).append(PluginScannerHelper.JINGWEI_PLUGIN_CONF_PATH);

		File conf = new File(sbConf.toString());
		return conf;
	}

	/**
	 * scan target and work dir in plugin dir.
	 */
	public void scan() {
		this.scanTarget();
		this.scanWork();
	}

	/**
	 * ɨ��workĿ¼
	 */
	private void scanWork() {
		// ��ǰworkĿ¼������
		List<File> workers = Arrays.asList(this.pluginScannerHelper.getPluginWorkTaskDirs());

		// ���ӵ�worker
		List<File> addedWorks = new ArrayList<File>();
		List<File> updateWorks = new ArrayList<File>();

		for (File f : workers) {
			if (!this.workDirs.containsKey(f)) {
				addedWorks.add(f);
				log.warn("[jingwei-server] scan new plugin work has been added : " + f.getAbsolutePath());
			} else {

				long libLastModified = 0L;
				long confLastModified = 0L;

				try {
					libLastModified = this.getLib(f).lastModified();
					confLastModified = this.getConf(f).lastModified();
				} catch (Exception e) {
					log.error("[jingwei server] work dir exist bur lib/conf delete", e);
				}

				if ((libLastModified != 0 && libLastModified != this.workDirs.get(f).getLibLastModified())
						|| (confLastModified != 0 && confLastModified != this.workDirs.get(f).getConfLastModified())) {
					updateWorks.add(f);
					log.warn("[jingwei-server] scan plugin work has been updated : " + f.getAbsolutePath());
				}
			}
		}

		if (!addedWorks.isEmpty()) {

			this.getNotifier().onAddedWorkTask(addedWorks);

			for (File f : addedWorks) {
				this.putNewLibConfLastModified(f);
			}
		}

		if (!updateWorks.isEmpty()) {

			this.getNotifier().onUpdateWorkTask(updateWorks);

			for (File f : updateWorks) {
				this.putNewLibConfLastModified(f);
			}
		}

		// ���ٵ�worker
		List<File> deleteWorks = new ArrayList<File>();
		for (File workDirName : this.workDirs.keySet()) {
			if (!workers.contains(workDirName)) {
				deleteWorks.add(workDirName);
			}
		}

		if (!deleteWorks.isEmpty()) {
			this.getNotifier().onDeleteWorkTask(deleteWorks);
			for (File f : deleteWorks) {
				this.workDirs.remove(f);
				log.warn("[jingwei-server] scan new plugin work has been deleted : " + f.getAbsolutePath());
			}
		}
	}

	/**
	 * ɨ��targetĿ¼
	 */
	private void scanTarget() {
		// ��ǰtargetĿ¼������
		List<File> targets = Arrays.asList(this.pluginScannerHelper.getPluginTargets());

		// ���ӵ�target
		List<File> addedTargets = new ArrayList<File>();
		List<File> updateTargets = new ArrayList<File>();

		for (File f : targets) {
			if (!this.targetFiles.containsKey(f)) {
				addedTargets.add(f);
				log.warn("[jingwei-server] scan new plugin target has been added : " + f.getAbsolutePath());
			} else {
				if (f.lastModified() > this.targetFiles.get(f)) {
					updateTargets.add(f);
					log.warn("[jingwei-server] scan plugin target has been updated : " + f.getAbsolutePath());
				}
			}
		}

		if (!addedTargets.isEmpty()) {

			this.getNotifier().onAddedTarget(addedTargets);

			for (File f : addedTargets) {
				this.targetFiles.put(f, f.lastModified());
			}
		}

		if (!updateTargets.isEmpty()) {

			this.getNotifier().onUpdateTarget(updateTargets);

			for (File f : updateTargets) {
				this.targetFiles.put(f, f.lastModified());
			}
		}

		// ���ٵ�target
		List<File> deleteTargets = new ArrayList<File>();
		for (File target : this.targetFiles.keySet()) {
			if (!targets.contains(target)) {
				deleteTargets.add(target);
				log.warn("[jingwei-server] scan new target has been deleted : " + target.getAbsolutePath());
			}
		}

		if (!deleteTargets.isEmpty()) {
			this.getNotifier().onDeleteTarget(deleteTargets);
			for (File f : deleteTargets) {
				this.targetFiles.remove(f);
			}
		}
	}

	public IPluginNotifier getNotifier() {
		return notifier;
	}

	public void setNotifier(IPluginNotifier notifier) {
		this.notifier = notifier;
	}

	public PluginScannerHelper getPluginScannerHelper() {
		return pluginScannerHelper;
	}

}
