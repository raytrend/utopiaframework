/*
 * PropertyFilter.java
 * 
 * Created on 27/11/2011
 */
package cn.raytrend.utopiaframework.core.orm;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Restrictions;

import cn.raytrend.utopiaframework.core.util.reflect.ConvertUtil;

/**
 * 与具体的 ORM 实现无关的属性过滤条件封装类, 主要记录页面中简单的搜索过滤条件, 其最大的好处是可以同时提供多个过滤条件.
 * 
 * @author zhouych
 */
public class PropertyFilter {

	/**
	 * 多个属性间 OR 关系的分隔符
	 */
	public static final String OR_SEPARATOR = "_OR_";
	
	/**
	 * 基本属性比较类型, 主要考虑到 {@link Restrictions} 的比较类型支持.
	 */
	public enum MatchType {
		EQ,		// 等于
		NE,		// 不等于
		GT,		// 大于
		GE,		// 大于或等于
		LT,		// 小于
		LE,		// 小于或等于
		LIKE;	// 字符串模式匹配
	}
	
	/**
	 * 基本属性数据类型, 主要考虑到当前数据库的支持类型.
	 */
	public enum PropertyType {
		
		B(Boolean.class),	// Boolean 类型
		I(Integer.class),	// Integer 类型
		F(Float.class),		// Float 类型
		N(Double.class),	// Double 类型
		L(Long.class),		// Long 类型
		S(String.class),	// String 类型
		D(Date.class);		// Date 类型

		private Class<?> clazz;

		private PropertyType(Class<?> clazz) {
			this.clazz = clazz;
		}

		public Class<?> getValue() {
			return clazz;
		}
	}
	
	/**
	 * 比较类型
	 */
	private MatchType matchType;
	
	/**
	 * 比较值
	 */
	private Object matchValue;
	
	/**
	 * 属性过滤类
	 */
	private Class<?> propertyClass;
	
	/**
	 * 比较属性过滤名列表
	 */
	private String[] propertyNames;
	
	/**
	 * 构造方法.
	 * 
	 * @param filterName
	 *            比较属性字符串, 含待比较的比较类型、属性值类型及属性列表. 比如: LIKE_S_NAME_OR_LOGIN_NAME
	 * @param value
	 *            待比较的值, 比如如果是 Boolean 类型可以为 "true", Date 类型可以为 "2011-07-28"
	 */
	public PropertyFilter(String filterName, String value) {
		/*
		 * 假设当前的 filterName 是 LIKE_S_NAME_OR_LOGIN_NAME, 则:
		 * 1) matchTypeStr		= LIKE
		 * 2) propertyTypeStr	= S
		 * 3) propertyNameStr	= NAME_OR_LOGIN_NAME
		 * 4) propertyNames[]	= [NAME, LOGIN_NAME]
		 */
		String matchTypeStr = StringUtils.substringBefore(filterName, "_");
		try {
			this.matchType = Enum.valueOf(MatchType.class, matchTypeStr);
		} catch (RuntimeException e) {
			throw new IllegalArgumentException("PropertyFilter - " + filterName
					+ " does not write as rule, so can not get the property match type." + e);
		}
		String excludeMatchTypeStr = StringUtils.substringAfter(filterName, "_");
		String propertyTypeStr = StringUtils.substringBefore(excludeMatchTypeStr, "_");
		try {
			this.propertyClass = Enum.valueOf(PropertyType.class, propertyTypeStr).getValue();
		} catch (RuntimeException e) {
			throw new IllegalArgumentException("PropertyFilter - " + filterName
					+ " does not write as rule, so can not get the property value type." + e);
		}
		String propertyNameStr = StringUtils.substringAfter(excludeMatchTypeStr, "_");
		this.propertyNames = StringUtils.splitByWholeSeparator(propertyNameStr, PropertyFilter.OR_SEPARATOR);
		this.matchValue = ConvertUtil.convertStringToObject(value, this.propertyClass);
	}
	
	/**
	 * 获取唯一的比较属性名称.
	 * 
	 * @return
	 */
	public String getPropertyName() {
		// 确保属性的长度为 1, 这样才是唯一比较属性
		if (propertyNames.length != 1) {
			throw new RuntimeException("there are not only one property in this property filter");
		}
		return propertyNames[0];
	}
	
	/**
	 * 是否比较多个属性.
	 * 
	 * @return
	 */
	public boolean hasMultiProperties() {
		return (propertyNames.length > 1);
	}

	public MatchType getMatchType() {
		return matchType;
	}

	public Object getMatchValue() {
		return matchValue;
	}

	public Class<?> getPropertyClass() {
		return propertyClass;
	}

	public String[] getPropertyNames() {
		return propertyNames;
	}
}
