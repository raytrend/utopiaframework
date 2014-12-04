/*
 * StringUtil.java
 * 
 * Created on 25/11/2011
 */
package cn.raytrend.utopiaframework.core.util.lang;

/**
 * 提供了一些处理字符串的方法.
 * 
 * @author zhouych
 */
public class StringUtil {

    /**
     * 返回文件的后缀名, 如果文件没有后缀名则返回 "".
     * 
     * @param fileName
     *             文件名
     * @return
     */
    public static String getFileExtension(String fileName) {
        if(fileName == null || fileName.length() == 0) {
            return "";
        }
        int index = fileName.indexOf(".");
        return index < 0 ? "" : fileName.substring(index + 1);
    }
    
    /**
     * 获取文件名的方法, 不带后缀名. 注意可能有 hack.jsp.txt 这种文件名的格式.
     */
    public static String getFileNameWithoutExtension(String fileName) {
        return fileName.substring(0, fileName.indexOf("."));
    }
}
