/*
 * BaseHibernateDao.java
 * 
 * Created on 26/11/2011
 */
package cn.raytrend.utopiaframework.core.orm.hibernate3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.NonUniqueResultException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.transform.ResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.raytrend.utopiaframework.core.util.reflect.ReflectionUtil;

/**
 * 封装了 Hibernate3 原生 API 的 DAO 泛型基类. 通过 Hibernate 来操纵对象, 主要是 {@link Session} 的一些方法的二次封装和 HQL
 * 与 QBC 的一些简单检索. 请注意到这里的方法几乎都是基于事务下的, 如果为了性能要使用非事务的方法, 可以使用
 * {@link #getSession(false)} 来获取到 <code>session</code> 处理并手动将其 <code>close()</code>.
 * 
 * @param <T>
 *            DAO 操作的对象类型
 * @param <PK>
 *            主键类型
 *            
 * @author zhouych
 * @see HibernateDao
 */
public class BaseHibernateDao<T, PK extends Serializable> {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 数据库的存储源, 如果一个应用只有一个数据库存储源, 那么只需创建一个该实例即可
	 */
	protected SessionFactory sessionFactory;
	
	/**
	 * 该 DAO 类操作的实体类
	 */
	protected Class<T> entityClass;
	
	/**
	 * 通过子类的泛型定义获得对象的类型. 比如:
	 * <pre>
	 * {@code
	 * public class UserDao extends BaseHibernateDao<User, Long>
	 * }
	 * </pre>
	 */
	public BaseHibernateDao() {
		this.entityClass = ReflectionUtil.getSuperClassGenericType(getClass());
	}
	
