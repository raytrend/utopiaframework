/*
 * HSQLBitAndFunction.java
 * 
 * Createed on 26/11/2011
 */
package cn.raytrend.utopiaframework.core.orm.hibernate3.dialect.function;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.Type;

/**
 * HSQL DB 的 <code>&</code> 运算比较特殊, 它是采用其内部提供的 <code>BITAND()</code> 函数来完成该功能, 所以需要特殊处理.
 * 
 * @author zhouych
 */
public class HSQLBitAndFunction extends BitAndFunction {

	@SuppressWarnings("unchecked")
	@Override
	public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory)
			throws QueryException {

		StringBuffer sb = new StringBuffer();
		sb.append("BITAND(");
		sb.append(arguments.get(0).toString());
		sb.append(",");
		sb.append(arguments.get(1).toString());
		sb.append(")");
		return sb.toString();
	}
}
