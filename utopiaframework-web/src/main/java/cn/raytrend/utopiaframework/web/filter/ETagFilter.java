/*
 * ETagFilter.java
 * 
 * Created on 29/11/2011
 */
package cn.raytrend.utopiaframework.web.filter;

import java.io.IOException;
import java.util.zip.CRC32;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import cn.raytrend.utopiaframework.web.http.ByteHttpServletResponseWrapper;
import cn.raytrend.utopiaframework.web.util.HttpUtil;

/**
 * 使用 ETag 提高性能的过程如下:
 * <ul>
 *     <li>1) 客户端请求页面A.</li>
 *     <li>2) 服务端返回页面A, 并为页面A设置一个ETag.</li>
 *     <li>3) 客户端显示页面A, 并将页面A和ETag一同缓存起来.</li>
 *     <li>4) 客户端再次请求页面A, 并将上次的ETag返回给服务端.</li>
 *     <li>5) 服务端检测ETag, 如果一致则返回响应304和一个空的响应体.</li>
 * </ul>
 * 在 web.xml 中可配置如下:
 * 
 * <pre>
 * {@code
 * <filter>
 *     <filter-name>etagFilter</filter-name>
 *     <filter-class>org.mysterylab.utopiaframework.web.filter.ETagFilter</filter-class>
 *     <init-param>
 *         <param-name>enabled</param-name>
 *         <param-value>true</param-value>
 *     </init-param>
 * </filter>
 * 
 * <filter-mapping>
 *     <filter-name>etagFilter</filter-name>
 *     <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * }
 * </pre>
 * 
 * 另外, ETag 可以由 Web 服务器来生成, 在服务器中可以自由定义 ETag 的格式和计算方法. Tomcat
 * 最为简单, 使用文档的大小和最后编辑时间生成; Apache 会利用更复杂的算法.
 * 在这里的代码只是简单使用文档大小来进行判定, 所以当服务器有 ETag 生成的时候, 可以将其关闭.
 * 
 * @author zhouych
 *
 */
public class ETagFilter implements Filter {
	
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
		
		if (!isEnabled) {
			return;
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
		
		ByteHttpServletResponseWrapper responseWrapper = new ByteHttpServletResponseWrapper(httpResponse);
		
		chain.doFilter(request, responseWrapper);
		
		responseWrapper.flushBuffer();
		
		byte[] responseData = responseWrapper.getResponseData();
		
		// 直接根据内容用 crc32 来计算 ETag, 当然也可以用 md5, 这里需要考虑哪个速度快(未测试)
		CRC32 crc32 = new CRC32();
		crc32.update(responseData);
		
		String token = "w/\"" + crc32.getValue() + "\"";
		HttpUtil.setEtagHeader(httpResponse, token);
		
		if (HttpUtil.checkIfNoneMatchHeader(httpRequest, httpResponse, token)) {
			// 如果已经修改了, 则重新发送内容
			httpResponse.setContentLength(responseData.length);
			ServletOutputStream out = httpResponse.getOutputStream();
			if (out != null) {
				out.write(responseData);
				out.flush();
				out.close();
			}
		}
	}
}
