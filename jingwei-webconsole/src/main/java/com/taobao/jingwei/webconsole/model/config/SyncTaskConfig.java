package com.taobao.jingwei.webconsole.model.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;

import com.taobao.jingwei.webconsole.model.config.applier.ApplierConfig;
import com.taobao.jingwei.webconsole.model.config.extractor.ExtractorConfig;

/**
 * @desc 
 * 
 * @author ˷�� <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Dec 21, 2012 6:49:48 PM
 */

public class SyncTaskConfig implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;
	private CommonConfig commonConfig;
	private ApplierConfig applierConfig;
	private ExtractorConfig extractorConfig;
	private CommonFilterConfig commonFilterConfig;

	public CommonConfig getCommonConfig() {
		return commonConfig;
	}

	public void setCommonConfig(CommonConfig commonConfig) {
		this.commonConfig = commonConfig;
	}

	public ApplierConfig getApplierConfig() {
		return applierConfig;
	}

	public void setApplierConfig(ApplierConfig applierConfig) {
		this.applierConfig = applierConfig;
	}

	public ExtractorConfig getExtractorConfig() {
		return extractorConfig;
	}

	public void setExtractorConfig(ExtractorConfig extractorConfig) {
		this.extractorConfig = extractorConfig;
	}

	public CommonFilterConfig getCommonFilterConfig() {
		return commonFilterConfig;
	}

	public void setCommonFilterConfig(CommonFilterConfig commonFilterConfig) {
		this.commonFilterConfig = commonFilterConfig;
	}

	public Object clone() {
		SyncTaskConfig o = null;
		try {
			o = (SyncTaskConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return o;
	}

	public Object deepClone() throws IOException, OptionalDataException, ClassNotFoundException {
		// ���Ƚ�����д������  
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream oo = new ObjectOutputStream(bo);
		oo.writeObject(this);

		// Ȼ�󽫶�������������  
		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
		ObjectInputStream oi = new ObjectInputStream(bi);

		return (oi.readObject());
	}

}
