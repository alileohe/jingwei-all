package com.taobao.jingwei.server.plugin;

import java.io.File;
import java.util.List;

/**
 * @desc 扫描到plugin，work目录变化，通知关注者
 * 
 * @author <a href="mailto:shuohai.lhl@taobao.com">朔海 shuohailhl</a>
 * 
 * @date 2011-12-7下午5:24:04
 */
public interface IPluginNotifier {
	/**
	 * target目录下的文件（对应的任务）更新
	 * 
	 * @param updateTargetFileNames 更新文件的列表
	 */
	void onUpdateTarget(List<File> updateTargetFileNames);

	/**
	 * work目录下的文件夹（对应的任务）更新
	 * 
	 * @param updateWorkDirs 更新文件夹的列表
	 */
	void onUpdateWorkTask(List<File> updateWorkDirs);

	/**
	 * 新增target
	 * 
	 * @param addedTargetFileNames 新增target的文件列表
	 */
	void onAddedTarget(List<File> addedTargetFileNames);

	/**
	 * 新增work
	 * 
	 * @param addedWorkDirs 新增work文件夹 列表
	 */
	void onAddedWorkTask(List<File> addedWorkDirs);

	/**
	 * 删除target
	 * 
	 * @param deleteTargetFiles 删除target的列表
	 */
	void onDeleteTarget(List<File> deleteTargetFiles);

	/**
	 * 删除work
	 * 
	 * @param deleteWorkDirs 删除work的文件列表
	 */
	void onDeleteWorkTask(List<File> deleteWorkDirs);
}
