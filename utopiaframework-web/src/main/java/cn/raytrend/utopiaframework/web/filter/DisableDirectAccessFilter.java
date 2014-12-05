/*
 * DisableDirectAccessFilter.java
 * 
 * Created on 29/11/2011
 */
package cn.raytrend.utopiaframework.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

/**
 * 禁止用户直接访问指定的资源文件, 比如 .jsp 或 .vm 文件等. 在 web.xml 中可以配置如下:
 * 
 * <pre>
 * {@code
 * <filter>
 *     <filter-name>disableDirectAccessFilter</filter-name>
 *     <filter-class>org.mysterylab.utopiaframework.web.filter.DisableDirectAccessFilter</filter-class>
 *     <init-param>
 *         <param-name>enabled</param-name>
 *         <param-value>true</param-value>
 *     </init-param>
 *     <init-param>
 *         <param-name>redirectView</param-name>
 *         <param-value>/index</param-value>
 *     </init-param>
 * </filter>
 * 
 * <filter-mapping>
 *     <filter-name>disableDirectAccessFilter</filter-name>
 *     <url-pattern>*.jsp</url-pattern>
 *     <url-pattern>*.ftl</url-pattern>
 *     <url-pattern>*.vm</url-pattern>
 * </filter-mapping>
 * }
 * </pre>
 * 
 * 如此一来, 当用户请求的 url 是以 .jsp 结尾的话则跳转到指定的 url 请求, 默认跳回主页. 可以保护 .jsp
 * 文件不被直接访问而不需将其置入 WEB-INF/ 目录下. 因为放到 WEB-INF/ 下面开发起来无法做到可视化, 有点麻烦.
 * 
 * @author zhouych
 */
public class DisableDirectAccessFilter implements Filter {
	
	/**
	 * 是否开启, 默认为启用, 用于开发阶段
	 */
	private boolean isEnabled = true;
	
	/**
	 * 转向的页面
	 */
	private String redirectView;

	@Override
	public void init(FilterConfig config) throws ServletException {
		
		String enabled = config.getInitParameter("enabled");
		if (StringUtils.isNotBlank(enabled)) {
			isEnabled = Boolean.parseBoolean(enabled);
		}
		
		if (!isEnabled) {
			return;
		}
		
		// 根据 web.xml 决定要禁止的后缀名文件
		redirectView = config.getInitParameter("redirectView");
		if (StringUtils.isBlank(redirectView)) {
			redirectView = "/index";
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
		
		String contextPath = httpRequest.getContextPath();
		
		/*
		 * 创建 HttpSession, 注意如果没有下面这句话的话, 程序会抛出如下错误:
		 * 
		 * 2011-10-3 23:26:50 org.apache.jasper.runtime.JspFactoryImpl internalGetPageContext
		 * 严重: Exception initializing page context
		 * java.lang.IllegalStateException: Cannot create a session after the response has been committed
		 * 	       at org.apache.catalina.connector.Request.doGetSession(Request.java:2400)
		 *         ......
		 * 
		 * 出现的原因是在 response 输出响应之后才创建 HttpSession, 如此一来服务器已经将数据发送到客户端了,
		 * 无法再发送 Session id 了. 所以需要先创建 HttpSession.
		 */
		
		@SuppressWarnings("unused")
		HttpSession session = httpRequest.getSession(true);
		
		// 直接跳转到指定的页面, 比如 /index 主页
		httpResponse.sendRedirect(contextPath + redirectView);
		
		chain.doFilter(httpRequest, httpResponse);
	}
}
