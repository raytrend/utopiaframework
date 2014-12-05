/*
 * CharacterEncodingFilter.java
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

/**
 * 字符编码的过滤器, 推荐使用 Spring 的 {@link org.springframework.web.filter.CharacterEncodingFilter} 而不是本过滤器.
 * 考虑到 Spring 的字符过滤器只支持 servlet 2.4+ 的环境, 为了兼容某些 Web 容器(如 Resin 2), 而特意提供该过滤器. 使用时需要在
 * web.xml 中声明如下:
 * 
 * <pre>
 * {@code
 * <filter>
 *     <filter-name>encodingFilter</filter-name>
 *     <filter-class>org.mysterylab.utopiaframework.web.filter.CharacterEncodingFilter</filter-class>
 *     <init-param>
 *         <param-name>encoding</param-name>
 *         <param-value>UTF-8</param-value>
 *     </init-param>
 * </filter>
 * <filter-mapping>
 *     <filter-name>encodingFilter</filter-name>
 *     <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * }
 * </pre>
 * 
 * <b>WARNING: </b>注意需要将该过滤器置于所有过滤器之前.
 * 
 * @author zhouych
 */
public class CharacterEncodingFilter implements Filter {
	
	/**
	 * 默认编码类型
	 */
	private String encoding = "UTF-8";

	@Override
	public void init(FilterConfig config) throws ServletException {
		String encodingParam = config.getInitParameter("encoding");
		if (encodingParam != null) {
			encoding = encodingParam;
		}
	}
	
	@Override
	public void destroy() {
		// 不做处理
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		// 只处理 HTTP 请求
		if (! (request instanceof HttpServletRequest)) {
			chain.doFilter(request, response);
			return;
		}
		
		request.setCharacterEncoding(encoding);
		
		chain.doFilter(request, response);
	}
}
