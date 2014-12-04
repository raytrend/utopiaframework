/*
 * ImageUtil.java
 * 
 * Created on 25/11/2011
 */
package cn.raytrend.utopiaframework.core.util.awt.image;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import cn.raytrend.utopiaframework.core.util.lang.StringUtil;

/**
 * 负责对图片进行处理的工具类, 包括裁减图片等功能, 可以应用在上传图片后对图片进行切割等场合.
 * 
 * @author zhouych
 */
public class ImageUtil {

	/**
	 * 对图片进行裁减处理. 经过测试, 发现 (.bmp|.jpg) 文件正常.
	 * 
	 * @param srcImage
	 *            原始图片
	 * @param destImage
	 *            裁减后的图片
	 * @param x
	 *            开始裁减的 x 坐标
	 * @param y
	 *            开始裁减的 y 坐标
	 * @param width
	 *            裁减的宽度
	 * @param height
	 *            裁减的高度
	 * @throws IOException
	 */
	public static void createPreviewImage(String srcImage, String destImage, int x, int y, int width, int height)
			throws IOException {

		// 在内存中生成待处理的图像
		BufferedImage srcBuffImage = ImageIO.read(new BufferedInputStream(new FileInputStream(srcImage)));

		if (srcBuffImage != null) {
			
			int max_width = srcBuffImage.getWidth();	// 图片的宽度
			int max_height = srcBuffImage.getHeight();	// 图片的高度
			if (x < 0 || x > max_width) {
				x = 0;
			}
			if (y < 0 || y > max_height) {
				y = 0;
			}
			if (width < 0 || width > max_width) {
				width = max_width;
			}
			if (height < 0 || height > max_height) {
				height = max_height;
			}
			// 构造图片的裁减区域
			Rectangle rectangle = new Rectangle(x, y, width, height);
			// 获取图像的后缀名，如jpg、jpeg、bmp、png或gif等
			String formatName = StringUtil.getFileExtension(srcImage);
			// 返回相应后缀名的图像解码器
			Iterator<ImageReader> ite = ImageIO.getImageReadersByFormatName(formatName);
			ImageReader reader = ite.next();
			// 获取当前图像文件的图像流
			ImageInputStream iis = ImageIO.createImageInputStream(new FileInputStream(srcImage));
			// 使用该图像解码器来处理该图像流
			reader.setInput(iis, true);
			ImageReadParam param = reader.getDefaultReadParam();
			param.setSourceRegion(rectangle);
			// 获取到的目标
			BufferedImage destBuffImage = reader.read(0, param);
			// 保存新图片
			ImageIO.write(destBuffImage, formatName, new File(destImage));
			// 最后关闭文件流
			if (iis != null) {
				iis.close();
			}
		}
	}
}
