/*
 * CustomUUIDHexIdEntity.java
 * 
 * Created on 26/11/2011
 */
package cn.raytrend.utopiaframework.core.orm.hibernate3.id;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

/**
 * 统一定义 Domain/Entity 类的 id 的基类. 该基类定义了如下内容:
 * <ul>
 *     <li>1) id 的属性名.</li>
 *     <li>2) 数据类型.</li>
 *     <li>3) 列名映射.</li>
 *     <li>4) 生成策略.</li>
 * </ul>
 * 注意到这里将注解写在属性上, 其子类也必须如此 (也即不能写到方法上), 否则会报错.
 * 
 * @author zhouych
 * @see IncrementIdEntity
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class CustomUUIDHexIdEntity implements Serializable {
	
	/**
	 * 主键 id
	 */
	@Id
	@Column(name = "ID")
	@GenericGenerator(name = "CustomUUIDHexGenerator",
			strategy = "org.mysterylab.utopiaframework.core.orm.hibernate3.id.support.CustomUUIDHexGenerator")
	@GeneratedValue(generator = "CustomUUIDHexGenerator")
	protected String id;

	public String getId() {
		return id;
	}

	/**
	 * 为了安全, 不允许外部程序访问该方法来设置对象的 OID.
	 * 
	 * @param id
	 */
	@SuppressWarnings("unused")
	private void setId(String id) {
		this.id = id;
	}
}
