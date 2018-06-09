package com.taobao.jingwei.webconsole.util.upload;

import java.util.List;
import java.util.Map;

import com.taobao.jingwei.webconsole.biz.exception.TimeoutException;

/**
 * �ϴ����ļ��洢��HA�Ĳ�ͬconsole��
 * @author shuohailhl
 *
 */
public interface UploadFileService extends UploadFileConst {
	/**
	 * ��ȡ�ļ����ĸ�ip�ϵ�map��key �ļ�����value �ļ����ڵ�ip�������̨�����϶������ļ����򷵻ر�����ip
	 * @param parentPath ��Ŀ¼
	 */
	Map<String, String> getAllFilesMap2Ip(String parentPath, int timeoutMils);

	/**
	 * ��ȡ����������tar�ļ�
	 * @return
	 */
	List<String> getLocalFileNames(String parentPath);

	/**
	 * ��ȡ����console�ϴ洢��tar��
	 * @return
	 */
	List<String> getAllFileNames(String ctxPath, int timeoutMils);

	/**
	 * ��ȡha�ĶԵ�console�����ϵ�tar file�ļ��б�
	 * @param timeout ��ʱʱ��
	 * @return 
	 */
	List<String> getPeerConsoleFileNames(String ip, int timeoutMils);

	/**
	 * ɾ���ԵȻ������ϴ���tar��
	 * @param ip ��������Ļ���ip
	 * @param tar �ļ���
	 * @param timeoutMils ��ʱʱ��
	 * @return <code>true</code>��ʾɾ���ɹ���<code>false</code>��ʾɾ��ʧ��
	 */
	Boolean delPeerConsoleFile(String ip, int port, String tar, int timeoutMils) throws TimeoutException;

	/**
	 * ��tar���ӱ���console���͵�jingwei server
	 * @param fileList Ҫ���͵��ļ��б�
	 * @param serverIpList Ҫ���͵ĵ���Ŀ�����ip�б�
	 * @return ����״̬�б�������fileList.size() * serverIpList.size()
	 */
	List<UploadStatus> sendToJwServer(List<String> fileList, List<String> serverIpList);

	/**
	 * ��tar���ӱ���console���͵�jingwei server
	 * @param file Ҫ���͵��ļ�
	 * @param serverIpList Ҫ���͵ĵ���Ŀ�����ip�б�
	 * @return ����״̬
	 */
	List<UploadStatus> sendToJwServer(String file, List<String> serverIpList);

	/**
	 * ��tar���ӱ���console���͵�jingwei server
	 * @param file Ҫ���͵��ļ�
	 * @param serverIpList Ҫ���͵ĵ���Ŀ�����ip�б�
	 * @param md5 �����md5������У��ʧ����û��
	 * @return ����״̬
	 */
	List<UploadStatus> sendToJwServer(String file, String md5, List<String> serverIpList);

}
