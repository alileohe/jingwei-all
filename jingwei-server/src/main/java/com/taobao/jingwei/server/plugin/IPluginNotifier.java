package com.taobao.jingwei.server.plugin;

import java.io.File;
import java.util.List;

/**
 * @desc ɨ�赽plugin��workĿ¼�仯��֪ͨ��ע��
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">˷�� shuohailhl</a>
 * 
 * @date 2011-12-7����5:24:04
 */
public interface IPluginNotifier {
	/**
	 * targetĿ¼�µ��ļ�����Ӧ�����񣩸���
	 * 
	 * @param updateTargetFileNames �����ļ����б�
	 */
	void onUpdateTarget(List<File> updateTargetFileNames);

	/**
	 * workĿ¼�µ��ļ��У���Ӧ�����񣩸���
	 * 
	 * @param updateWorkDirs �����ļ��е��б�
	 */
	void onUpdateWorkTask(List<File> updateWorkDirs);

	/**
	 * ����target
	 * 
	 * @param addedTargetFileNames ����target���ļ��б�
	 */
	void onAddedTarget(List<File> addedTargetFileNames);

	/**
	 * ����work
	 * 
	 * @param addedWorkDirs ����work�ļ��� �б�
	 */
	void onAddedWorkTask(List<File> addedWorkDirs);

	/**
	 * ɾ��target
	 * 
	 * @param deleteTargetFiles ɾ��target���б�
	 */
	void onDeleteTarget(List<File> deleteTargetFiles);

	/**
	 * ɾ��work
	 * 
	 * @param deleteWorkDirs ɾ��work���ļ��б�
	 */
	void onDeleteWorkTask(List<File> deleteWorkDirs);
}
