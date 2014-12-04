/*
 * MathUtil.java
 * 
 * Created on 25/11/2011
 */
package cn.raytrend.utopiaframework.core.util.lang;

/**
 * 提供了一些常用的数学函数, 并且对 {@link Math} 做了二次封装.
 * 
 * @author zhouych
 */
public class MathUtil {

	/**
	 * 将某 float 浮点数四舍五入到指定的小数个数, 请注意到有可能由于数值过大导致溢出的问题.
	 * 
	 * @param num
	 * @param decimalsNum
	 * @return
	 */
	public static float round(float num, int decimalsNum) {
		if (decimalsNum < 1) {
			throw new IllegalArgumentException("decimalsNum can't be less than 1");
		}
		int tmp = (int) Math.pow(10d, (double)decimalsNum);
		if (tmp == 0) {
			throw new IllegalArgumentException("check again your num, the divisor(result) can't be 0");
		}
		return (float) Math.round(num * tmp) / tmp;
	}
}
