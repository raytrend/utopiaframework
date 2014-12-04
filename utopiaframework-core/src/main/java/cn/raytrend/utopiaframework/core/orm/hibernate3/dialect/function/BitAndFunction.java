/*
 * BitAndFunction.java
 * 
 * Created on 26/11/2011
 */
package cn.raytrend.utopiaframework.core.orm.hibernate3.dialect.function;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.Mapping;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.IntegerType;
import org.hibernate.type.Type;

/**
 * 为数据库添加 & 运算. 目前很多数据库已经支持 & 操作, 比如 MySQL 中的 <code>a & b</code>. 这里扩展 Hibernate 来支持 & 操作,
 * 之后 Hibernate 会自动将 <code>bitand(a, b)</code> 翻译成 <code>a & b</code>.
 * 
 * @author zhouych
 */
public class BitAndFunction implements SQLFunction {

	
	@Override
	public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
		// 返回的 SQL 类型
		return IntegerType.INSTANCE;
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	@Override
	public boolean hasParenthesesIfNoArguments() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory)
			throws QueryException {
		
		// & 运算需要两个参数, a & b
		if (arguments.size() != 2) {
			throw new IllegalArgumentException("BitAndFunction needs 2 arguments");
		}
		return arguments.get(0).toString() + " & " + arguments.get(1).toString();
	}
}