	/**
	 * 跳过 DAO 层, 直接在 Service 层使用 BaseHibernateDao 的构造函数,
	 * 在构造函数中定义对象类型.
	 * <pre>
	 * {@code
	 * BaseHibernateDao<User, Long> userDao = new BaseHibernateDao<User, Long>(sessionFactory, User.class);
	 * }
	 * </pre>
	 * @param sessionFactory
	 * @param entityClass
	 */
	public BaseHibernateDao(final SessionFactory sessionFactory, final Class<T> entityClass) {
		this.sessionFactory = sessionFactory;
		this.entityClass = entityClass;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	/**
	 * 当配置 sessionFactory 的时候采用 Spring 的自动注入而不是默认的 hibernate.cfg.xml 来创建.
	 * 一般可以在 applicationContext-hibernate.xml 文件下配置该实例, 比如代码:
	 * <pre>
	 * {@code
	 * <bean id="sessionFactory" class="org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean">
	 *     <property name="dataSource" ref="dataSource" />
	 *     <property name="packagesToScan" value="org.mysterylab.project.aircraft.entity" />
	 *     <property name="hibernateProperties">
	 *         <props>
	 *             <prop key="hibernate.connection.charSet">UTF-8</prop>
	 *             ......
	 *         </props>
	 *     </property>
	 * </bean>
	 * }
	 * </pre>
	 * 
	 * @param sessionFactory
	 *            数据库存储源
	 */
	@Resource
	public void setSessionFactory(final SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * 根据是否需要事务的环境而创建 session.
	 * 
	 * <p>
	 * session 的实例是轻量级对象, 还有, 创建 session 的时候, 并不会立即打开与数据库之间的链接, 它只在需要数据库操作时才会获取
	 * jdbc 链接, 所以创建和 session 和销毁 session 所使用的资源很少, 并不会消耗太多性能. 另外, 它是非线程安全的,
	 * 所以应该避免多个线程共享同一个 session 的情况出现.
	 * </p>
	 * 
	 * <p>
	 * 这里顺便说下  {@link SessionFactory#openSession()} 方法与 {@link SessionFactory#getCurrentSession()}的区别:
	 * 前者得到一个新的 session, 而后者则是从当前线程中得到事务开始时创建的 Transaction 的那个 session. 所以, 在使用后者的时候,
	 * 必须要加入事务. 常见的做法是在业务逻辑的 Service 层的每个操作数据库的方法中加入事务控制, 比如:
	 * <pre>
	 * //@Service
	 * public class AccountManager {
	 *     //@Resource
	 *     private UserDao userDao;
	 *     
	 *     //@Transactional(readonly = true)
	 *     public void getUser(int id) {
	 *         return userDao.get(id);
	 *     }
	 * }
	 * </pre>
	 * 以上的方法是由 Spring 来接管事务. Spring 会在该次事务结束之后将 session 关闭, 所以这里引出了一个 "Open session in view"
	 * 的问题, 该问题另外讨论. 这样一来就由 Spring 来管理 <code>session</code> 的生命周期. 如果不使用事务, 将会抛出如下异常:
	 * <code>
	 * org.hibernate.HibernateException: No Hibernate Session bound to thread, and configuration does not allow creation of non-transactional one here
	 * </code>
	 * </p>
	 * 
	 * <p>
	 * 经测试, 不使用事务的效率非常高. 业界中也有人曾经提出一个观点: 很多性能优先的应用连事务都不会有. 所以优先选用非事务方法.
	 * 但是这样一来就需要在代码中手动地对 <code>session</code> 进行 <code>close()</code>.
	 * </p>
	 * 
	 * @param isTransactional
	 *           是否需要事务
	 * @return
	 */
	public Session getSession(boolean isTransactional) {
		if (isTransactional) {
			return sessionFactory.getCurrentSession();
		}
		return sessionFactory.openSession();
	}
	
	//-- 常用操作实体类的方法 --//
	
	/**
	 * 取得对象的主键名, 这个也是元数据使用的一个例子.
	 * 
	 * @return
	 */
	public String getIdName() {
		ClassMetadata meta = getSessionFactory().getClassMetadata(entityClass);
		return meta.getIdentifierPropertyName();
	}
	
	/**
	 * 按 id 获取对象. 注意 <code>load()</code> 方法有以下注意的地方:
	 * <ul>
	 *     <li>
	 *     1) 运行 <code>load()</code> 方法时 Hibernate 不会执行任何 select 语句, 只是返回 T 的代理类的实例(该功能由 CGLIB
	 *     提供), 附带 OID 属性, 只有在进一步访问其他属性(如 <code>t.getName()</code>) 的时候才会执行 select
	 *     语句去数据库获取数据, 此所谓延迟加载.
	 *     </li>
	 *     <li>
	 *     2) 运行 <code>load()</code> 方法时当在数据库中不存在与 OID 对应的记录的时候不会抛出异常,
	 *     只有在访问其属性的时候 ((如 <code>t.getName()</code>) 才会抛出 {@link ObjectNotFoundException} 的异常.
	 *     </li>
	 *     <li>
	 *     3) 可以使用如下代码来显式初始化 T 的游离对象:
	 *     <pre>
	 *     T t = load(T.class, 1);
	 *     if (!org.hibernate.Hibernate.isInitialized(t)) {
	 *         org.hibernate.Hibernate.initialize(t);
	 *     }
	 *     </pre>
	 *     </li>
	 * </ul>
	 * 
	 * @param id
	 *            主键
	 * @return
	 * @see #get(Serializable)
	 */
	@SuppressWarnings("unchecked")
	public T load(PK id) {
		return (T) getSession(true).load(entityClass, id);
	}
	
	/**
	 * 按 id 获取对象. 采用 {@link Session#get(Class, Serializable)} 方法在数据库不存在与 OID 对应的记录的时候, 会返回 null.
	 * 采用的是立即检索策略.
	 * <p>
	 * 采用 get() 还是采用 load() 方法的区别在于前者是立即加载, 而后者是延迟加载, 使用的场合有:
	 * <ul>
	 *     <li>1) 如果加载一个对象的目的是为了访问它的各个属性, 用 <code>get()</code> 方法.</li>
	 *     <li>2) 如果加载一个对象的目的是为了删除它, 或者是为了建立与别的对象的关联关系, 用 <code>load()</code> 方法.</li>
	 * </ul>
	 * 
	 * 比如以下代码:
	 * <pre>
	 * Transaction tx = session.beginTransaction();
	 * // 立即检索策略
	 * Order order = (Order) session.get(Order.class, new Long(1));
	 * // 延迟检索策略
	 * Customer customer = (Customer) session.load(Customer.class, new Long(1));
	 * // 建立 Ordre 与 Customer 的多对一单向关联关系
	 * order.setCustomer(customer);
	 * tx.commit();
	 * // Session 不需要知道 Customer 的各个属性的值, 而只要知道 Customer 对象的 OID 即可生成如下 sql 语句:
	 * // update ORDER set CUSTOMER_ID=1, ORDER_NUMBER=... where ID=1;
	 * </pre>
	 * 
	 * @param id
	 *            主键
	 * @return
	 * @see #load(Serializable)
	 */
	@SuppressWarnings("unchecked")
	public T get(PK id) {
		return (T) getSession(true).get(entityClass, id);
	}
	
	/**
	 * 保存新增或修改的对象.
	 * 
	 * @param entity
	 *            对象
	 */
	public void save(T entity) {
		getSession(true).saveOrUpdate(entity);
		logger.debug("save entity: {}", entity);
	}
	
	/**
	 * 删除对象.
	 * 
	 * @param entity
	 *            对象必须是 session 中的对象或含 id 属性的 transient 对象
	 */
	public void delete(T entity) {
		getSession(true).delete(entity);
		logger.debug("delete entity: {}", entity);
	}
	
	/**
	 * 按 id 删除对象. 采用 {@link #load(Serializable)} 方法延迟加载删除, 可以提高些许性能.
	 * 
	 * @param id
	 *            主键
	 */
	public void delete(PK id) {
		delete(load(id));
		logger.debug("delete entity {},id is {}", entityClass.getSimpleName(), id);
	}
	
	/**
	 * 默认以 QBC 的方式获取全部对象.
	 * 
	 * @return
	 */
	public List<T> getAll() {
		return find();
	}
	
	/**
	 * 默认以 QBC 的方式获取全部对象, 其中加入了按属性进行排序.
	 * 
	 * @param orderByProperty
	 *            排序属性
	 * @param isAsc
	 *            是否升序排序
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<T> getAll(String orderByProperty, boolean isAsc) {
		Criteria criteria = createCriteria();
		if (isAsc) {
			criteria.addOrder(Order.asc(orderByProperty));
		} else {
			criteria.addOrder(Order.desc(orderByProperty));
		}
		return criteria.list();
	}
	
	//-- 使用 QBC 的方式进行查询 --//
	
	/**
	 * 根据 Criterion 条件创建 Criteria. 注意其与 {@link BaseHibernateDao#find(Criterion...)} 方法的结合使用.
	 * 
	 * @param criterions
	 *            数量可变的 Criterion
	 * @return
	 */
	public Criteria createCriteria(Criterion... criterions) {
		Criteria criteria = getSession(true).createCriteria(entityClass);
		for (Criterion c : criterions) {
			criteria.add(c);
		}
		return criteria;
	}
	
	/**
	 * 采用 QBC 的检索方式获得对象列表.
	 * 
	 * @param criterions
	 *            数量可变的 Criterion
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<T> find(Criterion... criterions) {
		return createCriteria(criterions).list();
	}
	
	/**
	 * 采用 QBC 的检索方式按属性查找对象列表, 匹配方式为相等.
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            属性值
	 * @return
	 */
	public List<T> find(String propertyName, Object value) {
		Criterion criterion = Restrictions.eq(propertyName, value);
		return find(criterion);
	}
	
	/**
	 * 采用 QBC 的检索方式按照一系列的属性名和属性值查找对象, 匹配方式为相等.
	 * 
	 * @param names
	 *            属性名数组
	 * @param values
	 *            属性值数组
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<T> find(String[] names, Object... values) {
		if (names.length != values.length) {
			throw new IllegalArgumentException("names's length must be equal to values's");
		}
		Criteria criteria = createCriteria();
		for (int i = 0; i < names.length; i++) {
			criteria.add(Restrictions.eq(names[i], values[i]));
		}
		return criteria.list();
	}
	
	/**
	 * 采用 QBC 的检索方式查询唯一对象. 可能有以下几种情况:
	 * <ul>
	 * 	<li>1) 如果有多个值抛 {@link NonUniqueResultException} 异常, 需要用 setMaxResults(1) 方法来限制.</li>
	 * 	<li>2) 如果有值且只有一个, 返回一个 Object.</li>
	 * 	<li>3) 如果没值, 返回 null.</li>
	 * </ul>
	 * 
	 * @param criterions
	 *            数量可变的 Criterion
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T findUnique(Criterion... criterions) {
		return (T) createCriteria(criterions).setMaxResults(1).uniqueResult();
	}
	
	/**
	 * 采用 QBC 的检索方式按属性查找唯一对象, 匹配方式为相等. 该方式可以用户在登录的时候根据对方的登录名来获取该用户, 代码如下:
	 * <pre>
	 * // 获取当前的请求用户
	 * public User getUserByUsername(String username) {
	 *     return userDao.findUniqueBy("username", username);
	 * }
	 * </pre>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            属性值
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T findUnique(String propertyName, Object value) {
		Criterion criterion = Restrictions.eq(propertyName, value);
		return (T) createCriteria(criterion).setMaxResults(1).uniqueResult();
	}
	
	/**
	 * 采用 QBC 的检索方式按照一系列的属性名和属性值查找唯一对象, 匹配方式为相等.
	 * <p>
	 * 这种方法在进行登录检验的时候非常有用, 只需简单地编写代码如下即可:
	 * <pre>
	 * // 获取当前的登录的用户
	 * String[] names = {"username", "password"}; 
	 * public User checkLogin(String username, String password) {
	 *     return userDao.findUniqueBy(names, username, password);
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param names
	 *            属性名数组
	 * @param values
	 *            属性值数组
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T findUnique(String[] names, Object... values) {
		if (names.length != values.length) {
			throw new IllegalArgumentException("names's length must be equal to values's");
		}
		Criteria criteria = createCriteria();
		for (int i = 0; i < names.length; i++) {
			criteria.add(Restrictions.eq(names[i], values[i]));
		}
		return (T) criteria.setMaxResults(1).uniqueResult();
	}
	
	/**
	 * 采用 QBC 的检索方式来计算该实体类对应的数据表中的记录个数, 采用的是投影操作的方式. 注意到由于投影操作和查询的 Order
	 * 条件是冲突的, 所以需要使用反射来进行排除.
	 * 
	 * @param criteria
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public long countResult(Criteria criteria) {
		
		CriteriaImpl impl = (CriteriaImpl) criteria;

		// 先把 Projection、ResultTransformer、OrderBy 取出来, 清空三者后再执行 Count 操作
		Projection projection = impl.getProjection();
		ResultTransformer transformer = impl.getResultTransformer();
		List<CriteriaImpl.OrderEntry> orderEntries = null;
		
		/*
		 * Hibernate 3.6.5 源码: <code>CriteriaImpl#private List orderEntries = new ArrayList();</code>
		 * 可以通过反射来获得其orderEntries 的值, 如果有值则需要暂时把其值清空, 查询完毕再设值回去.
		 */
		orderEntries = (List) ReflectionUtil.getFieldValue(impl, "orderEntries");
		boolean isRestore = false;
		if (orderEntries.size() > 0) {
			isRestore = true;
			ReflectionUtil.setFieldValue(impl, "orderEntries", new ArrayList());
		}
		
		/*
		 * 执行 Count 查询, 注意这里需要先转换成 Number 类型.
		 * 在 Hibernate 2.x 时代返回的是 Integer 类型, 而 Hibernate 3.x 时代返回的是 Long.
		 */
		Number totalCountObject = (Number) criteria.setProjection(Projections.rowCount()).setMaxResults(1).uniqueResult();
		long totalCount = (totalCountObject != null) ? totalCountObject.longValue() : 0;
		
		/*
		 * 将之前的 Projection, ResultTransformer 和 OrderBy 条件重新设回去.
		 * 否则只能查询到总记录数, 而采用 c.list() 返回记录集时却为 null.
		 */
		criteria.setProjection(projection);

		if (projection == null) {
			criteria.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
		}
		if (transformer != null) {
			criteria.setResultTransformer(transformer);
		}
		if (isRestore) {
			ReflectionUtil.setFieldValue(impl, "orderEntries", orderEntries);
		}
		
		return totalCount;
	}
	
	//-- 使用 HQL 的方式进行查询 --//
	
	/**
	 * 根据查询 HQL 与参数列表创建 Query 对象, 采用了 {@link Query#setParameter(int, Object)} 来绑定任意类型参数. 比如:
	 * <pre>
	 * Query query = session.createQuery("from Order o where o.customer=? and o.orderNumber like ?");
	 * query.setParameter(0, customer);
	 * query.setParameter(1, orderNumber);
	 * </pre>
	 * 
	 * 上面的程序默认是使用了 Hibernate 的自动根据参数值的 Java 类型来进行对应的映射类型, 这样可以减少在第三个参数中指定 Java
	 * 类型的麻烦. 但是对于日期的 java.util.Date 类型, 会对应多种 Hibernate 映射类型, 如 Hibernate.DATE 或 Hibernate.TIMESTAMP,
	 * 因此必须在 setParameter() 方法中显式地指定到底对应那种 Hibernate 映射类型. 比如:
	 * <pre>
	 * Query query = session.createQuery("from Customer c where c.birthday=:birthday");
	 * query.setParameter("birthday&quot", birthday, Hibernate.DATE);
	 * </pre>
	 * 所以这个时候该方法将不再适用. 另外注意到这里要采用按位置的方式来进行参数绑定, 所以在写 hql 的时候请注意.
	 * 
	 * @param hql
	 *            查询的 hql 语句
	 * @param values
	 *            数量可变的参数, 按位置绑定
	 * @return
	 */
	public Query createQuery(String hql, Object... values) {
		/*
		 * 考虑两种情况:
		 * 1) "from Customer as c where c.age=? and c.name=?";
		 * 2) "from Customer as c where c.age=? and c.name=? order by c.age";
		 * 这里将无法确切地获取到需要绑定的参数的个数, 所以只好使用下面这种方式进行处理.
		 */
		if (StringUtils.split(hql, "?").length < values.length) {
			throw new IllegalArgumentException("parameters' size must be equal to hqls'");
		}
		Query query = getSession(true).createQuery(hql);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				// 按顺序绑定参数
				query.setParameter(i, values[i]);
			}
		}
		return query;
	}
	
