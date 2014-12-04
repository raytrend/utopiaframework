/*
 * IncrementIdEntity.java
 * 
 * Created on 26/11/2011
 */
package cn.raytrend.utopiaframework.core.orm.hibernate3.id;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.id.IncrementGenerator;
import org.hibernate.id.factory.DefaultIdentifierGeneratorFactory;

/**
 * 统一定义 Domain/Entity 类的 id 的基类, 该基类使用了自增类型的主键生成方案. 在 {@link DefaultIdentifierGeneratorFactory}
 * 中注册了多种 strategy, 其中 <code>increment</code> 对应了 {@link IncrementGenerator} 的生成策略.
 * <p>
 * 注意到这里将注解写在属性上, 其子类也必须如此 (也即不能写到方法上), 否则会报错.
 * </p>
 * 比如, 一个常见的用法是:
 * {@code
 *  public class User extends IncrementIdEntity<Integer> {
 *      @Column(name = "username", nullable = false)
 *      private String username;
 *      ......
 *  }
 * }
 * 
 * @author zhouych
 * @param <T>
 *            主键的类型, 一般是 Integer 或 Long 类型
 * @see IncrementGenerator
 * @see DefaultIdentifierGeneratorFactory
 */
@SuppressWarnings("serial")
@MappedSuperclass
public class IncrementIdEntity<T> implements Serializable {

    /**
     * 使用数据库底层自定义的自增策略
     */
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected T id;

	public T getId() {
		return id;
	}

	/**
	 * 为了安全, 不允许外部程序访问该方法来设置对象的 OID.
	 * 
	 * @param id
	 */
	@SuppressWarnings("unused")
	private void setId(T id) {
		this.id = id;
	}
}
