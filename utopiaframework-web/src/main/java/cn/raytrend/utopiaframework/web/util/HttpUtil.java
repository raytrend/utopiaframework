/*
 * HttpUtil.java
 * 
 * Created on 29/11/2011
 */
package cn.raytrend.utopiaframework.web.util;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import cn.raytrend.utopiaframework.core.util.codec.EncoderUtil;

/**
 * HTTP 的工具类, 主要封装了 HTTP 中的 Header 等信息.
 * 
 * @author zhouych
 */
public class HttpUtil {

	//-- Header 的返回类型 --//
	
	public static final String HEADER_TYPE_TEXT	= "text/plain";
	
	public static final String HEADER_TYPE_JSON	= "application/json";
	
	public static final String HEADER_TYPE_XML	= "text/xml";
	
	public static final String HEADER_TYPE_HTML	= "text/html";
	
	public static final String HEADER_TYPE_JS	= "text/javascript";
	
	public static final String HEADER_TYPE_EXCEL= "application/vnd.ms-excel";

	/**
	 * 设置客户端缓存过期时间 的 Header.
	 * 
	 * @param response
	 *            服务端响应
	 * @param expiresSeconds
	 *            过期时间, 从当前时间算起, 以秒为单位
	 */
	public static void setExpiresHeader(HttpServletResponse response, long expiresTimeSeconds) {
		
		// 设置缓存过期时间
		response.setDateHeader("Expires", System.currentTimeMillis() + expiresTimeSeconds * 1000);
		// Cache-Control 是用来弥补 Expires 标记的不足, 防止服务端和客户端机器的时间不同 (HTTP/1.1 新增)
		response.setHeader("Cache-Control", "max-age=" + expiresTimeSeconds);
	}
	
	/**
	 * 设置禁止客户端缓存的 Header.
	 * 
	 * @param response
	 *            服务端响应
	 */
	public static void setDisableCacheHeader(HttpServletResponse response) {

		response.setDateHeader("Expires", 1L);
		response.addHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, no-store, max-age=0");
	}

	/**
	 * 设置 LastModified Header.
	 * 
	 * @param response
	 *            服务端响应
	 * @param lastModifiedDate
	 *            最后修改时间
	 */
	public static void setLastModifiedHeader(HttpServletResponse response, long lastModifiedDate) {
		response.setDateHeader("Last-Modified", lastModifiedDate);
	}

	/**
	 * 设置 Etag Header.
	 * 
	 * @param response
	 *            服务端响应
	 * @param etag
	 *            ETag 值
	 */
	public static void setEtagHeader(HttpServletResponse response, String etag) {
		response.setHeader("ETag", etag);
	}
	