	/**
	 * 为 {@link #countResult(String, Object...)} 和 {@link #countResult(String, Map)} 获得 Hql 查询获得的对象总数做预处理.
	 * 
	 * @param orgHql
	 *            原始 hql 查询语句, 比如: select u from GenericUser as u where u.age<30 order by u.age desc
	 * @return
	 */
	protected String prepareCountHql(String orgHql) {
		
		String fromHql = orgHql;
		/*
		 * select 子句与 order by 子句会影响 count 查询, 进行简单的排除. 比如:
		 * select name from order as o order by o.number asc -> from order as o
		 */
		fromHql = "from " + StringUtils.substringAfter(fromHql, "from");
		fromHql = StringUtils.substringBefore(fromHql, "order by");

		// 最后简化成: select count(*) from GenericUser as u where u.age<30
		String countHql = "select count(*) " + fromHql;
		return countHql;
	}
	
	/**
	 * 采用 HQL 的检索方式查询对象列表.
	 * 
	 * @param <X>
	 * @param hql
	 *            查询的 hql 语句
	 * @param values
	 *            数量可变的参数, 按位置绑定
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <X> List<X> find(String hql, Object... values) {
		return createQuery(hql, values).list();
	}
	
	/**
	 * 采用 HQL 的检索方式查询唯一对象.
	 * 
	 * @param <X>
	 * @param hql
	 *            查询的 hql 语句
	 * @param values
	 *            数量可变的参数, 按位置绑定
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <X> X findUnique(String hql, Object... values) {
		return (X) createQuery(hql, values).setMaxResults(1).uniqueResult();
	}
	
	/**
	 * 采用 HQL 的检索方式来计算该实体类对应的数据表中的记录个数.
	 * <p>
	 * 注意本函数只能自动处理简单的 hql 语句, 复杂的 hql 查询请另行编写 count 语句查询.
	 * </p>
	 * 
	 * @param hql
	 *            简单的 hql 语句, 比如: select u from GenericUser as u where u.age<30 order by u.age desc
	 * @param values
	 * @return
	 */
	public long countResult(String hql, Object... values) {
		String countHql = prepareCountHql(hql);
		try {
			Number count = findUnique(countHql, values);
			return count.longValue();
		} catch (Exception e) {
			throw new RuntimeException("hql can't be auto count, hql is:" + countHql, e);
		}
	}
	
