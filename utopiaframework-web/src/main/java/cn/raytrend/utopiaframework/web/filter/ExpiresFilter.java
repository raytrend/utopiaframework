/*
 * ExpiresFilter.java
 * 
 * Created on 29/11/2011
 */
package cn.raytrend.utopiaframework.web.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.raytrend.utopiaframework.web.util.HttpUtil;

/**
 * 利用浏览器端的 Expires 标记来缓存静态文件, 比如 css 和 jpg 文件, 在 web.xml 中可配置如下:
 * 
 * <pre>
 * {@code
 * <filter>
 *     <filter-name>expiresFilter</filter-name>
 *     <filter-class>org.mysterylab.utopiaframework.web.filter.ExpiresFilter</filter-class>
 *     <init-param>
 *         <param-name>enabled</param-name>
 *         <param-value>true</param-value>
 *     </init-param>
 *     <init-param>
 *         <param-name>debug</param-name>
 *         <param-value>false</param-value>
 *     </init-param>
 *     <init-param>
 *         <param-name>.css</param-name>
 *         <param-value>86400</param-value>
 *     </init-param>
 *     <init-param>
 *         <param-name>.jpg</param-name>
 *         <param-value>86400</param-value>
 *     </init-param>
 *     <init-param>
 *         <param-name>.png</param-name>
 *         <param-value>86400</param-value>
 *     </init-param>
 * </filter>
 * 
 * <filter-mapping>
 *     <filter-name>expiresFilter</filter-name>
 *     <url-pattern>*.css</url-pattern>
 *     <url-pattern>*.jpg</url-pattern>
 *     <url-pattern>*.png</url-pattern>
 * </filter-mapping>
 * }
 * </pre>
 * 
 * 以上代码表示将指定的静态文件缓存时间设置为 86400s(一天). 当然也可以通过 Apache 服务器的
 * mode_expires 模块来配置. 另外需要注意的是客户端如果采用了 F5 键来刷新的话 Expires 标记将失效.
 * 
 * @author zhouych
 */
public class ExpiresFilter implements Filter {
	
	protected Logger logger = LoggerFactory.getLogger(ExpiresFilter.class);
	
	/**
	 * 是否开启, 默认为未启用, 用于开发阶段
	 */
	private boolean isEnabled = true;
	
	/**
	 * 是否输出调试信息, 默认为启用, 部署时可将其关闭
	 */
	private boolean isDebug = true;
	
	/**
	 * 指示静态资源的 map, key 为资源的后缀名, value 为过期时间
	 */
	private Map<String, Long> resourcesMap = new HashMap<String, Long>(5);
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(FilterConfig config) throws ServletException {
		
		String enabled = config.getInitParameter("enabled");
		if (StringUtils.isNotBlank(enabled)) {
			isEnabled = Boolean.parseBoolean(enabled);
		}
		
		if (!isEnabled) {
			return;
		}
		
		String debug = config.getInitParameter("debug");
		if (StringUtils.isNotBlank(debug)) {
			isDebug = Boolean.parseBoolean(debug);
		}
		
		// 如果没有禁止, 则继续读取参数
		Enumeration<String> parameterNames = config.getInitParameterNames();
		while (parameterNames.hasMoreElements()) {
			String paramName = parameterNames.nextElement();
			if (paramName.equals("enabled") || paramName.equals("debug")) {
				// 忽略 enabled & debug 的参数
				continue;
			}
			String paraValue = config.getInitParameter(paramName);
			if (StringUtils.isNumeric(paraValue)) {
				long time = Long.valueOf(paraValue);
				resourcesMap.put(paramName, time);
				if (isDebug) {
					logger.debug("[logger] - file -> {} & expiredTime -> {}", paramName, time);
				}
			}
		}
	}

	@Override
	public void destroy() {
		// 不做处理
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		// 如果未开启功能, 则不做任何处理
		if (!isEnabled) {
			chain.doFilter(request, response);
			return;
		}
		
		// 只处理 HTTP 请求
		if (! (request instanceof HttpServletRequest)) {
			chain.doFilter(request, response);
			return;
		}
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		String uri = httpRequest.getRequestURI();
		int tmp = uri.lastIndexOf(".");
		if (tmp != -1) {
			// 如果请求的是有后缀名的文件, 比如 index.jsp 和 style.css 等文件
			String ext = uri.substring(tmp);
			Long expiredTime = resourcesMap.get(ext);
			if (expiredTime != null) {
				HttpUtil.setExpiresHeader(httpResponse, expiredTime.longValue());
			}
		}
		chain.doFilter(httpRequest, httpResponse);
	}
}
