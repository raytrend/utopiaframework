/*
 * GzipFilter.java
 * 
 * Created on 29/11/2011
 */
package cn.raytrend.utopiaframework.web.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
import cn.raytrend.utopiaframework.web.util.codec.CompressUtil;

/**
 * Gzip 是 HTTP 协议中使用的一种加密算法, 它可以将服务器端返回的页面文件和资源文件等通过高压缩算法将其压缩,
 * 然后再传输到客户端, 由客户端的浏览器负责解压缩和呈现, 一般可以压缩 60% 的流量, 效果非常显著.<br>
 * 
 * 实现 Gzip 压缩有两种做法, 一种是在服务器端 (如 Tomcat ) 中开启其功能, 比如:
 * 
 * <pre>
 * {@code
 * <Connector port="8080" maxHttpHeaderSize="8192" maxThreads="150"
 *     minSpareThreads="25" maxSpareThreads="75" enableLookups="false" redirectPort="8443"
 *     acceptCount="100" connectionTimeout="20000" disableUploadTimeout="true" compression="on"
 *     compressionMinSize="2048" noCompressionUserAgents="gozilla, traviata"
 *     compressableMimeType="text/html,text/xml,text/css,text/javascript,image/gif,image/jpg" />
 * }
 * </pre>
 * 
 * 另一种是在代码级别实现, 可以对 {@link HttpServletResponse} 进行包装, 截取所有的输出,
 * 等到过滤器链处理完毕之后, 再对截获的输出进行处理, 并写入到真正的 {@link HttpServletResponse}
 * 中去.
 * <p>
 * 效果可以从返回内容的 Header 中查看, 主要看 Content-Length 和 Content-Encoding. web.xml 配置如下:</p>
 * 
 * <pre>
 * {@code
 * <filter>
 *     <filter-name>gzipFilter</filter-name>
 *     <filter-class>org.mysterylab.utopiaframewok.web.filter.GzipFilter</filter-class>
 *     <init-param>
 *         <param-name>enabled</param-name>
 *         <param-value>true</param-value>
 *     </init-param>
 *     <init-param>
 *         <param-name>bufferSize</param-name>
 *         <param-value>10240</param-value>
 *     </init-param>
 * </filter>
 * 
 * <filter-mapping>
 *     <filter-name>gzipFilter</filter-name>
 *     <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * }
 * </pre>
 * 
 * 所以当 Web 服务器已提供该功能的时候, 可以将该过滤器关闭.
 * 
 * @author zhouych
 */
public class GzipFilter implements Filter {
	
	/**
	 * 是否开启, 默认为启用, 用于开发阶段
	 */
	private boolean isEnabled = true;
	
	/**
	 * 默认的缓冲区大小, 由开发者根据当前页面的内容大小来在 web.xml 中定义
	 */
	private int bufferSize = 10240;

	@Override
	public void init(FilterConfig config) throws ServletException {
		
		String enabled = config.getInitParameter("enabled");
		if (StringUtils.isNotBlank(enabled)) {
			isEnabled = Boolean.parseBoolean(enabled);
		}
		
		if (!isEnabled) {
			return;
		}
		
		// 根据 web.xml 初始化缓冲区大小
		String bufferSizeStr = config.getInitParameter("bufferSize");
		if (StringUtils.isNotBlank(bufferSizeStr)) {
			this.bufferSize = Integer.valueOf(bufferSizeStr).intValue();
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
		
		// 判断当前浏览器是否支持 Gzip 解析
		if (isGzipSupport(httpRequest)) {
			ByteHttpServletResponseWrapper responseWrapper =
				new ByteHttpServletResponseWrapper(httpResponse);
			
			// 继续传递处理
			chain.doFilter(request, responseWrapper);
			
			responseWrapper.flushBuffer();
			
			// 压缩数据
			byte[] gzipData = CompressUtil.gzipCompress(responseWrapper.getResponseData(),
					new ByteArrayOutputStream(bufferSize));
			httpResponse.addHeader("Content-Encoding", "gzip");
			httpResponse.setContentLength(gzipData.length);
			ServletOutputStream out = response.getOutputStream();
			out.write(gzipData);
			out.flush();
		} else {
			// 继续传递处理
			chain.doFilter(request, response);
		}
	}
	
	/**
	 * 判断客户端的浏览器是否支持 Gzip 编码. 现如今一般浏览器都支持 gzip 功能,
	 * 并会在请求信息中表示如下:
	 * <pre>
	 * <b>Accept-Encoding</b> gzip,deflate
	 * </pre>
	 * 
	 * @param request
	 * @return
	 */
	private boolean isGzipSupport(HttpServletRequest request) {
		String encoding = request.getHeader("Accept-Encoding");
		// 如果浏览器没有发送, 则不能进行压缩, 以防出错
		if (StringUtils.isBlank(encoding)) {
			return false;
		}
		return (encoding.indexOf("gzip")!= -1);
	}
}