	/**
	 * 根据查询 HQL 与参数列表创建 Query 对象, 采用了 {@link Query#setProperties(Object))} 来把命名参数和对象的属性值绑定起来.
	 * 比如:
	 * <pre>
	 * Query query = session.createQuery("from Customer as c where c.name=:name and c.age=:age";
	 * Customer customer = new Customer();
	 * customer.setName("Tom");
	 * customer.setAge(21);
	 * // 命名参数中的 "name" 和 "age" 必须分别对应 Customer 类的 name 属性和 age 属性, 否则会抛异常
	 * query.setProperties(customer);
	 * </pre>
	 * 
	 * 在本方法中采用的是传递一个 {@link Map} 作为参数, 同样会根据 Map 的名来进行自动绑定. 另外, setProperties() 方法调用
	 * setParameter() 方法, setParameter() 方法再根据 Customer 对象的属性的 Java 类型来判断 Hibernate 映射类型.
	 * 所以如果命名参数为日期类型, 则不能通过 setProperties() 方法来绑定. 另外, 参数绑定对 null 是安全的, 如下代码不会抛异常:
	 * <pre>
	 * String name = null;
	 * session.createQuery("from Customer as c where c.name=:name").setString("name", name).list();
	 * </pre>
	 * 
	 * 上面的查询语句对应的 SQL 语句是:
	 * <pre>
	 * select * from CUSTOMERS where NMAE=null;
	 * </pre>
	 * 
	 * 这条查询语句的查询结果永远为空, 因为在 SQL 中, 表达式 (null=null) 及表达式 ('Tom'=null) 的比较结果既不是 true 也不是
	 * false, 而是 null. 所以如果要查询名字为 null 的客户, 应该使用 "is null" 比较运算符, 比如:
	 * <pre>
	 * // HQL 检索方式
	 * session.createQuery("from Customer c where c.name is null");
	 * // QBC 检索方式
	 * session.createCriteria(Customer.class).add(Restrictions.isNull("name"));
	 * </pre>
	 * 
	 * @param hql
	 *            查询的 hql 语句
	 * @param values
	 *            数量可变的参数, 按名称绑定
	 * @return
	 */
	public Query createQuery(String hql, Map<String, ?> values) {
		Query query = getSession(true).createQuery(hql);
		if (values != null) {
			// 把命名参数与对象的属性值进行绑定
			query.setProperties(values);
		}
		return query;
	}
	
