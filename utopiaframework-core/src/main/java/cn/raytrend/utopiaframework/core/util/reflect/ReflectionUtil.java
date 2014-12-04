/*
 * ReflectionUtil.java
 * 
 * Created on 26/11/2011
 */
package cn.raytrend.utopiaframework.core.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 反射的工具类, 主要提供访问私有变量并获取泛型的类, 提取集合中元素的属性等.
 * 
 * @author zhouych
 */
public class ReflectionUtil {

	private static final Logger logger = LoggerFactory.getLogger(ReflectionUtil.class);
	
	/**
	 * 不断地向上寻找其父类, 获取到对象的 DeclaredField, 并强制设置为可访问, 如果一直到 {@link Object} 仍无法找到，则返回 null.
	 * 
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static Field getAccessibleField(final Object obj, final String fieldName) {
		
		for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				Field field = superClass.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException e) {
				// Field 不在当前类的定义当中，继续向父类推进
			}
		}
		return null;
	}
	
	/**
	 * 不断地向上寻找其父类, 获取到对象的 DeclaredMethod, 并强制设置为可访问, 如果一直到 {@link Object} 仍无法找到, 则返回
	 * null. 这里主要用于方法需要被多次调用的情形, 先使用本函数取得 Method, 再调用 Method.invoke(Object obj, Object... args).
	 * 
	 * @param obj
	 * @param methodName
	 * @param parameterTypes
	 * @return
	 */
	public static Method getAccessibleMethod(final Object obj, final String methodName,
			final Class<?>... parameterTypes) {
	
		for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				Method method = superClass.getDeclaredMethod(methodName, parameterTypes);
				method.setAccessible(true);
				return method;
			} catch (NoSuchMethodException e) {
				// Method 不在当前类的定义当中，继续向父类推进
			}
		}
		return null;
	}
	
	/**
	 * 直接调用对象的方法, 无视其 private/protected 的修饰符.
	 * 
	 * @param obj
	 * @param methodName
	 * @param parameterTypes
	 * @param Object
	 * @return
	 */
	public static Object invokeMethod(final Object obj, final String methodName, final Class<?>[] parameterTypes,
			final Object[] args) {
		
		Method method = getAccessibleMethod(obj, methodName, parameterTypes);
		if (method == null) {
			throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + obj + "]");
		}
		try {
			return method.invoke(obj, args);
		} catch (Exception e) {
			throw new RuntimeException("Unexpected Checked Exception.", e);
		}
	}
	
	/**
	 * 调用 getter 方法, 风格保持 getXXX().
	 * 
	 * @param obj
	 * @param propertyName
	 * @return
	 */
	public static Object invokeGetterMethod(Object obj, String propertyName) {
		String getterMethodName = "get" + StringUtils.capitalize(propertyName);
		return invokeMethod(obj, getterMethodName, new Class[] {}, new Object[] {});
	}
	
	/**
	 * 调用 setter 方法, 使用 value 的类来查找 setter 方法.
	 * 
	 * @param obj
	 * @param propertyName
	 * @param value
	 */
	public static void invokeSetterMethod(Object obj, String propertyName, Object value) {
		invokeSetterMethod(obj, propertyName, value, null);
	}
	
	/**
	 * 调用 setter 方法, 风格保持 setXXX().
	 * @param obj
	 * @param propertyName
	 * @param value
	 * @param propertyType
	 */
	public static void invokeSetterMethod(Object obj, String propertyName, Object value, Class<?> propertyType) {
		Class<?> type = propertyType != null ? propertyType : value.getClass();
		String setterMethodName = "set" + StringUtils.capitalize(propertyName);
		invokeMethod(obj, setterMethodName, new Class[] {type}, new Object[] {value});
	}
	
	/**
	 * 直接读取对象的属性值, 不经过 getter 函数且无视其 private/protected 修饰符.
	 * 
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static Object getFieldValue(final Object obj, final String fieldName) {
		
		Field field = getAccessibleField(obj, fieldName);
		if (field == null) {
			throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
		}
		try {
			return field.get(obj);
		} catch (IllegalAccessException e) {
			logger.error("Exception could not be happended.", e.getMessage());
		}
		return null;
	}
	
	/**
	 * 直接设置对象的属性值, 不经过 setter 函数且无视其 private/protected 修饰符.
	 * 
	 * @param obj
	 * @param fieldName
	 * @param value
	 */
	public static void setFieldValue(final Object obj, final String fieldName, final Object value) {
		
		Field field = getAccessibleField(obj, fieldName);
		if (field == null) {
			throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
		}
		try {
			field.set(obj, value);
		} catch (IllegalAccessException e) {
			logger.error("Exception could not be happended.", e.getMessage());
		}
	}
	
	/**
	 * 通过反射获得 clazz 的最顶级的父类.
	 * 
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getSuperClassGenericType(final Class clazz) {
		return getSuperClassGenericType(clazz, 0);
	}
	
	/**
	 * 通过反射, 获得 {@link Class} 定义中声明的父类的泛型参数的类型，如果无法找到, 则返回 Object.class. 比如:
	 * <pre>
	 * {@code
	 * public UserDao extends HibernateDao<User, Long>
	 * }
	 * </pre>
	 * 
	 * @param clazz
	 * @param index
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Class getSuperClassGenericType(final Class clazz, final int index) {
		
		Type genType = clazz.getGenericSuperclass();
		
		if (!(genType instanceof ParameterizedType)) {
			logger.warn("{}'s superclass not ParameterizedType.", clazz.getSimpleName());
			return Object.class;
		}
		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		
		if (index >= params.length || index < 0) {
			logger.warn("Index: " + index + ", size of " + clazz.getSimpleName() + "'s ParameterizedType: "
					+ params.length);
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			logger.warn(clazz.getSimpleName() + " cannot set the actual class on superclass generic parameter.");
			return Object.class;
		}
		
		return (Class) params[index];
	}
}
