/*
 * ConverUtil.java
 * 
 * Created on 25/11/2011
 */
package cn.raytrend.utopiaframework.core.util.reflect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * 转换的工具类.
 * 
 * @author zhouych
 */
public class ConvertUtil {
	
	static {
		registerDateConverter();
	}

	/**
	 * 通过 getter 函数提取集合中的对象的属性, 并组合成 List.
	 * 
	 * @param collection
	 *            来源集合
	 * @param propertyName
	 *            要提取的属性名
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List convertElementPropertyToList(final Collection collection, final String propertyName) {
		
		List list = new ArrayList();
		try {
			for (Object obj : collection) {
				list.add(PropertyUtils.getProperty(obj, propertyName));
			}
		} catch (Exception e) {
			throw new RuntimeException("Unexpected Checked Exception.", e);
		}
		return list;
	}

	/**
	 * 通过 getter 函数提取集合中的对象的属性, 并组合成由分隔符分割的字符串.
	 * 
	 * @param collection
	 *            来源集合
	 * @param propertyName
	 *            要提取的属性名
	 * @param separator
	 *            分隔符
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String convertElementPropertyToString(final Collection collection, final String propertyName,
			final String separator) {
		List list = convertElementPropertyToList(collection, propertyName);
		return StringUtils.join(list, separator);
	}

	/**
	 * 转换字符串到相应类型.
	 * 
	 * @param value
	 *            待转换的字符串
	 * @param toType
	 *            转换的目标类型
	 * @return
	 */
	public static Object convertStringToObject(String value, Class<?> toType) {
		try {
			return ConvertUtils.convert(value, toType);
		} catch (Exception e) {
			throw new RuntimeException("Unexpected Checked Exception.", e);
		}
	}

	/**
	 * 定义日期 Converter 的格式为：yyyy-MM-dd 或者 yyyy-MM-dd HH:mm:ss
	 */
	private static void registerDateConverter() {
		
		DateConverter dc = new DateConverter();
		dc.setUseLocaleFormat(true);
		dc.setPatterns(new String[] {
				"yyyy-MM-dd",
				"yyyy-MM-dd HH:mm:ss"
		});
		ConvertUtils.register(dc, Date.class);
	}
}
