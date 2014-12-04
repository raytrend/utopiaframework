/*
 * JsonBinder.java
 * 
 * Created on 02/12/2011
 */
package cn.raytrend.utopiaframework.core.util.json;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 利用 Jackson 中的 {@link ObjectMapper} 类来让 java 对象和 json 字符串实现互相转换. 在某些场合下将 java 对象转换为 json
 * 字符串有很多好处, 比如往缓存中存放数据. 可以看作是对 Jackson 的简单封装, 也可以通过 {@link #getObjectMapper()}
 * 方法获得<code>objectMapper</code> 对象来完成其他操作.
 * <p>
 * 为了获得更好的效率, 采用了 singleton 模式, jackson 对此是支持的.
 * </p>
 * 
 * @author zhouych
 */
public class JsonBinder {

	private static Logger logger = LoggerFactory.getLogger(JsonBinder.class);
	
	private static ObjectMapper objectMapper = new ObjectMapper();
	
	private static JsonBinder jsonBinder;
	
	private JsonBinder(Inclusion inclusion) {
		// serializing 设置成 Inclusion.NON_DEFAULT 来提高性能
		objectMapper.getSerializationConfig().withSerializationInclusion(inclusion);
	}
	
	/**
	 * 允许修改 {@link Inclusion}, 默认是 {@link Inclusion#NON_DEFAULT}, 也即对于未修改的属性不用进行 json 转换.
	 * 
	 * @param inclusion
	 * @see #getInstance()
	 */
	public static JsonBinder getInstance(Inclusion inclusion) {
		if (jsonBinder == null) {
			jsonBinder = new JsonBinder(inclusion);
		}
		return jsonBinder;
	}
	
	public static JsonBinder getInstance() {
		if (jsonBinder == null) {
			jsonBinder = new JsonBinder(Inclusion.NON_DEFAULT);
		}
		return jsonBinder;
	}
	
	/**
	 * 从 json 字符串生成相应的 java 对象.
	 * <ul>
	 *     <li>1) 如果 JSON 字符串为 NULL 或 "null", 则返回 null.</li>
	 *     <li>2) 如果 JSON 字符串为 "[]", 则返回空集合.</li>
	 * </ul>
	 * 
	 * @param <T>
	 * @param jsonString
	 * @param clazz
	 * @return
	 */
	public <T> T fromJson(String jsonString, Class<T> clazz) {
		if (StringUtils.isEmpty(jsonString)) {
			return null;
		}
		try {
			return objectMapper.readValue(jsonString, clazz);
		} catch (IOException e) {
			logger.warn("parse json string error: " + jsonString, e);
			return null;
		}
	}
	
	/**
	 * 将 java 对象生成 json 格式.
	 * <ul>
	 *     <li>1) 如果对象为 null, 则返回 "null".</li>
	 *     <li>2) 如果对象为空集合, 则返回 "[]".</li>
	 * </ul>
	 * 
	 * @param object
	 * @return
	 */
	public String toJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (IOException e) {
			logger.warn("write a json string error: " + object, e);
			return null;
		}
	}
	
	/**
	 * 获取 <code>objectMapper</code> 对象, 完成其他该 binder 未封装的功能.
	 * 
	 * @return
	 */
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}