	/**
	 * 采用 HQL 的检索方式查询对象列表.
	 * 
	 * @param <X>
	 * @param hql
	 *            查询的 hql 语句
	 * @param values
	 *            数量可变的参数, 按名称绑定
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <X> List<X> find(String hql, Map<String, ?> values) {
		return createQuery(hql, values).list();
	}
	
	/**
	 * 采用 HQL 的检索方式查询唯一对象.
	 * 
	 * @param <X>
	 * @param hql
	 *            查询的 hql 语句
	 * @param values
	 *            数量可变的参数, 按名称绑定
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <X> X findUnique(String hql, Map<String, ?> values) {
		return (X) createQuery(hql, values).setMaxResults(1).uniqueResult();
	}
	
	/**
	 * 采用 HQL 的检索方式来计算该实体类对应的数据表中的记录个数.
	 * <p>
	 * 注意本函数只能自动处理简单的 hql 语句, 复杂的 hql 查询请另行编写 count 语句查询.
	 * </p>
	 * 
	 * @param hql
	 *            简单的 hql 语句, 比如: select u from GenericUser as u where u.age<30 order by u.age desc
	 * @param values
	 * @return
	 */
	public long countResult(final String hql, final Map<String, ?> values) {
		String countHql = prepareCountHql(hql);
		try {
			Number count = findUnique(countHql, values);
			return count.longValue();
		} catch (Exception e) {
			throw new RuntimeException("hql can't be auto count, hql is:" + countHql, e);
		}
	}
	
