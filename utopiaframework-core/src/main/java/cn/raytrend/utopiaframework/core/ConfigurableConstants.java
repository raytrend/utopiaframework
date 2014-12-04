/*
 * ConfigurableConstants.java
 * 
 * Created on 25/11/2011
 */
package cn.raytrend.utopiaframework.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.raytrend.utopiaframework.core.util.Constants;

/**
 * 一个可以读入 .properites 文件配置的 Constants 静态基类.
 * 
 * @author zhouych
 * @see Constants
 */
public class ConfigurableConstants {

	protected static Logger logger = LoggerFactory.getLogger(ConfigurableConstants.class);

	protected static Properties properties = new Properties();

	/**
	 * 静态读入属性文件到 properties 中
	 * 
	 * @param propertyFileName
	 *            属性文件名
	 */
	protected static void init(String propertyFileName) {
		InputStream in = null;
		try {
			in = ConfigurableConstants.class.getClassLoader().getResourceAsStream(propertyFileName);
			if (in != null) {
				properties.load(in);
			}
		} catch (IOException e) {
			logger.error("load {} into Constants error!", propertyFileName);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error("close {} error!", propertyFileName);
				}
			}
		}
	}
	
	/**
	 * 封装了 {@link Properties} 类的 {@link Properties#getProperty(String, String)}
	 * 函数，使得 properties 变量对子类透明化.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	protected static String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}
}
