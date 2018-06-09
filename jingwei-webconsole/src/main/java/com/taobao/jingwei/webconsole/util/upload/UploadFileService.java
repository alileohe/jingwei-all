package com.taobao.jingwei.webconsole.util.upload;

import java.util.List;
import java.util.Map;

import com.taobao.jingwei.webconsole.biz.exception.TimeoutException;

/**
 * 上传的文件存储在HA的不同console上
 * @author shuohailhl
 *
 */
public interface UploadFileService extends UploadFileConst {
	/**
	 * 获取文件在哪个ip上的map，key 文件名，value 文件所在的ip；如果两台机器上都存在文件，则返回本机的ip
	 * @param parentPath 父目录
	 */
	Map<String, String> getAllFilesMap2Ip(String parentPath, int timeoutMils);

	/**
	 * 获取本机的所有tar文件
	 * @return
	 */
	List<String> getLocalFileNames(String parentPath);

	/**
	 * 获取所有console上存储的tar包
	 * @return
	 */
	List<String> getAllFileNames(String ctxPath, int timeoutMils);

	/**
	 * 获取ha的对等console机器上的tar file文件列表；
	 * @param timeout 超时时间
	 * @return 
	 */
	List<String> getPeerConsoleFileNames(String ip, int timeoutMils);

	/**
	 * 删除对等机器上上传的tar包
	 * @param ip 接受请求的机器ip
	 * @param tar 文件名
	 * @param timeoutMils 超时时间
	 * @return <code>true</code>表示删除成功，<code>false</code>表示删除失败
	 */
	Boolean delPeerConsoleFile(String ip, int port, String tar, int timeoutMils) throws TimeoutException;

	/**
	 * 把tar包从本机console发送到jingwei server
	 * @param fileList 要发送的文件列表
	 * @param serverIpList 要发送的到的目标机器ip列表
	 * @return 发送状态列表；数量是fileList.size() * serverIpList.size()
	 */
	List<UploadStatus> sendToJwServer(List<String> fileList, List<String> serverIpList);

	/**
	 * 把tar包从本机console发送到jingwei server
	 * @param file 要发送的文件
	 * @param serverIpList 要发送的到的目标机器ip列表
	 * @return 发送状态
	 */
	List<UploadStatus> sendToJwServer(String file, List<String> serverIpList);

	/**
	 * 把tar包从本机console发送到jingwei server
	 * @param file 要发送的文件
	 * @param serverIpList 要发送的到的目标机器ip列表
	 * @param md5 计算的md5，用于校验失败了没有
	 * @return 发送状态
	 */
	List<UploadStatus> sendToJwServer(String file, String md5, List<String> serverIpList);

}
