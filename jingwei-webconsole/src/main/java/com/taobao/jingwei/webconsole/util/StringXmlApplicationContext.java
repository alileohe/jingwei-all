package com.taobao.jingwei.webconsole.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 * @desc 
 * 
 * @author Ë·º£ <a href="mailto:shuohai.lhl@taobao.com"> shuohailhl</a>
 * 
 * @date Jan 4, 2013 1:32:11 PM
 */
public class StringXmlApplicationContext extends AbstractXmlApplicationContext {
	private Resource[] configResources;
	private ClassLoader cl;

	public StringXmlApplicationContext(String stringXml) {
		this(new String[] { stringXml }, null, null);
	}

	public StringXmlApplicationContext(String[] stringXmls) {
		this(stringXmls, null, null);
	}

	public StringXmlApplicationContext(String stringXml, ClassLoader cl) {
		this(new String[] { stringXml }, null, cl);
	}

	public StringXmlApplicationContext(String[] stringXmls, ClassLoader cl) {
		this(stringXmls, null, cl);
	}

	public StringXmlApplicationContext(String[] stringXmls, ApplicationContext parent, ClassLoader cl) {
		super(parent);
		this.cl = cl;
		this.configResources = new Resource[stringXmls.length];
		for (int i = 0; i < stringXmls.length; i++) {
			this.configResources[i] = new ByteArrayResource(stringXmls[i].getBytes());
		}
		refresh();
	}

	protected Resource[] getConfigResources() {
		return this.configResources;
	}

	@Override
	public ClassLoader getClassLoader() {
		if (cl == null) {
			return super.getClassLoader();
		} else {
			return this.cl;
		}
	}
}