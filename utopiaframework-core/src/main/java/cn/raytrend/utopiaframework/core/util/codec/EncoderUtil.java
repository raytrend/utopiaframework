/*
 * EncoderUtil.java
 * 
 * Created on 25/11/2011
 */
package cn.raytrend.utopiaframework.core.util.codec;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * 各种格式的编解码工具类, 主要集成了 commons-codec 和 JDK 提供的编解码方法.
 * 
 * @author zhouych
 */
public class EncoderUtil {

	/**
	 * 默认的编码为 UTF-8
	 */
	private static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * Hex 编码.
	 * 
	 * @param input
	 *            待编码的字节流
	 * @return
	 */
	public static String hexEncode(byte[] input) {
		return Hex.encodeHexString(input);
	}

	/**
	 * Hex 解码.
	 * 
	 * @param input
	 *            待解码的字符串
	 * @return
	 */
	public static byte[] hexDecode(String input) {
		try {
			return Hex.decodeHex(input.toCharArray());
		} catch (DecoderException e) {
			throw new IllegalStateException("Hex Decoder exception", e);
		}
	}
	
	//-- base64 编解码相关 --//
	
	/**
	 * Base64 编码, 以指定的字符编码方式进行.
	 * 
	 * @param input
	 * @param charsetName
	 * @return
	 */
	public static String base64Encode(String input, String charsetName) {
		try {
			return new String(Base64.encodeBase64(input.getBytes(charsetName)));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("charse " + charsetName + " cannot be supported.");
		}
	}
	
	/**
	 * Base64 编码, 默认以 utf-8 格式进行编码.
	 * 
	 * @param input
	 *            待编码的字符串
	 * @return
	 */
	public static String base64Encode(String input) {
		return base64Encode(input, DEFAULT_ENCODING);
	}
	
	/**
	 * Base64 编码.
	 * 
	 * @param input
	 *            待编码的字节流
	 * @return
	 */
	public static String base64Encode(byte[] input) {
		return new String(Base64.encodeBase64(input));
	}
	
	
	
	/**
	 * Base64 编码, URL 安全(将 Base64 中的 URL 非法字符如 +/= 转为其他字符, 详见 RFC3548).
	 * 
	 * @param input
	 *            待编码的字节流
	 * @return
	 */
	public static String base64UrlSafeEncode(byte[] input) {
		return Base64.encodeBase64URLSafeString(input);
	}

	/**
	 * Base64解码.
	 * 
	 * @param input
	 *            待解码的字符串
	 * @return
	 */
	public static byte[] base64Decode(String input) {
		return Base64.decodeBase64(input);
	}

	/**
	 * URL 编码, Encode 默认为 UTF-8.
	 * 
	 * @param input
	 *            待编码的字符串
	 * @return
	 */
	public static String urlEncode(String input) {
		try {
			return URLEncoder.encode(input, DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Unsupported Encoding Exception", e);
		}
	}
	
	/**
	 * URL 解码, Encode 默认为 UTF-8.
	 * 
	 * @param input
	 *            待解码的字符串
	 * @return
	 */
	public static String urlDecode(String input) {
		try {
			return URLDecoder.decode(input, DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Unsupported Encoding Exception", e);
		}
	}

	/**
	 * HTML 转码.
	 * 
	 * @param html
	 *            待转码的 HTML 字符串
	 * @return
	 */
	public static String html4Escape(String html) {
		return StringEscapeUtils.escapeHtml4(html);
	}

	/**
	 * HTML 解码.
	 * 
	 * @param htmlEscaped
	 *            待解码的 HTML 字符串
	 * @return
	 */
	public static String html4Unescape(String htmlEscaped) {
		return StringEscapeUtils.unescapeHtml4(htmlEscaped);
	}

	/**
	 * XML 转码.
	 * 
	 * @param xml
	 *            待转码的 XML 字符串
	 * @return
	 */
	public static String xmlEscape(String xml) {
		return StringEscapeUtils.escapeXml(xml);
	}

	/**
	 * XML 解码.
	 * 
	 * @param xmlEscaped
	 *            待解码的 XML 字符串
	 * @return
	 */
	public static String xmlUnescape(String xmlEscaped) {
		return StringEscapeUtils.unescapeXml(xmlEscaped);
	}
}
