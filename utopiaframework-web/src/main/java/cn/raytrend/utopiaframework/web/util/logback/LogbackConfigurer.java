/*
 * LogbackConfigurer.java
 * 
 * Created on 29/11/2011
 */
package cn.raytrend.utopiaframework.web.util.logback;

import java.io.FileNotFoundException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * 读取 logback.xml 配置文件的配置器.
 * 
 * @author zhouych
 */
public abstract class LogbackConfigurer {
	
	protected static Logger logger = LoggerFactory.getLogger(LogbackConfigurer.class);
	
	protected static LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

	/**
	 * 初始化日志配置
	 * 
	 * @param location
	 * @throws FileNotFoundException
	 */
	public static void initLogging(String location) throws FileNotFoundException {
		String resolvedLocation = SystemPropertyUtils.resolvePlaceholders(location);
		URL url = ResourceUtils.getURL(resolvedLocation);
		JoranConfigurator configurator = new JoranConfigurator();
		// 需要 reset() 来清除以前的 Configuration 内容, 但是对于 multi-step Configuration, 该方法被忽略
		loggerContext.reset();
		configurator.setContext(loggerContext);
		try {
			configurator.doConfigure(url);
		} catch (JoranException e) {
			StatusPrinter.print(loggerContext.getStatusManager());
		}
	}

	/**
	 * 关闭日志配置
	 */
	public static void shutdownLogging() {
		loggerContext.stop();
	}
}
