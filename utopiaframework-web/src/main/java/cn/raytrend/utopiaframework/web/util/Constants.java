package cn.raytrend.utopiaframework.web.util;

import java.text.SimpleDateFormat;

public class Constants {
    
    /**
     * 目录命名格式: yyyyMM
     */
    public static final SimpleDateFormat FORMATTER_DIR = new SimpleDateFormat("yyyyMM");

    /**
     * 文件命名格式: yyyyMMddHHmmssSSS
     */
    public static final SimpleDateFormat FORMATTER_FILE = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    
    /**
     * 文件上传路径
     */
    public static final String PATH_UPLOAD_FILE = "/uploadfiles/";

    /**
     * 用于 HTTP session 中保存的验证码, 见 org.mysterylab.utopiaframework.web.servlet.ImageCaptchaServlet
     */
    public final static String HTTP_SESSION_CAPTCHA_CODE = "captcha_code";
}