	//-- 其他的一些 Hibernate 支持的方法 --//
	
	/**
	 * 手动 <code>flush()</code> 来清理缓存并更新数据库, 该方法只对事务 <code>session</code> 起作用.
	 * <p>
	 * 注意到 <code>flush()</code> 在清理缓存的时候会执行一系列的 SQL 语句, 在这个时候应该可以从 console 中看到 SQL 语句,
	 * 但不会提交事务. <code>Session</code> 注意通过 {@link FlushMode} 来设置缓存清理模式, 可以通过
	 * {@link #setFlushMode(FlushMode)} 方法来设置.
	 * </p>
	 * 
	 * <p>
	 * 多数情况下应用并不需要显示地调用该方法, 该方法适合于以下场景:
	 * <ul>
	 * 	<li>1) 插入、删除或更新某个持久化对象会引起数据库中的触发器, 这个时候就需要手动地 <code>flush()</code> 来触发触发器.</li>
	 * 	<li>2) 在应用中混合地使用了 Hibernate API 和 JDBC API.</li>
	 * 	<li>3) JDBC 程序不够健壮, 导致 Hibernate 在自动清理缓存的模式下无法正常工作.</li>
	 * </ul>
	 * </p>
	 */
	public void flush() {
		getSession(true).flush();
	}
	
