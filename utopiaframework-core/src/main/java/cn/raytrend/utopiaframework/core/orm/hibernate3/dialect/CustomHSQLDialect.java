/*
 * CustomHSQLDialect.java
 * 
 * Created on 26/11/2011
 */
package cn.raytrend.utopiaframework.core.orm.hibernate3.dialect;

import org.hibernate.dialect.HSQLDialect;

import cn.raytrend.utopiaframework.core.orm.hibernate3.dialect.function.HSQLBitAndFunction;

/**
 * 自定义的支持 HSQL 的方言, 在 Hibernate 提供的 {@link HSQLDialect} 基础上继承.
 * 
 * @author zhouych
 */
public class CustomHSQLDialect extends HSQLDialect {

	public CustomHSQLDialect() {
		
		super();
		
		// 注册 & 运算函数, 之后 bitand(a, b) 即可实现 BITAND(a,b) 的效果
		registerFunction("bitand", new HSQLBitAndFunction());
	}
}
