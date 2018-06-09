package com.taobao.jingwei.common.dynacode;

import com.alibaba.common.lang.ArrayUtil;
import com.alibaba.common.lang.StringUtil;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.*;

/**   
 * <p>description: java��̬�����࣬��������ʱ��Ҫ���������java�����text��code�ַ���
 * 	�ٵ���compileAndLoadClass�������б��룬����compileAndLoadClass֮ǰ����ͨ����ص�
 * 	SET���������ñ����һЩ����
 * <p> 
 *
 * @{#} DynaCode.java Create on Sep 22, 2011 11:34:10 AM   
 *   
 * Copyright (c) 2011 by qihao.
 *
 *@author <a href="mailto:qihao@taobao.com">qihao</a> 
 *@version 1.0   
 */
public class DynaCode {

	private static final Log logger = LogFactory.getLog(DynaCode.class);

	private static final String FILE_SP = System.getProperty("file.separator");

	private static final String LINE_SP = System.getProperty("line.separator");

	/**
	 * ����java�ļ���·��
	 */
	private String sourcePath = System.getProperty("java.io.tmpdir") + FILE_SP + "dynacode";

	/**
	 * ����class�ļ���·��
	 */
	private String outPutClassPath = sourcePath;

	/**
	 * ����ʹ�õ�����ClassPath��ClassLoader
	 */
	private ClassLoader parentClassLoader;

	/**
	 * java��text����
	 */
	private List<String> codeStrs;

	/**
	 * �Ѿ����غõ�class
	 */
	private Map<String/*fullClassName*/, Class<?>/*class*/> loadClass;

	/**
	 * �������,ʹ�õ�classpath�������ָ����ʹ�õ�ǰ������
	 * classloder�����е�classpath���б���
	 */
	private String classpath;

	/**
	 * ���������ͬjavac��bootclasspath
	 */
	private String bootclasspath;

	/**
	 * ���������ͬjavac��extdirs
	 */
	private String extdirs;

	/**
	 * ���������ͬjavac��encoding
	 */
	private String encoding = "GBK";

	/**
	 *  ���������ͬjavac��target
	 */
	private String target;

	@SuppressWarnings("unchecked")
	public DynaCode(String code) {
		this(Thread.currentThread().getContextClassLoader(), ArrayUtil.toList(new String[] { code }));
	}

	public DynaCode(List<String> codeStrs) {
		this(Thread.currentThread().getContextClassLoader(), codeStrs);
	}

	public DynaCode(ClassLoader parentClassLoader, List<String> codeStrs) {
		this(extractClasspath(parentClassLoader), parentClassLoader, codeStrs);
	}

	public DynaCode(String classpath, ClassLoader parentClassLoader, List<String> codeStrs) {
		this.classpath = classpath;
		this.parentClassLoader = parentClassLoader;
		this.codeStrs = codeStrs;
		this.loadClass = new HashMap<String, Class<?>>(codeStrs.size());
	}

	/**���벢�Ҽ��ظ�����java������
	 * @throws Exception
	 */
	public void compileAndLoadClass() throws Exception {
		String[] sourceFiles = this.uploadSrcFile();
		this.compile(sourceFiles);
		this.loadClass(this.loadClass.keySet());
	}

	public static String getClassName(String code) {
		String className = StringUtil.substringBefore(code, "{");
		if (StringUtil.isBlank(className)) {
			return className;
		}
		if (StringUtil.contains(code, " class ")) {
			className = StringUtil.substringAfter(className, " class ");
			if (StringUtil.contains(className, " extends ")) {
				className = StringUtil.substringBefore(className, " extends ").trim();
			} else if (StringUtil.contains(className, " implements ")) {
				className = StringUtil.trim(StringUtil.substringBefore(className, " implements "));
			} else {
				className = StringUtil.trim(className);
			}
		} else if (StringUtil.contains(code, " interface ")) {
			className = StringUtil.substringAfter(className, " interface ");
			if (StringUtil.contains(className, " extends ")) {
				className = StringUtil.substringBefore(className, " extends ").trim();
			} else {
				className = StringUtil.trim(className);
			}
		} else if (StringUtil.contains(code, " enum ")) {
			className = StringUtil.trim(StringUtil.substringAfter(className, " enum "));
		} else {
			return StringUtil.EMPTY_STRING;
		}
		return className;
	}

