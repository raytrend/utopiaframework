/*
 * CKEditor3UploadServlet.java
 * 
 * Created on 29/11/2011
 */
package cn.raytrend.utopiaframework.web.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.raytrend.utopiaframework.core.util.lang.StringUtil;
import cn.raytrend.utopiaframework.web.util.Constants;

/**
 * 实现 CKEditor 3.x 的附件, 图片和 Flash 三种资源上传功能的 Servlet. 使用的方法非常简单, 首先在 web.xml 中声明如下:
 * <pre>
 * {@code
 * <servlet>
 *     <servlet-name>ckeditorUploader</servlet-name>
 *     <servlet-class>org.mysterylab.utopiaframework.web.servlet.CKEditor3UploadServlet</servlet-class>
 *     <init-param>
 *         <param-name>baseDir</param-name>
 *         <param-value>/uploadfiles/</param-value>
 *     </init-param>
 *     <init-param>
 *         <param-name>debug</param-name>
 *         <param-value>true</param-value>
 *     </init-param>
 *     <init-param>
 *         <param-name>allowedExtensionsFile</param-name>
 *         <param-value>rar|zip|doc|docx|xsl|ppt</param-value>
 *     </init-param>
 *     <init-param>
 *         <param-name>deniedExtensionsFile</param-name>
 *         <param-value></param-value>
 *     </init-param>
 *     <init-param>
 *         <param-name>allowedExtensionsImage</param-name>
 *         <param-value>jpg|jpeg|gif|png|bmp</param-value>
 *     </init-param>
 *     <init-param>
 *         <param-name>deniedExtensionsImage</param-name>
 *         <param-value></param-value>
 *     </init-param>
 *     <init-param>
 *         <param-name>allowedExtensionsFlash</param-name>
 *         <param-value>swf|fla</param-value>
 *     </init-param>
 *     <init-param>
 *         <param-name>deniedExtensionsFlash</param-name>
 *         <param-value></param-value>
 *     </init-param>
 * </servlet>
 * <servlet-mapping>
 *     <servlet-name>ckeditorUploader</servlet-name>
 *     <url-pattern>/ckeditor/upload</url-pattern>
 * </servlet-mapping>
 * }
 * </pre>
 * 
 * 然后在使用到 CKEditor 的 jsp 页面上声明如下:
 * 
 * <pre>
 * {@code
 * <textarea id="content" name="content" cols="80">
 * </textarea>
 * <script type="text/javascript">
 *     // 用 CKEditor API 来替换原来的 HTML 节点
 *     CKEDITOR.replace('content', {
 *         filebrowserUploadUrl : '<c:url value="/ckeditor/upload?Type=File"/>',
 *         filebrowserImageUploadUrl : '<c:url value="/ckeditor/upload?Type=Image"/>',
 *         filebrowserFlashUploadUrl : '<c:url value="/ckeditor/upload?Type=Flash"/>'
 *     });
 * </script>
 * }
 * </pre>
 * 
 * 这里指出一点的是, 用 HTTP POST 方法传输大数据的时候效率非常差, 这个时候可以考虑下 WebDAV.
 * 
 * @author zhouych
 */
