package com.taobao.jingwei.webconsole.model.config;

import java.util.List;

import com.taobao.jingwei.webconsole.model.config.util.ConfigUtil;

/**
 * @desc �Ӷ��ŷָ��������ַ������л�ȡ��׺����
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 8, 2013 9:45:29 AM
 */

public class DefaultSuffixPolicy implements SuffixPolicy {

	private String commaSepString;

	public List<Integer> createSuffixes() {
		return ConfigUtil.commaSepString2IntList(this.getCommaSepString());
	}

	public String getCommaSepString() {
		return commaSepString;
	}

	public void setCommaSepString(String commaSepString) {
		this.commaSepString = commaSepString;
	}

}