	public static String getPackageName(String code) {
		String packageName = StringUtil.substringBefore(StringUtil.substringAfter(code, "package "), ";").trim();
		return packageName;
	}

	public static String getQualifiedName(String code) {
		StringBuilder sb = new StringBuilder();
		String className = getClassName(code);
		if (StringUtil.isNotBlank(className)) {

			String packageName = getPackageName(code);
			if (StringUtil.isNotBlank(packageName)) {
				sb.append(packageName).append(".");
			}
			sb.append(className);
		}
		return sb.toString();
	}

	public static String getFullClassName(String code) {
		String packageName = getPackageName(code);
		String className = getClassName(code);
		return StringUtil.isBlank(packageName) ? className : packageName + "." + className;
	}

	/**���ظ���className��class
	 * @param classFullNames
	 * @throws ClassNotFoundException
	 * @throws MalformedURLException 
	 */
	private void loadClass(Set<String> classFullNames) throws ClassNotFoundException, MalformedURLException {
		synchronized (loadClass) {
			//ʹ��outPutClassPath��URL�������µ�ClassLoader
			ClassLoader classLoader = new URLClassLoader(new URL[] { new File(outPutClassPath).toURI().toURL() },
					parentClassLoader);
			for (String key : classFullNames) {
				Class<?> classz = classLoader.loadClass(key);
				if (null != classz) {
					loadClass.put(key, classz);
					logger.warn("Dyna Load Java Class File OK:----> className: " + key);
				} else {
					logger.warn("Dyna Load Java Class File Fail:----> className: " + key);
				}
			}
		}
	}

