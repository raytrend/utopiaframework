/*
 * LogbackWebConfigurer.java
 * 
 * Created on 28/11/2011
 */
package cn.raytrend.utopiaframework.web.util.logback;

import java.io.FileNotFoundException;

import javax.servlet.ServletContext;

import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.util.Log4jWebConfigurer;
import org.springframework.web.util.WebUtils;

import cn.raytrend.utopiaframework.web.listener.LogbackConfigListener;

/**
 * 仿照 {@link Log4jWebConfigurer} 写的 logback 的 web 配置器.
 * 
 * @author zhouych
 * @see LogbackConfigListener
 * @see LogbackConfigurer
 */
public abstract class LogbackWebConfigurer {

	/**
	 * logback 的配置文件 (一般是 logback.xml) 的路径
	 */
	public static final String CONFIG_LOCATION_PARAM = "logbackConfigLocation";
	
	/**
	 * 是否向 System 暴露 web app root 的变量
	 */
	public static final String EXPOSE_WEB_APP_ROOT_PARAM = "logbackExposeWebAppRoot";
	
	/**
	 * 开启日志功能.
	 * 
	 * @param servletContext
	 */
	public static void initLogging(ServletContext servletContext) {
		if (exposeWebAppRoot(servletContext)) {
			// 如果设置为暴露 web app root 的值则向 System 注册
			WebUtils.setWebAppRootSystemProperty(servletContext);
		}
		
		String location = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
		if (location != null) {
			try {
				if (!ResourceUtils.isUrl(location)) {
					location = SystemPropertyUtils.resolvePlaceholders(location);
					location = WebUtils.getRealPath(servletContext, location);
				}
				
				servletContext.log("Initializing logback from [" + location + "]");
				// 正式开启 logback
				LogbackConfigurer.initLogging(location);
			} catch (FileNotFoundException e) {
				throw new IllegalArgumentException("Invalid 'logbackConfigLocation' parameter: " + e.getMessage());
			}
		}
	}
	
	/**
	 * 关闭日志功能, 并且解除 web app root 变量的注册.
	 * 
	 * @param servletContext
	 */
	public static void shutdownLogging(ServletContext servletContext) {
		servletContext.log("Shutting down logback");
		try {
			LogbackConfigurer.shutdownLogging();
		} finally {
			if (exposeWebAppRoot(servletContext)) {
				// 如果向系统注册了该变量, 则需要在最后解除
				WebUtils.removeWebAppRootSystemProperty(servletContext);
			}
		}
	}
	
	/**
	 * 是否向 System 暴露 web app root 的路径变量, 如果为空不写则返回 true.
	 * 
	 * @param servletContext
	 * @return
	 */
	private static boolean exposeWebAppRoot(ServletContext servletContext) {
		String isExpose = servletContext.getInitParameter(EXPOSE_WEB_APP_ROOT_PARAM);
		return (isExpose == null || Boolean.valueOf(isExpose));
	}
}
