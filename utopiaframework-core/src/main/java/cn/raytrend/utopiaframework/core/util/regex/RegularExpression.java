/*
 * RegularExpression.java
 * 
 * Created on 26/11/2011
 */
package cn.raytrend.utopiaframework.core.util.regex;

/**
 * 各种验证比如邮箱手机号码等的正则表达式.
 * 
 * @author zhouych
 */
public class RegularExpression {

	/**
	 * 验证邮箱格式的正则表达式
	 */
	public final static String REG_EMAIL = "^[\\w\\d]+@[\\w\\d]+(\\.[\\w\\d]+)+$";

	/**
	 * 验证电话的正则表达式, 格式应为: 020-39349804、0123-1123141 等
	 */
	public final static String REG_TELEPHONE = "\\d{4}-\\d{8}|\\d{4}-\\d{7}|\\d(3)-\\d(8)";
	
	/**
	 * 验证手机号码的正则表达式
	 */
	public final static String REG_MOBILEPHONE = "^[1][3,5]+\\d{9}";
	
	/**
	 * 验证 URL 的正则表达式
	 */
	public final static String REG_URL = "[a-zA-z]+://[^\\s]*";
	
	/**
	 * 验证 QQ 的正则表达式. 首位不为 0, 且长度在 15 以内
	 */
	public final static String REG_QQ = "[1-9][0-9]{4,13}";
	
	/**
	 * 验证邮编的正则表达式
	 */
	public final static String REG_POSTALCODE = "[1-9]\\d{5}(?!\\d)";
}