	/**��������ļ�����·����java�ļ��б�
	 * @param srcFiles
	 * @throws Exception
	 */
	private void compile(String[] srcFiles) throws Exception {
		String args[] = this.buildCompileJavacArgs(srcFiles);
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		logger.warn("Dyna Complie Java Class File:---->" + ToStringBuilder.reflectionToString(args));
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			throw new NullPointerException(
					"ToolProvider.getSystemJavaCompiler() return null,please use JDK replace JRE!");
		}
		int resultCode = compiler.run(null, null, err, args);
		if (resultCode != 0) {
			throw new Exception(err.toString());
		}
	}

	/**������code��text����java�ļ�������д��Ӳ��
	 * @return
	 * @throws Exception
	 */
	private String[] uploadSrcFile() throws Exception {
		List<String> srcFileAbsolutePaths = new ArrayList<String>(codeStrs.size());
		for (String code : codeStrs) {
			if (StringUtil.isNotBlank(code)) {
				String packageName = getPackageName(code);
				String className = getClassName(code);
				if (StringUtil.isNotBlank(className)) {
					File srcFile = null;
					BufferedWriter bufferWriter = null;
					try {
						if (StringUtil.isBlank(packageName)) {
							File pathFile = new File(sourcePath);
							//��������ھʹ���
							if (!pathFile.exists()) {
								if (!pathFile.mkdirs()) {
									throw new RuntimeException("create PathFile Error!");
								}
							}
							srcFile = new File(sourcePath + FILE_SP + className + ".java");
						} else {
							String srcPath = StringUtil.replace(packageName, ".", FILE_SP);
							File pathFile = new File(sourcePath + FILE_SP + srcPath);
							//��������ھʹ���
							if (!pathFile.exists()) {
								if (!pathFile.mkdirs()) {
									throw new RuntimeException("create PathFile Error!");
								}
							}
							srcFile = new File(pathFile.getAbsolutePath() + FILE_SP + className + ".java");
						}
						synchronized (loadClass) {
							loadClass.put(getFullClassName(code), null);
						}
						if (null != srcFile) {
							logger.warn("Dyna Create Java Source File:---->" + srcFile.getAbsolutePath());
							srcFileAbsolutePaths.add(srcFile.getAbsolutePath());
							srcFile.deleteOnExit();
						}
						OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(srcFile),
								encoding);
						bufferWriter = new BufferedWriter(outputStreamWriter);
						for (String lineCode : code.split(LINE_SP)) {
							bufferWriter.write(lineCode);
							bufferWriter.newLine();
						}
						bufferWriter.flush();
					} finally {
						if (null != bufferWriter) {
							bufferWriter.close();
						}
					}
				}
			}
		}
		return srcFileAbsolutePaths.toArray(new String[srcFileAbsolutePaths.size()]);
	}

	/**���ݸ����ļ��б�͵�ǰ�ı������������
	 * ����javac�ı����������
	 * @param srcFiles
	 * @return
	 */
	private String[] buildCompileJavacArgs(String srcFiles[]) {
		ArrayList<String> args = new ArrayList<String>();
		if (StringUtil.isNotBlank(classpath)) {
			args.add("-classpath");
			args.add(classpath);
		}
		if (StringUtil.isNotBlank(outPutClassPath)) {
			args.add("-d");
			args.add(outPutClassPath);
		}
		if (StringUtil.isNotBlank(sourcePath)) {
			args.add("-sourcepath");
			args.add(sourcePath);
		}
		if (StringUtil.isNotBlank(bootclasspath)) {
			args.add("-bootclasspath");
			args.add(bootclasspath);
		}
		if (StringUtil.isNotBlank(extdirs)) {
			args.add("-extdirs");
			args.add(extdirs);
		}
		if (StringUtil.isNotBlank(encoding)) {
			args.add("-encoding");
			args.add(encoding);
		}
		if (StringUtil.isNotBlank(target)) {
			args.add("-target");
			args.add(target);
		}
		for (int i = 0; i < srcFiles.length; i++) {
			args.add(srcFiles[i]);
		}
		return args.toArray(new String[args.size()]);
	}

	/**
	 * ���ݸ�����classLoader��ȡ���Ӧ��classPath�������ַ���
	 * ·��
	 * URLClassLoader.
	 */
	private static String extractClasspath(ClassLoader cl) {
		StringBuffer buf = new StringBuffer();
		while (cl != null) {
			if (cl instanceof URLClassLoader) {
				URL urls[] = ((URLClassLoader) cl).getURLs();
				for (int i = 0; i < urls.length; i++) {
					if (buf.length() > 0) {
						buf.append(File.pathSeparatorChar);
					}
					String s = urls[i].getFile();
					try {
						s = URLDecoder.decode(s, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						continue;
					}
					File f = new File(s);
					buf.append(f.getAbsolutePath());
				}
			}
			cl = cl.getParent();
		}
		return buf.toString();
	}

	public String getOutPutClassPath() {
		return outPutClassPath;
	}

	public void setOutPutClassPath(String outPutClassPath) {
		this.outPutClassPath = outPutClassPath;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public ClassLoader getParentClassLoader() {
		return parentClassLoader;
	}

	public void setParentClassLoader(ClassLoader parentClassLoader) {
		this.parentClassLoader = parentClassLoader;
	}

	public String getClasspath() {
		return classpath;
	}

	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	public String getBootclasspath() {
		return bootclasspath;
	}

	public void setBootclasspath(String bootclasspath) {
		this.bootclasspath = bootclasspath;
	}

	public String getExtdirs() {
		return extdirs;
	}

	public void setExtdirs(String extdirs) {
		this.extdirs = extdirs;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public Map<String, Class<?>> getLoadClass() {
		return loadClass;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		//׼����̬��java��code�ַ���
		StringBuffer interfaceSb = new StringBuffer();
		interfaceSb.append("package com.taobao.jingwei.tasks.test;");
		interfaceSb.append("public interface Hello {public String say();}");
		StringBuffer classSb = new StringBuffer();
		classSb.append("package com.taobao.jingwei.tasks.test;");
		classSb.append("public class HelloImpl implements Hello {public String say() {return \"We Say Hello �ҿ�!\";}}");

		List<String> codes = new ArrayList<String>(2);
		codes.add(interfaceSb.toString());
		codes.add(classSb.toString());
		//����DynaCode
		DynaCode dc = new DynaCode(codes);
		//ִ�б��벢��load
		dc.compileAndLoadClass();
		//��ȡ��Ӧ��clazz
		Map<String, Class<?>> map = dc.getLoadClass();
		//����ִ�н��
		Class<?> clazz = map.get(getQualifiedName(classSb.toString()));
		Object helloObj = (Object) clazz.newInstance();
		String str = (String) clazz.getMethod("say", new Class<?>[] {}).invoke(helloObj, new Object[] {});
		System.out.println(str);
	}
}