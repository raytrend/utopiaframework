/*
 * CompressUtil.java
 * 
 * Created on 25/11/2011
 */
package cn.raytrend.utopiaframework.web.util.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * 提供各种压缩和解压缩方法的工具类, 比如 zip, gzip 等.
 * 
 * @author zhouych
 */
public class CompressUtil {

	/**
	 * 采用 GZip 对数据进行压缩, Web 环境可以使用, 服务器端压缩数据后返回客户端由浏览器进行解析.
	 * 
	 * @param data
	 *            待压缩的字节数组数据
	 * @param output
	 *            字节缓冲区
	 * @return
	 */
	public static byte[] gzipCompress(byte[] data, ByteArrayOutputStream output) {
		
		GZIPOutputStream gzipOutput = null;
		try {
			gzipOutput = new GZIPOutputStream(output);
			gzipOutput.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (gzipOutput != null) {
				try {
					gzipOutput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return output.toByteArray();
	}
}
