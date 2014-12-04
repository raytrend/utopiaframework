/*
 * CustomMySQL5Dialect.java
 * 
 * Created on 26/11/2011
 */
package cn.raytrend.utopiaframework.core.orm.hibernate3.dialect;

import org.hibernate.dialect.MySQL5Dialect;

import cn.raytrend.utopiaframework.core.orm.hibernate3.dialect.function.BitAndFunction;

/**
 * 自定义的支持 MySQL 5 的方言, 在 Hibernate 提供的 {@link MySQL5Dialect} 基础上继承.
 * 比如, 一个常见的用法是在 Hibernate 的配置文件 applicationContext-hibernate.xml 里使用该方言, 如:
 * {@code
 *  <prop key="hibernate.dialect">org.mysterylab.utopiaframework.core.orm.hibernate3.dialect.CustomMySQL5Dialect</prop>
 * }
 * 
 * @author zhouych
 */
public class CustomMySQL5Dialect extends MySQL5Dialect {

	public CustomMySQL5Dialect() {
		
		super();
		
		// 注册 & 运算函数, 之后 bitand(a, b) 即可实现 a & b 的效果
		registerFunction("bitand", new BitAndFunction());
	}
}
