package com.bdindex.model;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

/**
 * 统一利用ModelFactory创建model
 **/

public class ReaderFactory {

	private static Logger logger = Logger.getLogger(ReaderFactory.class);

	// singleton
	private ReaderFactory() {
	}

	public static ReaderFactory newInstance() {
		return ReaderFactoryInstance.instance;
	}

	private static class ReaderFactoryInstance {
		static final ReaderFactory instance = new ReaderFactory();
	}

	/**
	 * 根据传入参数创建一个具体的Model生成类
	 * 
	 * @param 传入文件类型
	 * @return
	 */
	public Reader create(String type, File file) {
		// 载入所有Model生成类
		List<Class<? extends Reader>> readers;
		Reader res = null;
		try {
			readers = load("com.bdindex.model");
			for (Class<? extends Reader> reader : readers) {
				// 解析注解
				InputType inputType = praseInputType(reader);
				if (Objects.equals(type, inputType.value())) {
					try {
						res = reader.getConstructor(File.class).newInstance(
								file);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return res;
	}

	/**
	 * @param 生成model类所在包名
	 * @return 生成model类列表
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private List<Class<? extends Reader>> load(String packageName)
			throws URISyntaxException, UnsupportedEncodingException {
		List<Class<? extends Reader>> list = new ArrayList<Class<? extends Reader>>();

		// 判断是jar运行还是ide运行
		String path = ReaderFactory.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		path = URLDecoder.decode(path, "UTF-8");
		File jarFile = new File(path);
		System.out.println("jarFile exist!-->" + jarFile.exists());
		// 兼容从jar中读取
		String fileName = null; // 要载入的策略类
		if (jarFile.isFile()) {
			JarFile jar;
			try {
				jar = new JarFile(jarFile);
				final Enumeration<JarEntry> entries = jar.entries();
				// gives ALL entries in jar
				String newPackage = packageName.replace(".", "/") + "/";
				while (entries.hasMoreElements()) {
					final String name = entries.nextElement().getName();
					if (name.startsWith(newPackage) && name.endsWith(".class")) {
						fileName = name.replace("/", ".");
						fileName = fileName.replace(".class", "");
						// log
						try {
							Class<?> clazz = getClass().getClassLoader()
									.loadClass(fileName);
							if (clazz != Reader.class
									&& Reader.class.isAssignableFrom(clazz)) {
								// log
								logger.info("载入策略类： " + clazz.getName());
								list.add((Class<? extends Reader>) clazz);
							}
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			URI filePath = getClass().getClassLoader()
					.getResource(packageName.replace(".", "/")).toURI();
			// 获取并载入所有model生成类
			File[] mgFiles = new File(filePath).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".class"))
						return true;
					return false;
				}
			});
			for (File file : mgFiles) {
				String b = file.getName().replace(".class", "");
				String a = packageName + "." + b;
				System.out.println(a);
				fileName = a;
				// log
				try {
					Class<?> clazz = getClass().getClassLoader().loadClass(
							fileName);
					if (clazz != Reader.class
							&& Reader.class.isAssignableFrom(clazz)) {
						// log
						logger.info("载入策略类： " + clazz.getName());
						list.add((Class<? extends Reader>) clazz);
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		return list;
	}

	/**
	 * @param clazz
	 * @return InputType class
	 * @throws NullPointerException
	 */
	private InputType praseInputType(Class<? extends Reader> clazz) {
		InputType inputType = clazz.getAnnotation(InputType.class);
		return Objects.requireNonNull(inputType);
	}

}
