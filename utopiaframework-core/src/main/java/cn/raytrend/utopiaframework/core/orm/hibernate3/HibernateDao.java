/*
 * HibernateDao.java
 * 
 * Created on 27/11/2011
 */
package cn.raytrend.utopiaframework.core.orm.hibernate3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import cn.raytrend.utopiaframework.core.orm.Page;
import cn.raytrend.utopiaframework.core.orm.PropertyFilter;
import cn.raytrend.utopiaframework.core.orm.PropertyFilter.MatchType;

/**
 * 在 {@link BaseHibernateDao} 的基础上加入了如下特性:
 * <ul>
 * 	<li>1) 分页查询.</li>
 * 	<li>2) 按属性过滤条件列表查询.</li>
 * </ul>
 *
 * @param <T>
 *            DAO 操作的对象类型
 * @param <PK>
 *            主键类型
 *            
 * @author zhouych
 * @see PropertyFilter
 */
public class HibernateDao<T, PK extends Serializable> extends BaseHibernateDao<T, PK> {

	/**
	 * 通过子类的泛型定义获得对象的类型. 比如:
	 * <pre>
	 * {@code
	 * public class UserDao extends HibernateDao<User, Long>
	 * }
	 * </pre>
	 */
	public HibernateDao() {
		super();
	}
	
	/**
	 * 跳过 DAO 层, 直接在 Service 层使用 BaseHibernateDao 的构造函数, 在构造函数中定义对象类型.
	 * <pre>
	 * {@code
	 * HibernateDao<User, Long> userDao = new HibernateDao<User, Long>(sessionFactory, User.class);
	 * }
	 * </pre>
	 * @param sessionFactory
	 * @param entityClass
	 */
	public HibernateDao(final SessionFactory sessionFactory, final Class<T> entityClass) {
		super(sessionFactory, entityClass);
	}
	
	/**
	 * 默认以 QBC 的方式获取指定页的内容.
	 * 
	 * @param page
	 * @return
	 */
	public Page<T> getAll(Page<T> page) {
		return findPage(page);
	}
	
	//-- 以 QBC 的方式进行分页查询 --//
	
	@SuppressWarnings("unchecked")
	public Page<T> findPage(Page<T> page, Criterion... criterions) {
		Criteria criteria = createCriteria(criterions);
		// Page 默认是自动计算记录数
		if (page.isAutoCount()) {
			long totalCount = countResult(criteria);
			page.setTotalCount(totalCount);
		}
		setPageParameterToCriteria(criteria, page);
		
		List<T> result = criteria.list();
		page.setResult(result);
		return page;
	}
	
	/**
	 * 设置分页参数到 {@link Criteria} 对象, 这里主要是使用到了 <code>Criteria</code> 的分页查询.
	 * 
	 * @param criteria
	 *           将 <code>Page</code> 中的参数设置到其中的并返回的 <code>Criteria</code>
	 * @param page
	 * @return
	 */
	protected Criteria setPageParameterToCriteria(Criteria criteria, Page<T> page) {
		if (page.getPageSize() <= 0) {
			throw new IllegalArgumentException("Page#pageSize must larger than 0");
		}
		
		// 注意 Hibernate 的 firstResult 的序号从 0 开始
		criteria.setFirstResult(page.getFirst() - 1);
		criteria.setMaxResults(page.getPageSize());
		
		if (page.isOrderBySetted()) {
			// 如果当前页内容中设置了排序的条件, 则必须进行处理 (注意到排序条件可能有多条, 以 ',' 分离)
			String[] orderByArray = StringUtils.split(page.getOrderBy(), ',');
			String[] orderArray = StringUtils.split(page.getOrder(), ',');			
			// 分页多重排序参数中的排序字段与排序方向的个数必须相等
			if (orderArray.length != orderByArray.length) {
				throw new IllegalArgumentException("Page#orderArray's length must be equal to Page#orderByArray's");
			}
			for (int i = 0; i < orderByArray.length; i++) {
				if (Page.ASC.equals(orderArray[i])) {
					criteria.addOrder(Order.asc(orderByArray[i]));
				} else {
					criteria.addOrder(Order.desc(orderByArray[i]));
				}
			}
		}
		
		return criteria;
	}
	
	//-- 以 HQL 的方式进行分页查询 --//
	
	/**
	 * 按 HQL 分页查询, 不支持排序, 推荐使用 {@link #findPage(Page, Criterion...)} 方法.
	 * 
	 * @param page
	 *            分页参数. 注意不支持其中的 orderBy 参数
	 * @param hql
	 *            hql 语句
	 * @param values
	 *            数量可变的查询参数,按顺序绑定
	 * 
	 * @return 分页查询结果, 附带结果列表及所有查询输入参数
	 */
	@SuppressWarnings("unchecked")
	public Page<T> findPage(Page<T> page, String hql, Object... values) {
		
		Query query = createQuery(hql, values);
		if (page.isAutoCount()) {
			long totalCount = countResult(hql, values);
			page.setTotalCount(totalCount);
		}
		setPageParameterToQuery(query, page);
		
		List<T> result = query.list();
		page.setResult(result);
		return page;
	}
	
