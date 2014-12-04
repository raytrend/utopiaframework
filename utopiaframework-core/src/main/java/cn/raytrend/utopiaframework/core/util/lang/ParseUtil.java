/*
 * ParseUtil.java
 * 
 * Created on 25/11/2011
 */
package cn.raytrend.utopiaframework.core.util.lang;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.raytrend.utopiaframework.core.util.Constants;

/**
 * 提供了用于解析、转换字符串、时间等的方法.
 * 
 * @author zhouych
 */
public class ParseUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(ParseUtil.class);
	
	/**
	 * 删除 input 字符串中的 html 格式并截取某一部分长度返回.
	 * 
	 * @param input
	 *            需要删除其中的 html 格式的字符串
	 * @param length
	 *            需要获取到的字符串的长度
	 * @return
	 */
    @Deprecated
	public static String parseHtml2String(String input, int length) {
		if (StringUtils.isBlank(input)) {
			return "";
		}
		// 去掉所有html元素,
		String str = input.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "");
		str = str.replaceAll("[(/>)<]", "");
		int len = str.length();
		if (len <= length) {
			return str;
		} else {
			str = str.substring(0, length);
			str += "...";
		}
		return str;
	}
    
    /**
     * 将时间的 string 类型转换成 Date 类型, 如果转换失败则返回 null.
     * 
     * @param dateString
     * @return
     */
    public static Date parseSQLDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.SQL_TIMESTAMP);
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            // cannot be happened
        }
        return null;
    }
	
	/**
	 * 解析日期, 返回指定的日期表示形式, 比如 'yyyy年mm月dd日' 或 'yyyy-MM-dd' 等.
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String parseDate(Date date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}
	
	/**
     * 将文件对象转换成二进制流. WARN: 该方法无经过测试, 有可能导致内存回收困难.
     * 
     * @param file
     * @return
     * @throws IOException 
     */
    public static byte[] getBytesFromFile(File file) throws IOException {
        if (file == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            int index = -1;
            while ((index = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, index);
            }
        } catch (Exception e) {
            logger.error("write obj error, {}", e.getMessage());
            return null;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return baos.toByteArray();
    }
    
    /**
     * 将输入流转换成二进制流. WARN: 该方法无经过测试, 有可能导致内存回收困难.
     * 
     * @param input
     * @param fileSize
     * @return
     * @throws IOException
     */
    public static byte[] getBytesFromInputStream(InputStream input, int fileSize) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("inputsream cann't be null");
        }
        byte[] data = new byte[fileSize];
        try {
            input.read(data);
        } catch (IOException e) {
            logger.error("write obj error, {}", e.getMessage());
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return data;
    }
}
