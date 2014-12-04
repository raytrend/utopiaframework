/*
 * CustomUUIDHexGenerator.java
 * 
 * Created on 26/11/2011
 */
package cn.raytrend.utopiaframework.core.orm.hibernate3.id.support;

import java.io.Serializable;

import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.UUIDHexGenerator;

/**
 * 继承 Hibernate 的 {@link UUIDHexGenerator}, 将 UUID 的长度缩短到 16 位.
 * UUID 主要用于多数据源下的情形, 分布式中不能使用数据库自增主键的策略. 使用策略为:
 * <ul>
 *     <li>1) AppId - 使用 2 位的自定义的值, 代替原算法中的 IP(8位) + 同一 IP 上的 JVM(8位)</li>
 *     <li>2) Timestamp - 沿用原算法.</li>
 *     <li>3) Count - 使用相同 JVM 同一毫秒的计数器, 长度变为 2.</li>
 * </ul>
 * 全部沿用 Hex 编码, AppId(2) + Timestamp(12) + Count(2) = UUID(16)
 * 
 * @author zhouych
 */
public class CustomUUIDHexGenerator extends UUIDHexGenerator {

	@Override
	public Serializable generate(SessionImplementor session, Object obj) {

		// 仿照 UUIDHexGenerator 的实现方式
		return new StringBuilder(16)
				.append(format(getAppId()))
				.append(format(getHiTime()))
				.append(format(getLoTime()))
				.append(formatShort(getCount()))
				.toString();
	}
	
	protected short getAppId() {
		return 0;
	}
	
	/**
	 * 将最大值为 255 的数值的长度格式化为长度 2 的字符串.
	 * 
	 * @param value
	 * @return
	 */
	protected String formatShort(short value) {
		String format = Integer.toHexString(value);
		if (format.length() < 2) {
			format = "0" + format;
		}
		return format;
	}
}