	/**
	 * 手动设置缓存清理模式, 该方法只对事务 <code>session</code> 起作用. 缓存的清理模式主要有以下三种:
	 * <ul>
	 * 	<li>1) FlushMode.AUTO(default) -> 查询方法(清理) & commit方法(清理) & flush方法(清理)</li>
	 * 	<li>2) FlushMode.COMMIT -> 查询方法(不清理) & commit方法(清理) & flush方法(清理)</li>
	 * 	<li>3) FlushMode.NEVER -> 查询方法(不清理) & commit方法(不清理) & flush方法(清理)</li>
	 * </ul>
	 * {@link FlushMode#AUTO} 是默认并优先考虑的模式, 它可以保证在整个事务中 <code>Session</code>
	 * 缓存中的对象和数据库数据保持一致; 但如果事务仅包含查询数据库的操作, 不妨将 <code>session</code> 设置成
	 * {@link FlushMode#COMMIT} 模式, 可以避免在执行各种查询操作时先清理缓存, 稍微提高应用性能; {@link FlushMode#NEVER}
	 * 表示只有当程序显示调用 <code>flush()</code> 的时候才清理缓存, 这个适用于长时间运行的复杂事务.
	 * 
	 * <p>
	 * 这里的一个建议就是: 如果是只读事务, 推荐手动设置成 {@link FlushMode#COMMIT} 模式.
	 * </p>
	 * 
	 * @param flushMode
	 * @see #flush()
	 */
	public void setFlushMode(FlushMode flushMode) {
		getSession(true).setFlushMode(flushMode);
	}
	
