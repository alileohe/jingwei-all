package com.taobao.jingwei.core.util;

import com.taobao.tddl.venus.replicator.extractor.ExtractorException;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
*
<p>
description:����SERVERID���ɲ���
<p>
*
* ServerIdTest.java Create on Dec 6, 2012 6:36:50 PM
*
* Copyright (c) 2011 by qihao.
*
*@author
<a href="mailto:qihao@taobao.com">qihao</a>
*@version 1.0
*/
public class ServerIdTest {

	public static final int generateUniqueServerId(String sourceId, String ip) throws ExtractorException {
		try {
			// a=`echo $masterip|cut -d\. -f1`
			// b=`echo $masterip|cut -d\. -f2`
			// c=`echo $masterip|cut -d\. -f3`
			// d=`echo $masterip|cut -d\. -f4`
			// #server_id=`expr  $a \* 256 \* 256 \* 256 + $b \* 256 \* 256 + $c \* 256 + $d `
			// #server_id=$b$c$d
			// server_id=`expr   $b \* 256 \* 256 + $c \* 256 + $d `
			InetAddress localHost = InetAddress.getByName(ip);
			byte[] addr = localHost.getAddress();
			int salt = (sourceId != null) ? sourceId.hashCode() : 0;
			return ((0x7f & salt) << 24) + ((0xff & (int) addr[1]) << 16) // NL
					+ ((0xff & (int) addr[2]) << 8) // NL
					+ (0xff & (int) addr[3]);
		} catch (UnknownHostException e) {
			throw new ExtractorException("Unknown host", e);
		}
	}

	/**
	 * @param args
	 * @throws ExtractorException 
	 */
	public static void main(String[] args) throws ExtractorException {
		String taskName = "TOP-APP-AUTH-1";
		String ip = "172.24.160.164";
		System.out.println(generateUniqueServerId(taskName, ip));
	}
}