/*
 * DisableUrlSessionFilter.java
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
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

/**
 * 禁止 URL 重写并阻止产生诸如 jsessionid 问题的过滤器.
 * <p>
 * jsessionid 是诸如 Tomcat 等容器为了支持浏览器的禁用 cookie 情况, 而为了保持当前请求 session
 * 的状态, 使用了一个 jsessionid 来作为当前 session 请求的标识, 这就是所谓的 session id, 也称为
 * "内存 cookie "; 而常说的 cookie 则称为 "硬盘 cookie ", 因为它是存储在硬盘当中.</p>
 * <p>
 * jsessionid 的表示形式为在 URL 请求的后面加一个参数, 如: http://mysterylab.com/info/1;jsessionid=SESSIOON_IDENTIFIER,
 * 这样在后台处理 info 的 id 转为 Integer 类型的时候会出错. 另外, jsessionid 还有一些安全隐患,
 * 所以我们需要禁止它出现.</p>
 * 
 * 在 web.xml 中可配置如下:
 * 
 * <pre>
 * {@code
 * <filter>
 *     <filter-name>disableUrlSessionFilter</filter-name>
 *     <filter-class>org.mysterylab.utopiaframework.web.filter.DisableUrlSessionFilter</filter-class>
 *     <init-param>
 *         <param-name>enabled</param-name>
 *         <param-value>true</param-value>
 *     </init-param>
 * </filter>
 * 
 * <filter-mapping>
 *     <filter-name>disableUrlSessionFilter</filter-name>
 *     <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * }
 * </pre>
 * 
 * @author zhouych
 */
public class DisableUrlSessionFilter implements Filter {
	
	/**
	 * 是否开启, 默认为启用, 用于开发阶段
	 */
	private boolean isEnabled = true;

	@Override
	public void init(FilterConfig config) throws ServletException {
		
		String enabled = config.getInitParameter("enabled");
		if (StringUtils.isNotBlank(enabled)) {
			isEnabled = Boolean.parseBoolean(enabled);
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
		
		// 如果该请求的 session id 是通过客户端的 URL 的一部分提供的, 则将该 session 失效
		if (httpRequest.isRequestedSessionIdFromURL()) {
			HttpSession session = httpRequest.getSession();
			if (session != null) {
				session.invalidate();
			}
		}
		
		/*
		 * 采用包装器来封装服务器的响应, 主要是将之前本身应该加入 URL 的 jsessionid 去除.
		 * jsessionid 问题本身也是由于 redirect 重写而导致的, 所以现在这里将重写前的 url 直接返回.
		 */
		HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(httpResponse) {

			@Override
			public String encodeRedirectUrl(String url) {
				return url;
			}

			@Override
			public String encodeRedirectURL(String url) {
				return url;
			}

			@Override
			public String encodeUrl(String url) {
				return url;
			}

			@Override
			public String encodeURL(String url) {
				return url;
			}
		};
		
		// 继续传递处理
		chain.doFilter(request, responseWrapper);
	}
}
