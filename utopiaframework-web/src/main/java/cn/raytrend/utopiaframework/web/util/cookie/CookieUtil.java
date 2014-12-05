/*
 * CookieUtil.java
 * 
 * Created on 05/03/2012
 */
package cn.raytrend.utopiaframework.web.util.cookie;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 读写客户端 cookie 的工具类.
 * 
 * @author zhouych
 */
public class CookieUtil {
	
	/**
	 * 注意, 如果要操作同一个 cookie, 除了名称一致外, 还需要保持 path 一致
	 */
	public static final String DEFAULT_COOKIE_PATH = "/";

	/**
	 * 往客户端添加一个 cookie.
	 * 
	 * @param response
	 *            服务端响应
	 * @param cookieName
	 *            cookie 名称
	 * @param cookieValue
	 *            cookie 值
	 * @param cookieMaxAage
	 *            cookie 的最大过期时间, 设置为 -1 表示随着浏览器关闭而删除
	 */
	public static void addCookie(HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxAage) {
		
		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setPath(DEFAULT_COOKIE_PATH);
		if (cookieMaxAage > 0) {
			cookie.setMaxAge(cookieMaxAage);
		}
		response.addCookie(cookie);
	}
	
	/**
	 * 修改 cookie 的值.
	 * 
	 * @param request
	 * @param response
	 * @param cookieName
	 *            被修改的 cookie 的名称
	 * @param newCookieValue
	 *            新的 cookie 值
	 * @return 是否修改成功, 成功则返回 true, 否则返回 false
	 */
	public static boolean editCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
			String newCookieValue) {
		
		Cookie cookie = readCookie(request, cookieName);
		if (cookie != null) {
			cookie.setPath(DEFAULT_COOKIE_PATH);
			cookie.setValue(newCookieValue);
			response.addCookie(cookie);
			return true;
		}
		return false;
	}
	
	/**
	 * 清空所有Cookie信息
	 * @param req 请求信息
	 * @param resp 响应信息
	 */
	public static void killAllCookies(HttpServletRequest req,HttpServletResponse resp){
		Cookie[] cookies = req.getCookies();
		if(cookies == null) return;
		for(Cookie cookie : cookies){
			cookie.setMaxAge(0);
			cookie.setPath(DEFAULT_COOKIE_PATH);
			cookie.setValue("");
			resp.addCookie(cookie);
		}
	}
	
	/**
	 * 删除客户端的 cookie.
	 * 
	 * @param request
	 * @param response
	 * @param cookieName
	 *            被删除的 cookie 的名称
	 * @return
	 */
	public static boolean deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
		
		Cookie cookie = readCookie(request, cookieName);
		if (cookie != null) {
			cookie.setPath(DEFAULT_COOKIE_PATH);
			cookie.setMaxAge(0);
			cookie.setValue("");
			response.addCookie(cookie);
			return true;
		}
		return false;
	}
	
	/**
	 * 从客户端读取一个 cookie, 读取失败则返回 null.
	 * 
	 * @param request
	 *            客户端请求
	 * @param cookieName
	 *            cookie 名称
	 * @return
	 */
	public static Cookie readCookie(HttpServletRequest request, String cookieName) {
		
		Map<String, Cookie> cookieMap = setCookiesToMap(request);
		if (cookieMap.containsKey(cookieName)) {
			return cookieMap.get(cookieName);
		}
		return null;
	}
	
	/**
	 * 将客户端的所有的 cookie 封装到一个 Map 当中.
	 * 
	 * @param request
	 * @return
	 */
	private static Map<String, Cookie> setCookiesToMap(HttpServletRequest request) {
		
		Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				cookieMap.put(cookie.getName(), cookie);
			}
		}
		return cookieMap;
	}
}