	/**
	 * 判断本次 http 请求是否 ajax 请求, 在很多情况下 ajax 请求要另外处理, 比如返回一个错误状态码.
	 * 本方法在 ie-6+, firefox, chrome 下测试通过.
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isAjaxRequest(HttpServletRequest request) {
		if (StringUtils.isBlank(request.getHeader("X-Requested-With"))) {
			return false;
		}
		return request.getHeader("X-Requested-With").equals("XMLHttpRequest");
	}
	
	/**
	 * 根据浏览器 If-Modified-Since Header, 计算文件是否已被修改. 如果无修改, checkIfModify 返回 false, 设置
	 * 304 not modify status.
	 * 
	 * @param lastModified
	 *            内容的最后修改时间
	 */
	public static boolean checkIfModifiedSinceHeader(HttpServletRequest request, HttpServletResponse response,
			long lastModified) {
		
		long ifModifiedSince = request.getDateHeader("If-Modified-Since");
		if ((ifModifiedSince != -1) && (lastModified < ifModifiedSince + 1000)) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return false;
		}
		return true;
	}
	
	/**
	 * 根据浏览器 If-None-Match Header, 计算 Etag 是否已无效. 如果 Etag 有效, checkIfNoneMatch 返回 false, 设置
	 * 304 not modify status.
	 * 
	 * @param etag
	 *            ETag 值
	 */
	public static boolean checkIfNoneMatchHeader(HttpServletRequest request, HttpServletResponse response, String etag) {
		
		String previousValue = request.getHeader("If-None-Match");
		
		if (previousValue != null && previousValue.equals(etag)) {
			// 如果客户端返回的 ETag 与当前服务器响应的 ETag 一致, 说明页面没有改动, 返回 "304 Not Modified"
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			// 再把最后修改时间 Last-Modified 设置成 If-Modified-Since
			response.setHeader("Last-Modified", request.getHeader("If-Modified-Since"));
			return false;
		}
		// 如果已经修改了, 则重新发送内容并设置新的 Last-Modified
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		Date lastModifiedDate = calendar.getTime();
		response.setDateHeader("Last-Modified", lastModifiedDate.getTime());
		return true;
	}
	
	/**
	 * 设置让浏览器弹出下载对话框的 Header.
	 * 
	 * @param fileName
	 *            下载后的文件名
	 */
	public static void setFileDownloadHeader(HttpServletResponse response, String fileName) {
		try {
			// 中文文件名支持
			String encodedfileName = new String(fileName.getBytes(), "ISO8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedfileName + "\"");
		} catch (UnsupportedEncodingException e) {
			// 未处理
		}
	}

	/**
	 * 取得带相同前缀的 Request Parameters. 返回的结果的 Parameter 名已去除前缀.
	 * 
	 * @param request
	 *            客户端请求
	 * @param prefix
	 *            参数前缀
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getParametersStartsWith(ServletRequest request, String prefix) {
		
		// 获取参数集, 形如: [method, productid, pageid] 等
		Enumeration<String> paramNames = request.getParameterNames();
		Map<String, Object> params = new TreeMap<String, Object>();
		if (prefix == null) {
			prefix = "";
		}
		while (paramNames != null && paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			if ("".equals(prefix) || paramName.startsWith(prefix)) {
				String unprefixed = paramName.substring(prefix.length());
				String[] values = request.getParameterValues(paramName);
				if (values == null || values.length == 0) {
					// do nothing
				} else if (values.length > 1) {
					params.put(unprefixed, values);
				} else {
					params.put(unprefixed, values[0]);
				}
			}
		}
		return params;
	}

	/**
	 * 对 Http Basic 验证的 Header 进行编码. 这样可以增加安全性 (不推荐使用 Basic 的认证).
	 * 
	 * @param userName
	 *            用户名
	 * @param password
	 *            密码
	 * @return
	 */
	public static String encodeHttpBasic(String userName, String password) {
		String encode = userName + ":" + password;
		return "Basic " + EncoderUtil.base64Encode(encode.getBytes());
	}
	
	/**
	 * 获取某次请求的不完整的 url, 比如: /index.do?method=xxx&productId=yyy.
	 * 
	 * @param request
	 * @return
	 */
	public static String getRequestURI(HttpServletRequest request) {
		
		String uri = request.getRequestURI();
		String query = request.getQueryString();
		if (StringUtils.isBlank(query)) {
			return uri;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(uri);
		sb.append("?");
		sb.append(query);
		
		return sb.toString();
	}
	
	/**
	 * 获取某次请求的完整的 url, 比如: http://www.mysterylab.org/index.do?method=x&pId=y.
	 * 
	 * @param request
	 * @return
	 */
	public static String getRequestURL(HttpServletRequest request) {
		
		StringBuffer sb = request.getRequestURL();
		String query = request.getQueryString();
		if (StringUtils.isNotBlank(query)) {
			sb.append("?");
			sb.append(query);
		}
		
		return sb.toString();
	}
	
	/**
     * 获得 web 应用的全局 url 地址, 比如返回 http://host:port/contextPath.
     * 这里注意到当部署到 ROOT 下的时候 contextPath 为空. 这里需要符合两种情况.
     * 
     * @return
     */
    public static String getWebAppURL(HttpServletRequest request) {
        return StringUtils.substringBefore(request.getRequestURL().toString(), request.getRequestURI());
    }
}
