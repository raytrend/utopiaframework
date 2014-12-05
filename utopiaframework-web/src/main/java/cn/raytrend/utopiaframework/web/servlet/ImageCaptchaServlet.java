/*
 * ImageCaptchaServlet.java
 * 
 * Created on 29/11/2011
 */
package cn.raytrend.utopiaframework.web.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.raytrend.utopiaframework.web.util.Constants;

/**
 * 生成图片验证码的 Servlet. 在 web.xml 文件中可以配置如下:
 * <pre>
 * {@code
 * <servlet>
 *     <servlet-name>imageServlet</servlet-name><br>
 *     <servlet-class>org.mysterylab.utopiaframework.web.servlet.ImageCaptchaServlet</servlet-class><br>
 * </servlet>
 * <servlet-mapping>
 *     <servlet-name>imageServlet</servlet-name>
 *     <url-pattern>/servlet/image.jpg</url-pattern>
 * </servlet-mapping>
 * }
 * </pre>
 * 生成验证码成功后会将验证码赋给 session 的 {@link Constants#HTTP_SESSION_CAPTCHA_CODE} 变量中.
 * 
 * @author zhouych
 * @see Constants#HTTP_SESSION_CAPTCHA_CODE
 */
@SuppressWarnings("serial")
public class ImageCaptchaServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		// 采用headless工作模式
		System.setProperty("java.awt.headless", "true");

	    //设置页面不缓存和返回内容格式
	    response.setHeader("Pragma", "No-cache");
	    response.setHeader("Cache-Control", "no-cache");
	    response.setDateHeader("Expires", 0);
	    response.setContentType("image/jpeg");

	    // 在内存中创建图象
	    int width = 55, height = 18;
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

	    // 获取图形上下文
	    Graphics g = image.getGraphics();

	    //生成随机类
	    Random random = new Random();

	    // 设定背景色
	    g.setColor(new Color(255, 255, 255));
	    g.fillRect(0, 0, width, height);

	    //设定字体
	    g.setFont(new Font("Lucida Console", Font.PLAIN, 14));

	    // 随机产生155条干扰线, 使图象中的认证码不易被其它程序探测到(当然也可以将其注释掉)
	    g.setColor(getRandColor(160, 200));
	    for (int i = 0; i < 155; i++) {
	        int x = random.nextInt(width);
	        int y = random.nextInt(height);
	        int xl = random.nextInt(12);
	        int yl = random.nextInt(12);
	        g.drawLine(x, y, x + xl, y + yl);
	    }

	    // 取随机产生的认证码(4位数字)
	    String sRand = "";
		String[] ychar = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
				"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
				"Y", "Z" };
	    for (int i = 0; i < 4; i++) {
	        String rand = ychar[random.nextInt(36)];
	        sRand += rand;
	        // 将认证码显示到图象中
	        g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
	        // 调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
	        g.drawString(rand, 13 * i + 6, 16);
	    }

	    // 将认证码存入SESSION
	    request.getSession().setAttribute(Constants.HTTP_SESSION_CAPTCHA_CODE, sRand);

	    // 图象生效
	    g.dispose();

	    //输出图象到页面
	    ImageIO.write(image, "JPEG", response.getOutputStream());
	}
	
	/**
	 * 给定范围获得随机颜色
	 * 
	 * @param fc
	 * @param bc
	 * @return
	 */
	private Color getRandColor(int fc, int bc) {
		Random random = new Random();
		if (fc > 255) {
			fc = 255;
		}
		if (bc > 255) {
			bc = 255;
		}
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}