@SuppressWarnings("serial")
public class CKEditor3UploadServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(CKEditor3UploadServlet.class);

	/**
	 * 上传文件的根目录, 默认为 webapp/uploadfiles
	 */
	private static String baseDir;

	/**
	 * 是否 debug 模式, 是的话则输出调试信息
	 */
	private static boolean debug = false;
	
	/**
	 * 被允许上传的文件的后缀名, 比如 Flash 只允许上传 swf 和 fla 格式
	 */
	private static Map<String, List<String>> allowedExtensions = new LinkedHashMap<String, List<String>>(3);
	
	/**
	 * 被阻止上传的文件的后缀名, 比如 File 需要限制 jsp 和 asp 等格式文件的上传
	 */
	private static Map<String, List<String>> deniedExtensions = new LinkedHashMap<String, List<String>>(3);

	/**
	 * Servlet初始化方法
	 */
	@Override
	public void init() throws ServletException {
		
		// 从 web.xml 文件中读取配置属性
		debug = (new Boolean(getInitParameter("debug"))).booleanValue();
		baseDir = getInitParameter("baseDir");
		if (baseDir == null) {
			baseDir = "/uploadfiles/";
		}
		// 在其目录前加一个 ckeditor3 的目录
		baseDir = baseDir + "ckeditor3/";
		// 完整的服务器文件上传路径
		String realBaseDir = getServletContext().getRealPath(baseDir);
		File baseFile = new File(realBaseDir);
		if (!baseFile.exists()) {
			baseFile.mkdirs();
		}
		
		// 载入允许文件列表
		allowedExtensions.put("File", stringToArrayList(this.getInitParameter("allowedExtensionsFile")));
		allowedExtensions.put("Image", stringToArrayList(this.getInitParameter("allowedExtensionsImage")));
		allowedExtensions.put("Flash", stringToArrayList(this.getInitParameter("allowedExtensionsFlash")));
		
		// 载入拒绝文件列表
		deniedExtensions.put("File", stringToArrayList(this.getInitParameter("deniedExtensionsFile")));
		deniedExtensions.put("Image", stringToArrayList(this.getInitParameter("deniedExtensionsImage")));
		deniedExtensions.put("Flash", stringToArrayList(this.getInitParameter("deniedExtensionsFlash")));
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	    response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter out = response.getWriter();
        // 从请求参数中获取上传文件的类型：File/Image/Flash
        String typeStr = request.getParameter("Type");
        if (typeStr == null) {
            typeStr = "Image";
        }
        Date now = new Date();
        // 设定上传文件路径
        String currentPath = baseDir + typeStr + "/" + Constants.FORMATTER_DIR.format(now);
        // 获得web应用的上传路径
        String currentDirPath = getServletContext().getRealPath(currentPath);
        // 判断文件夹是否存在，不存在则创建
        File dirTest = new File(currentDirPath);
        if (!dirTest.exists()) {
            dirTest.mkdirs();
        }
        // 将路径前加上web应用名
        currentPath = request.getContextPath() + currentPath;
        // 文件名和文件真实路径
        String fileUrl = "";

        // 使用 Apache Common 组件中的 fileupload 进行文件上传
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        try {
            List<FileItem> items = upload.parseRequest(request);
            Map<String, Object> fields = new HashMap<String, Object>();
            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                // 判断当前项目是普通表单项目还是上传文件
                if (item.isFormField()) {
                    fields.put(item.getFieldName(), item.getString());
                } else {
                    fields.put(item.getFieldName(), item);
                }
            }
            // CEKditor中 file 域的 name 值是 upload
            FileItem uplFile = (FileItem) fields.get("upload");
            // 获取文件名并做处理
            String fileNameLong = uplFile.getName();
            fileNameLong = fileNameLong.replace('\\', '/');
            String[] pathParts = fileNameLong.split("/");
            String fileName = pathParts[pathParts.length - 1];
            // 获取文件扩展名
            String ext = StringUtil.getFileExtension(fileName);
            // 设置上传文件名
            fileName = Constants.FORMATTER_FILE.format(now) + "." + ext;
            File pathToSave = new File(currentDirPath, fileName);
            fileUrl = currentPath + "/" + fileName;
            if (extIsAllowed(typeStr, ext)) {
                uplFile.write(pathToSave);
                if (debug) {
                    logger.info("文件上传成功");
                }
            } else {
                if (debug) {
                    logger.info("无效的文件类型 {}", ext);
                }
            }
        } catch (Exception ex) {
            if (debug) {
                ex.printStackTrace();
            }
        }

        // CKEditorFuncNum 是回调时显示的位置, 这个参数必须有
        String callback = request.getParameter("CKEditorFuncNum");
        out.println("<script type=\"text/javascript\">");
        out.println("window.parent.CKEDITOR.tools.callFunction(" + callback
                + ",'" + fileUrl + "',''" + ")");
        out.println("</script>");
        out.flush();
        out.close();
	}

	/**
	 * 判断扩展名是否允许的方法.
	 * 
	 * @param fileType
	 * @param fileExt
	 * @return
	 */
	private boolean extIsAllowed(String fileType, String fileExt) {

		fileExt = fileExt.toLowerCase();
		List<String> allowedList = allowedExtensions.get(fileType);
		List<String> deniedList = deniedExtensions.get(fileType);
		// 如果当前 list 包括被限制的后缀名, 则返回 false
		if (allowedList.size() == 0) {
			if (deniedList.contains(fileExt)) {
				return false;
			}
			return true;
		}
		if (deniedList.size() == 0) {
			if (allowedList.contains(fileExt)) {
				return true;
			}
			return false;
		}
		// 如果两个 list 都没有数值, 默认允许
		return true;
	}
	
	/**
	 * 将字符串转换成字符串数组, 比如: htm|jsp|jspx, 转换成: htm, jsp, jspx 的数组 list.
	 * 
	 * @param deniedExtensionsStr
	 * @return
	 */
	private ArrayList<String> stringToArrayList(String deniedExtensionsStr) {
		
		if (StringUtils.isNotBlank(deniedExtensionsStr)) {
			String[] deniedExtensionsStrArray = StringUtils.split("|");
			ArrayList<String> tmpList = new ArrayList<String>();
			for (String str : deniedExtensionsStrArray) {
				tmpList.add(str.toLowerCase());
			}
			return tmpList;
		}
		return new ArrayList<String>(0);
	}
}