	/**
	 * 按 HQL 分页查询, 不支持排序, 推荐使用 {@link #findPage(Page, Criterion...)} 方法.
	 * 
	 * @param page
	 *            分页参数. 注意不支持其中的 orderBy 参数
	 * @param hql
	 *            hql 语句
	 * @param values
	 *            命名参数, 按名称绑定
	 * 
	 * @return 分页查询结果, 附带结果列表及所有查询输入参数
	 */
	@SuppressWarnings("unchecked")
	public Page<T> findPage(Page<T> page, String hql, Map<String, ?> values) {
		
		Query query = createQuery(hql, values);
		if (page.isAutoCount()) {
			long totalCount = countResult(hql, values);
			page.setTotalCount(totalCount);
		}
		setPageParameterToQuery(query, page);

		List result = query.list();
		page.setResult(result);
		return page;
	}
	
	/**
	 * 设置分页参数到 Query 对象.
	 * 
	 * @param query
	 * @param page
	 * @return
	 */
	protected Query setPageParameterToQuery(final Query query, final Page<T> page) {
		if (page.getPageSize() <= 0) {
			throw new IllegalArgumentException("Page#pageSize must larger than 0");
		}
		
		// 注意 Hibernate 的 firstResult 的序号从 0 开始
		query.setFirstResult(page.getFirst() - 1);
		query.setMaxResults(page.getPageSize());
		return query;
	}
	
	//-- 采用 QBC 的方式来根据过滤条件列表查询结果 (高级查询) --//
	
	/**
	 * 按给定的属性, 属性值和比较条件来构造 {@link Criterion}.
	 * 
	 * @param propertyName
	 *            属性名
	 * @param propertyValue
	 *            属性值
	 * @param matchType
	 *            比较类型, 注意如果是 LIKE 类型则必须保证其属性值为 String 类型
	 * @return
	 */
	protected Criterion buildCriterion(String propertyName, Object propertyValue, MatchType matchType) {
		
		Criterion criterion = null;
		// 根据 MatchType 来构造条件 Criterion
		switch (matchType) {
			case EQ:
				criterion = Restrictions.eq(propertyName, propertyValue);
				break;
			case NE:
				criterion = Restrictions.ne(propertyName, propertyValue);
				break;
			case GT:
				criterion = Restrictions.gt(propertyName, propertyValue);
				break;
			case GE:
				criterion = Restrictions.ge(propertyName, propertyValue);
				break;
			case LT:
				criterion = Restrictions.lt(propertyName, propertyValue);
				break;
			case LE:
				criterion = Restrictions.le(propertyName, propertyValue);
				break;
			case LIKE:
				criterion = Restrictions.like(propertyName, (String) propertyValue, MatchMode.ANYWHERE);
		}
		return criterion;
	}
	
	/**
	 * 按给定的属性, 属性值和比较条件来获取对象列表.
	 * 
	 * @param propertyName
	 *            属性名
	 * @param propertyValue
	 *            属性值
	 * @param matchType
	 *            比较类型, 注意如果是 LIKE 类型则必须保证其属性值为 String 类型
	 * @return
	 */
	public List<T> find(String propertyName, Object propertyValue, MatchType matchType) {
		Criterion criterion = buildCriterion(propertyName, propertyValue, matchType);
		return find(criterion);
	}
	
	/**
	 * 按给定的属性, 属性值和比较条件以分页形式来获取对象列表.
	 * 
	 * @param page
	 * @param propertyName
	 *            属性名
	 * @param propertyValue
	 *            属性值
	 * @param matchType
	 *            比较类型, 注意如果是 LIKE 类型则必须保证其属性值为 String 类型
	 * @return
	 */
	public Page<T> findPage(Page<T> page, String propertyName, Object propertyValue, MatchType matchType) {
		return findPage(page, buildCriterion(propertyName, propertyValue, matchType));
	}
	
	/**
	 * 根据给定的 "属性过滤条件" 组合来构造 <code>Criterion</code>.
	 * 
	 * @param filters
	 *            属性过滤条件组合
	 * @return
	 */
	protected Criterion[] buildCriterionByPropertyFilter(List<PropertyFilter> filters) {
		
		List<Criterion> criterionList = new ArrayList<Criterion>(5);
		for (PropertyFilter filter : filters) {
			if (!filter.hasMultiProperties()) {
				// 只有一个属性需要比较的情况, 比较简单
				Criterion criterion = buildCriterion(filter.getPropertyName(), filter.getMatchValue(),
						filter.getMatchType());
				criterionList.add(criterion);
			} else {
				// 包含多个属性需要比较的情况,进行 or 处理
				Disjunction disjunction = Restrictions.disjunction();
				for (String param : filter.getPropertyNames()) {
					Criterion criterion = buildCriterion(param, filter.getMatchValue(), filter.getMatchType());
					disjunction.add(criterion);
				}
				criterionList.add(disjunction);
			}
		}
		return criterionList.toArray(new Criterion[criterionList.size()]);
	}
	
	/**
	 * 根据给定的 "属性过滤条件" 组合来获取对象列表.
	 * 
	 * @param filters
	 *            属性过滤条件组合
	 * @return
	 */
	public List<T> find(List<PropertyFilter> filters) {
		return find(buildCriterionByPropertyFilter(filters));
	}
	
	/**
	 * 根据给定的 "属性过滤条件" 组合以分页的形式来获取对象列表.
	 * 
	 * @param page
	 * @param filters
	 *            属性过滤条件组合
	 * @return
	 */
	public Page<T> findPage(Page<T> page, List<PropertyFilter> filters) {
		return findPage(page, buildCriterionByPropertyFilter(filters));
	}
}
