/*
 * ShortUrlGenerator.java
 * 
 * Created on 06/01/2012
 */
package cn.raytrend.utopiaframework.web.util.url;

import cn.raytrend.utopiaframework.core.util.codec.DigestUtil;


/**
 * 网址短链接的生成器.
 * 
 * @author zhouych
 * @see http://hi.baidu.com/cubeking/blog/item/5e73241e5d75769786d6b658.html
 */
public class ShortUrlUtil {
	
	/**
	 * 用来生成 url 的字符, 从 62 个字符中选出 6 个, 可以有 (62^6) 种结果
	 */
	private static char[] CHARS = new char[] {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
	};

	/**
	 * 获取指定的 url 的短链接. 该算法的原理如下:
	 * <ul>
	 * <li>1) 将长网址 md5 生成 32 位串, 分为 4 段, 每段 8 个字节.</li>
	 * <li>2) 对这 4 段循环处理, 取8个字节, 看成16进制串与 0x3fffffff(30位1)与操作, 即超过30位的忽略处理.</li>
	 * <li>3) 这 30 位分成 6 段, 每 5 位的数字作为字母表的索引取得特定字符, 依次进行获得 6 位字符串.</li>
	 * <li>4) 总的 md5 串可以获得 4 个 6 位串, 取里面的任意一个就可作为这个长 url 的短 url 地址.</li>
	 * </ul>
	 * 
	 * @param url
	 * @return
	 */
	private static String[] getShortURLs(String url) {
		
		// 对传入的网址进行 md5 加密
		String md5Url = DigestUtil.getMD5String(url);
		// 结果集, 一个 md5 可以得到 4 个结果
		String[] results = new String[4];
		
		for (int i = 0; i < results.length; i++) {
			// 把加密字符按照 8 位一组 16 进制与 0x3FFFFFFF 进行位与运算
			long hexLong = 0x3FFFFFFF & Long.parseLong(md5Url.substring(i * 8, i * 6 + 8), 16);
			String outChars = "";
			for (int j = 0; j < 6; j++) {
				// 把得到的值与 0x0000003D 进行位与运算，取得字符数组 chars 索引
				long index = 0x0000003D & hexLong;
				// 把取得的字符相加
				outChars += CHARS[(int) index];
				// 每次循环按位右移 5 位
				hexLong = hexLong >> 5;
			}
			// 把字符串存入对应索引的输出数组
			results[i] = outChars;
		}
		return results;
	}
	
	/**
	 * 获取指定的 url 的短链接, 取结果集的第一个作为短 url 结果.
	 * 
	 * @param url
	 * @return
	 */
	public static String getShortUrl(String url) {
		return getShortURLs(url)[0];
	}
}
