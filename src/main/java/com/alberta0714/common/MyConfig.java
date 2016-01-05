package com.alberta0714.common;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyConfig {
	private static Logger logger = LoggerFactory.getLogger(MyConfig.class);

	private static Properties p = new Properties();
	private static long lastModify = -1;
	private static long expires = 0;
	private static final String configPath = "web.properties";

	private static void reLoad() {
		try {
			p.load(MyConfig.class.getResourceAsStream("/" + configPath));
		} catch (Exception e) {
			logger.error("加载配置文件出错", e);
		}
	}

	public static String getProperty(String name) {
		long now = (new Date()).getTime();
		if (now > expires) {
			File f = new File(MyConfig.class.getResource("/").getPath() + configPath);
			if (f.lastModified() != lastModify) {
				lastModify = f.lastModified();

				reLoad();
			}
			expires = now + 6 * 1000;
		}

		return p.getProperty(name);
	}

	/**
	 * getProperty方法的简写
	 * 
	 * @param name
	 * @return
	 */
	public static String get(String name) {
		return getProperty(name);
	}

	/**
	 * 获取一个配制项，如果项没有被配制，则返回设置的默认值
	 * 
	 * @param name
	 *            配制项名
	 * @param defaultValue
	 *            默认值
	 * @return 配制值
	 */
	public static String get(String name, String defaultValue) {
		String ret = getProperty(name);
		return ret == null ? defaultValue : ret;
	}

	/**
	 * 从配制文件中获取一个整形的配制值，如果没有配制，则返回默认值
	 * 
	 * @param item
	 * @param defaultValue
	 * @return
	 */
	public static int getInt(String item, int defaultValue) {
		String value = getProperty(item);
		if (value == null) {
			return defaultValue;
		}
		int ret = defaultValue;
		try {
			ret = Integer.parseInt(value);
		} catch (Exception ignor) {

		}
		return ret;
	}

	/**
	 * 从配制文件中获取一个整型的缓存时间，单位（分钟），如果没有配制值，则返回默认值
	 * 
	 * @param cacheItem
	 *            缓存项名
	 * @param defaultValue
	 *            默认缓存时间
	 * @return 缓存时间
	 */
	public static long getCacheTime(String cacheItem, int defaultValue) {
		int ret = getInt(cacheItem, defaultValue);
		return 60L * 1000 * ret;
	}

	/**
	 * 从配制文件中获取一个具有最小值的整型缓存时间，单位（分钟），如果没有配制值，则返回默认值，如果配制值小于最小值，则返回最小值。
	 * 
	 * @param cacheItem
	 *            缓存项名
	 * @param defaultValue
	 *            默认缓存时间
	 * @param minValue
	 *            最小缓存时间
	 * @return 缓存时间
	 */
	public static long getCacheTime(String cacheItem, int defaultValue, int minValue) {
		int ret = getInt(cacheItem, defaultValue);
		if (ret < minValue) {
			ret = minValue;
		}
		return 60L * 1000 * ret;
	}
}
