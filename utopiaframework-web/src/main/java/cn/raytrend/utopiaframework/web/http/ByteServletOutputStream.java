/*
 * ByteServletOutputStream.java
 * 
 * Created on 29/11/2011
 */
package cn.raytrend.utopiaframework.web.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

/**
 * 用 {@link ByteArrayOutputStream} 来包装 {@link ServletOutputStream}, 加入缓冲机制. 这样可以将内容以字节码重新包装并输出.
 * 
 * @author zhouych
 * @see ByteHttpServletResponseWrapper
 */
public class ByteServletOutputStream extends ServletOutputStream {

	private ByteArrayOutputStream buffer;
	
	public ByteServletOutputStream(ByteArrayOutputStream buffer) {
		this.buffer = buffer;
	}
	
	@Override
	public void write(int b) throws IOException {
		buffer.write(b);
	}
}
