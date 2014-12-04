/*
 * Page.java
 * 
 * Created on 26/11/2011
 */
package cn.raytrend.utopiaframework.core.orm;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

/**
 * 与具体的 ORM 实现无关的分页参数及查询结果封装, 这里所有的序号从 1 开始.
 * 
 * @author zhouych
 * @param <T>
 *            Page 中记录的类型
 */
public class Page<T> {

	/**
	 * 升序排列
	 */
	public static final String ASC = "asc";
	
	/**
	 * 降序排列
	 */
	public static final String DESC = "desc";

	/**
	 * 当前页数
	 */
	protected int pageNo = 1;
	
	/**
	 * 每页容纳的记录条数
	 */
	protected int pageSize = -1;
	
	/**
	 * 排序字段
	 */
	protected String orderBy = null;
	
	/**
	 * 排序方向
	 */
	protected String order = null;
	
	/**
	 * 是否自动计算记录数
	 */
	protected boolean autoCount = true;

	/**
	 * 返回的记录集, 注意到使用 {@code Lists#newArrayList()} 的方式不用考虑泛型的问题, 在 JDK 7 中貌似添加了该功能
	 */
	protected List<T> result = Lists.newArrayList();
	
	/**
	 * 返回的记录条数
	 */
	protected long totalCount = -1;

	public Page() {
	}
	
	public Page(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageNo() {
		return pageNo;
	}

	/**
	 * 设置当前页的页号,序号从 1 开始，低于 1 时自动调整为 1.
	 * 
	 * @param pageNo
	 *            当前页码
	 */
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
		if (pageNo < 1) {
			this.pageNo = 1;
		}
	}
	
	/**
	 * 返回 {@link Page} 对象自身的 {@link Page#setPageNo(int)} 函数，可用于连续设置.
	 * 
	 * @param thePageNo
	 * @return
	 */
	public Page<T> pageNo(final int thePageNo) {
		setPageNo(thePageNo);
		return this;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	/**
	 * 返回 {@link Page} 对象自身的 {@link Page#setPageSize(int)} 函数,可用于连续设置.
	 * 
	 * @param thePageSize
	 * @return
	 */
	public Page<T> pageSize(final int thePageSize) {
		setPageSize(thePageSize);
		return this;
	}
	
	/**
	 * 根据 {@link Page#pageNo} 和 {@link Page#pageSize} 来计算当前页第一条记录在总结果集中的位置, 序号从 1 开始.
	 * 
	 * @return
	 */
	public int getFirst() {
		return ((pageNo - 1) * pageSize) + 1;
	}
	
	/**
	 * 根据 {@link Page#pageSize} 与 {@link Page#totalCount} 计算总页数, 默认值为 -1.
	 * 
	 * @return
	 */
	public long getTotalPages() {
		if (totalCount < 0) {
			return -1;
		}

		long count = totalCount / pageSize;
		if (totalCount % pageSize > 0) {
			count++;
		}
		return count;
	}

	/**
	 * 获得排序字段，无默认值. 多个排序字段时用','分隔.
	 * 
	 * @return
	 */
	public String getOrderBy() {
		return orderBy;
	}

	/**
	 * 设置排序字段，多个排序字段时用','分隔.
	 * 
	 * @param orderBy
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	
	/**
	 * 返回 {@link Page} 对象自身的 {@link Page#orderBy(String)} 函数, 可用于连续设置.
	 * 
	 * @param theOrderBy
	 * @return
	 */
	public Page<T> orderBy(final String theOrderBy) {
		setOrderBy(theOrderBy);
		return this;
	}

	public String getOrder() {
		return order;
	}

	/**
	 * 设置排序方向
	 * 
	 * @param order
	 *            可选值为 desc 或  asc, 多个排序字段时用','分隔
	 */
	public void setOrder(final String order) {
		String lowcaseOrder = StringUtils.lowerCase(order);
		// 检查 order 字符串的合法值
		String[] orders = StringUtils.split(lowcaseOrder, ',');
		for (String orderStr : orders) {
			if (!StringUtils.equals(DESC, orderStr) && !StringUtils.equals(ASC, orderStr)) {
				throw new IllegalArgumentException("order: " + orderStr + " is invalid.");
			}
		}
		this.order = lowcaseOrder;
	}

	/**
	 * 返回 {@link Page} 对象自身的 {@link Page#setOrder(String)} 函数, 可用于连续设置.
	 * 
	 * @param theOrder
	 * @return
	 */
	public Page<T> order(final String theOrder) {
		setOrder(theOrder);
		return this;
	}
	
	/**
	 * 是否已设置排序字段, 无默认值.
	 * 
	 * @return
	 */
	public boolean isOrderBySetted() {
		return (StringUtils.isNotBlank(orderBy) && StringUtils.isNotBlank(order));
	}
	
	/**
	 * 获得查询对象时是否先自动执行 count 查询获取总记录数, 默认为 true.
	 * 
	 * @return
	 */
	public boolean isAutoCount() {
		return autoCount;
	}
	
	/**
	 * 设置查询对象时是否自动先执行 count 查询获取总记录数.
	 * 
	 * @param autoCount
	 */
	public void setAutoCount(final boolean autoCount) {
		this.autoCount = autoCount;
	}
	
	/**
	 * 返回 {@link Page} 对象自身的 {@link Page#setAutoCount(boolean)} 函数,可用于连续设置.
	 * 
	 * @param theAutoCount
	 * @return
	 */
	public Page<T> autoCount(final boolean theAutoCount) {
		setAutoCount(theAutoCount);
		return this;
	}
	
	/**
	 * 获得页内的记录列表.
	 * 
	 * @return
	 */
	public List<T> getResult() {
		return result;
	}
	
	/**
	 * 设置页内的记录列表.
	 * 
	 * @param result
	 */
	public void setResult(final List<T> result) {
		this.result = result;
	}
	
	/**
	 * 获得总记录数, 默认值为 -1.
	 * 
	 * @return
	 */
	public long getTotalCount() {
		return totalCount;
	}
	
	/**
	 * 设置总记录数.
	 * 
	 * @param totalCount
	 */
	public void setTotalCount(final long totalCount) {
		this.totalCount = totalCount;
	}
	
	/**
	 * 是否还有上一页.
	 * 
	 * @return
	 */
	public boolean isHasPrevious() {
		return (pageNo - 1 >= 1);
	}
	
	/**
	 * 是否还有下一页.
	 * 
	 * @return
	 */
	public boolean isHasNext() {
		return (pageNo + 1 <= getTotalPages());
	}
	
	/**
	 * 取得上页的页号，序号从 1 开始. 当前页为首页时返回首页序号.
	 * 
	 * @return
	 */
	public int getPrePage() {
		if (isHasPrevious()) {
			return pageNo - 1;
		}
		return pageNo;
	}
	
	/**
	 * 取得下页的页号, 序号从 1 开始. 当前页为尾页时仍返回尾页序号.
	 * 
	 * @return
	 */
	public int getNextPage() {
		if (isHasNext()) {
			return pageNo + 1;
		}
		return pageNo;
	}
}
