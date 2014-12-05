/*
 * LogbackConfigListener.java
 * 
 * Created on 28/11/2011
 */
package cn.raytrend.utopiaframework.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.util.Log4jConfigListener;

import cn.raytrend.utopiaframework.web.util.logback.LogbackWebConfigurer;

/**
 * 仿照 {@link Log4jConfigListener} 编写一个 logback 日志的监听器. 注意到该 listener 必须在 {@link ContextLoaderListener}
 * 之前注册, 比如 web.xml 文件可编写如下:
 * <pre>
 * {@code
 * <context-param>
 *     <param-name>webAppRootKey</param-name>
 *     <param-value>webName.root</param-value>
 * </context-param>
 * <context-param>
 *     <param-name>logbackExposeWebAppRoot</param-name>
 *     <param-value>true</param-value>
 * </context-param>
 * <context-param>
 *     <param-name>logbackConfigLocation</param-name>
 *     <param-value>/WEB-INF/classes/logback.xml</param-value>
 * </context-param>
 * 
 * <listener>
 *     <listener-class>org.mysterylab.utopiaframework.web.listener.LogbackConfigListener</listener-class>
 * </listener>
 * <listener>
 *     <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
 * </listener>
 * }
 * </pre>
 * 之后, 可以在 logback.xml 的文件路径中以类似 "${webName.root}/WEB-INF/logs/webName.log" 的形式来配置生成的 log 路径.
 * 
 * @author zhouych
 */
public class LogbackConfigListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		LogbackWebConfigurer.initLogging(event.getServletContext());
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		LogbackWebConfigurer.shutdownLogging(event.getServletContext());
	}
}
