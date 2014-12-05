/*
 * PerformanceInterceptor.java
 * 
 * Created on 29/11/2011
 */
package cn.raytrend.utopiaframework.web.servlet.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import cn.raytrend.utopiaframework.web.util.HttpUtil;

/**
 * 性能拦截器, 在每次请求的时候对其进行拦截, 看该次请求花费了多少时间, 方便下一步的测试优化.
 * 
 * @author zhouych
 */
public class PerformanceInterceptor extends HandlerInterceptorAdapter {
	
	protected Logger logger = LoggerFactory.getLogger(PerformanceInterceptor.class);
	
	private static final String TIME_START = "PERF_START";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		request.setAttribute(TIME_START, System.currentTimeMillis());
		return true;
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

		// 该次请求结束之后, 记录其总的请求时间
		Long startTime = (Long) request.getAttribute(TIME_START);
		if (startTime != null) {
			long time = System.currentTimeMillis() - startTime.longValue();
			String uri = HttpUtil.getRequestURI(request);
			logger.info("[logger] - URL({}) -> Time({}ms.)", uri, time);
		}
	}
}
