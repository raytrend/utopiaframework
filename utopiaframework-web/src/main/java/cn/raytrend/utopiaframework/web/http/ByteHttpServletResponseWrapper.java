/*
 * ByteHttpServletResponseWrapper.java
 * 
 * Created on 29/11/2011
 */
package cn.raytrend.utopiaframework.web.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


import cn.raytrend.utopiaframework.web.filter.ETagFilter;
import cn.raytrend.utopiaframework.web.filter.GzipFilter;
import cn.raytrend.utopiaframework.web.http.ByteServletOutputStream;

/**
 * 用 {@link ByteArrayOutputStream} 来包装 {@link HttpServletResponseWrapper}, 加入缓冲机制.
 * 
 * @author zhouych
 * @see ETagFilter
 * @see GzipFilter
 */
public class ByteHttpServletResponseWrapper extends HttpServletResponseWrapper {

	//-- 该 response 包装器的输出方式 --//
	
	public static final int TYPE_OUT_NONE	= 0;
	
	public static final int TYPE_OUT_WRITER	= 1;
	
	public static final int TYPE_OUT_STREAM	= 2;
	
	/**
	 * 输出类型
	 */
	private int outputType = TYPE_OUT_NONE;
	
	/**
	 * 二进制流输出
	 */
	private ByteServletOutputStream out = null;
	
	/**
	 * 文本流输出
	 */
	private PrintWriter writer = null;
	
	/**
	 * 缓冲区
	 */
	private ByteArrayOutputStream buffer;
	
	public ByteHttpServletResponseWrapper(HttpServletResponse response) {
		super(response);
		buffer = new ByteArrayOutputStream();
	}
	
	public ByteHttpServletResponseWrapper(HttpServletResponse response, int bufferSize) {
		super(response);
		buffer = new ByteArrayOutputStream(bufferSize);
	}
	
	@Override
	public PrintWriter getWriter() throws IOException {
		
		if (outputType == TYPE_OUT_STREAM) {
			throw new IllegalStateException();
		}
		if (outputType == TYPE_OUT_WRITER) {
			return writer;
		}
		outputType = TYPE_OUT_WRITER;
		writer =  new PrintWriter(new OutputStreamWriter(buffer, getCharacterEncoding()));
		return writer;
	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		
		if (outputType == TYPE_OUT_WRITER) {
			throw new IllegalStateException();
		}
		if (outputType == TYPE_OUT_STREAM) {
			return out;
		}
		outputType = TYPE_OUT_STREAM;
		out = new ByteServletOutputStream(buffer);
		return out;
	}
	
	@Override
	public void flushBuffer() throws IOException {
		if (outputType == TYPE_OUT_STREAM) {
			out.flush();
		}
		if (outputType == TYPE_OUT_WRITER) {
			writer.flush();
		}
	}
	
	@Override
	public void reset() {
		outputType = TYPE_OUT_NONE;
		buffer.reset();
	}
	
	/**
	 * 获取服务器响应的数据.
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] getResponseData() throws IOException {
		return getBuffer().toByteArray();
	}

	/**
	 * 获取当前包装器的数据输出类型.
	 * 
	 * @return
	 */
	public int getOutputType() {
		return outputType;
	}

	/**
	 * 获取缓冲区.
	 * 
	 * @return
	 */
	public ByteArrayOutputStream getBuffer() {
		return buffer;
	}
}
