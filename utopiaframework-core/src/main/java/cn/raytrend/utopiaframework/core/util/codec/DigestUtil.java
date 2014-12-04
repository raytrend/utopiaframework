/*
 * DigestUtil.java
 * 
 * Created on 25/11/2011
 */
package cn.raytrend.utopiaframework.core.util.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 支持 SHA-1 / MD5 消息摘要的工具类, 支持 HEX 与 Base64 两种编码方式.
 * <p>
 * 在 Java 中, {@link MessageDigest} 中已经定义了 MD5 的计算, 所以只要简单地调用即可得到 MD5 的 128 位整数, 然后将此 128 位计 16
 * 个字节转换成 16 机制表示即可. 在 RFC 1321 中, 给出了测试用例用来检验其实现是否正确:
 * <ul>
 * 	<li>MD5 ("") = d41d8cd98f00b204e9800998ecf8427e</li>
 * 	<li>MD5 ("a") = 0cc175b9c0f1b6a831c399e269772661</li>
 * 	<li>MD5 ("abc") = 900150983cd24fb0d6963f7d28e17f72</li>
 * 	<li>MD5 ("message digest") = f96b697d7cb7938d525a2f31aaf161d0</li>
 * 	<li>MD5 ("abcdefghijklmnopqrstuvwxyz") = c3fcd3d76192e4007dfb496cca67e13b</li>
 * </ul>
 * 
 * @author zhouych
 * @see EncoderUtil
 */
public class DigestUtil {

	/**
	 * SHA-1 散列算法
	 */
	public static final String SHA1 = "SHA-1";
	
	/**
	 * MD5 散列算法
	 */
	public static final String MD5 = "MD5";
	
	/**
	 * MD5 加密需要用到的字符串
	 */
	private static char[] HEX_DIGITS = new char[]{
		'0', '1', '2', '3', '4', '5',
		'6', '7', '8', '9', 'A', 'B',
		'C', 'D', 'E', 'F'
	};
	
	/**
	 * 将输入的字符串生成 32 位的 MD5 值.
	 * 
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String getMD5String(String input) {
		
	    MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("exception can't be happened: " + e.getMessage());
        }
        byte[] inputBytes;
        try {
            inputBytes = input.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("exception can't be happened: " + e.getMessage());
        }
        // 将输入数据(口令)传给消息摘要
        md.update(inputBytes);
        // 生成消息摘要
        byte[] tmp = md.digest();
        char[] output = new char[16 * 2];
        int index = 0;
        for (int i = 0; i < 16; i++) {
            byte b = tmp[i];
            output[index++] = HEX_DIGITS[b >> 4 & 0xf];
            output[index++] = HEX_DIGITS[b & 0xf];
        }
        return new String(output);
	}
	
	/**
     * 给输入的字符串加 salt 之后再进行 md5 转换.
     * 
     * @param input
     * @param salt
     * @return
     * @see #getMD5String(String)
     */
    public static String getMD5StringWithSalt(String input, String salt) {
        
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("exception can't be happened: " + e.getMessage());
        }
        byte[] inputBytes;
        byte[] saltBytes;
        try {
            inputBytes = input.getBytes("UTF-8");
            saltBytes = salt.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("exception can't be happened: " + e.getMessage());
        }
        // 将 salt 传给消息摘要
        md.update(saltBytes);
        md.update(inputBytes);
        byte[] tmp = md.digest();
        char[] output = new char[16 * 2];
        int index = 0;
        for (int i = 0; i < 16; i++) {
            byte b = tmp[i];
            output[index++] = HEX_DIGITS[b >> 4 & 0xf];
            output[index++] = HEX_DIGITS[b & 0xf];
        }
        return new String(output);
    }
	
	/**
	 * 对输入的字符串进行 SHA 散列, 返回其 Hex 编码的结果.
	 * 
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String shalToHex(String input) throws NoSuchAlgorithmException {
		byte[] digestResult = digest(input, SHA1);
		return EncoderUtil.hexEncode(digestResult);
	}
	
	/**
	 * 对输入的字符串进行 SHA 散列, 返回其 Base64 编码的结果.
	 * 
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String shalToBase64(String input) throws NoSuchAlgorithmException {
		byte[] digestResult = digest(input, SHA1);
		return EncoderUtil.base64Encode(digestResult);
	}
	
	/**
	 * 对输入的字符串进行 SHA 散列, 返回其 Base64 编码的 URL 安全的结果.
	 * 
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String shalToBase64UrlSafe(String input) throws NoSuchAlgorithmException {
		byte[] digestResult = digest(input, SHA1);
		return EncoderUtil.base64UrlSafeEncode(digestResult);
	}
	
	/**
	 * 对文件进行 SHA 散列, 返回其 Hex 编码的结果.
	 * 
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String shalToHex(InputStream input) throws NoSuchAlgorithmException, IOException {
		return digest(input, SHA1);
	}
	
	/**
	 * 对文件进行散列, 支持 SHA1 和 MD5 算法.
	 * 
	 * @param input
	 * @param algorithm
	 * @return
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 */
	private static String digest(InputStream input, String algorithm) throws NoSuchAlgorithmException, IOException {

		MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		int read = input.read(buffer, 0, bufferSize);
		while (read > -1) {
			messageDigest.update(buffer, 0, read);
			read = input.read(buffer, 0, bufferSize);
		}
		return EncoderUtil.hexEncode(messageDigest.digest());
	}
	
	/**
	 * 对字符串进行散列, 支持 SHA1 和 MD5 算法.
	 * 
	 * @param input
	 * @param algorithm
	 * @return
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchAlgorithmException
	 */
	private static byte[] digest(String input, String algorithm) throws NoSuchAlgorithmException {
		
		return MessageDigest.getInstance(algorithm).digest(input.getBytes());
	}
}