	/**
	 * 采用 HQL 的检索方式进行批量修改/删除操作.
	 * <p>
	 * 批量处理是指在一个事务中处理大量数据, 比如下面的一段代码(将每个人的年龄加 1):
	 * <pre>
	 * {@code
	 * Iterator<Customer> customers = session.createQuery("from Customer as c where c.age>0").list().iterator();
	 * while (customers.hasNext()) {
	 *     Customer customer = customers.next();
	 *     customer.setAge(customer.getAge() + 1);
	 * }
	 * tx.commit();
	 * </pre>
	 * 现在假设 t_customer 表中有 10,000 条数据, 那么 Hibernate 会一下子加载 10,000 个 Customer 对象到内存中. 当执行 tx.commit()
	 * 的时候, 会清理缓存, 这时候 Hibernate 会执行 10,000 条 update 语句. 这种方式有两个缺点:
	 * <ul>
	 * 	<li>1) 占用过多的内存.</li>
	 * 	<li>2) 执行 10,000 条 SQL 语句, 频繁访问数据库导致性能下降.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 一般说来, 我们应尽可能避免在应用层进行批量处理, 而应该直接在数据层进行批量操作, 如果批量操作的逻辑比较复杂,
	 * 还可以考虑直接在数据库中通过存储过程来完成. (MySQL 暂时不支持通过存储过程来完成批量操作)
	 * </p>
	 * 
	 * <p>
	 * 当然, 如果一定也可以在 DAO 层完成批量操作, 主要有以下方式:
	 * <ul>
	 * 	<li>1) 通过 <code>session</code> 来进行批量操作, 需要设置 hibernate.jdbc.batch_size 的值, 一般设置为 10~50 较合适.</li>
	 * 	<li>2) 通过 {@link StatelessSession} 来进行批量操作.</li>
	 * 	<li>3) 通过 HQL 来进行批量操作, 该操作实际上直接在数据库中完成, 所处理的数据并不会加载到 <code>session</code> 的缓存中.</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * 这里我们使用的批量操作方法正是 Hibernate 3 中支持的, {@link Query#executeUpdate()} 方法可用于批量更新, 批量删除,
	 * 批量插入. 比如下面的一段代码:
	 * <pre>
	 * String hql = "update Customer as c set c.name=? where c.name=?";
	 * int updateEntities = createQuery(hql, "Mike", "Tom").executeUpdate();
	 * tx.commit();
	 * </pre>
	 * 以上的语句向数据库直接发送 SQL 语句: update t_customer set NAME="Mike" where NAME="TOM"; 或者如下代码:
	 * <pre>
	 * String hql = "delete Customer as c where c.name=?";
	 * int updateEntities = createQuery(hql, "Mike").executeUpdate();
	 * tx.commit();
	 * </pre>
	 * 以上的语句向数据库直接发送 SQL 语句: delete from t_customer where NAME="Mike";
	 * <p>
	 * 
	 * @param hql
	 *            查询的 hql 语句
	 * @param values
	 *            数量可变的参数, 按位置绑定
	 * @return 更新记录数
	 */
	public int batchExecute(String hql, Object... values) {
		return createQuery(hql, values).executeUpdate();
	}
	
	/**
	 * 采用 HQL 的检索方式进行批量修改/删除操作.
	 * 
	 * @param hql
	 *            查询的 hql 语句
	 * @param values
	 *            数量可变的参数, 按位置绑定
	 * @return 更新记录数
	 * @see #batchExecute(String, Object...)
	 */
	public int batchExecute(String hql, Map<String, ?> values) {
		return createQuery(hql, values).executeUpdate();
	}
}
