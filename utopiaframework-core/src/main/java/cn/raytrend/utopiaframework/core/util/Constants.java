/*
 * Constants.java
 * 
 * Created on 25/11/2011
 */
package cn.raytrend.utopiaframework.core.util;

import cn.raytrend.utopiaframework.core.ConfigurableConstants;

/**
 * Utopiaframework 的系统级静态变量.
 * 
 * @author zhouych
 */
public class Constants extends ConfigurableConstants {

	// 静态初始化读入 src/config/utopiaframework.properties 中设置的变量
	static {
		init("config/utopiaframework.properties");
		logger.info("UtopiaFramework version - 1.0");
	}
	
	/**
	 * 版本值
	 */
	public final static String VERSION = getProperty("VERSION", "1.0");
	
	/**
     * 数据库 sql 形式
     */
    public static final String SQL_TIMESTAMP = "yyyy-MM-dd hh:mm:ss";
}
