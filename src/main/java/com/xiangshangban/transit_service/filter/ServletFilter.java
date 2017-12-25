package com.xiangshangban.transit_service.filter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.xiangshangban.transit_service.bean.UniqueLogin;
import com.xiangshangban.transit_service.service.UniqueLoginService;
import com.xiangshangban.transit_service.service.UserCompanyService;
import com.xiangshangban.transit_service.service.UusersRolesService;
import com.xiangshangban.transit_service.service.UusersService;
import com.xiangshangban.transit_service.util.HttpClientUtil;
import com.xiangshangban.transit_service.util.RedisUtil;

@Component
public class ServletFilter implements Filter {

	private UniqueLoginService uniqueLoginService;

	private UusersRolesService uusersRolesService;

	private UusersService usersService;

	private UserCompanyService userCompanyService;

	public static void uploadFile(byte[] file, String filePath, String fileName) throws Exception {
		File targetFile = new File(filePath);
		if (!targetFile.exists()) {
			targetFile.mkdirs();
		}
		FileOutputStream out = new FileOutputStream(filePath + fileName);
		out.write(file);
		out.flush();
		out.close();
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		ServletContext sc = filterConfig.getServletContext();
		WebApplicationContext cxt = WebApplicationContextUtils.getWebApplicationContext(sc);
		if (cxt != null && cxt.getBean("uniqueLoginService") != null && uniqueLoginService == null)
			uniqueLoginService = (UniqueLoginService) cxt.getBean("uniqueLoginService");

		if (cxt != null && cxt.getBean("uusersRolesService") != null && uusersRolesService == null)
			uusersRolesService = (UusersRolesService) cxt.getBean("uusersRolesService");

		if (cxt != null && cxt.getBean("usersService") != null && usersService == null)
			usersService = (UusersService) cxt.getBean("usersService");

		if (cxt != null && cxt.getBean("userCompanyService") != null && userCompanyService == null)
			userCompanyService = (UserCompanyService) cxt.getBean("userCompanyService");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String url = req.getRequestURL().toString();
		String uri = req.getRequestURI();
		res.setContentType("textml;charset=UTF-8");
		// 这里填写你允许进行跨域的主机ip
		res.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
		res.setHeader("Access-Control-Allow-Credentials", "true");
		// 允许的访问方法
		res.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, PATCH");
		/* res.setHeader("Access-Control-Allow-Methods", req.getMethod()); */
		// Access-Control-Max-Age 用于 CORS 相关配置的缓存
		res.setHeader("Access-Control-Max-Age", "3600");
		// Content-Disposition文件下载时设置文件名
		res.setHeader("Access-Control-Allow-Headers",
				"Origin, No-Cache, X-Requested-With, If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type, type, REDIRECT,CONTENTPATH");
		res.setHeader("XDomainRequestAllowed", "1");
		// res.setHeader("Content-Disposition",
		// "attachment;filename="+java.net.URLEncoder.encode("报表.xls","UTF-8"));
		// res.setHeader("Access-Control-Allow-Methods", req.getMethod());
		boolean flag = true;
		boolean redirect = false;
		String redirectUrl = "";
		boolean uriFlag = true;
		if (!"OPTIONS".equals(req.getMethod())) {
			System.out.println("servletFilter\t:"+req.getMethod());
			System.out.println("servletFilter\t:"+uri);
			System.out.println("servletFilter\t:"+"session\t:"+req.getSession().getId());
			if (flag) {
				String[] includeMode = HttpClientUtil.getIncludeMode();
				for (String mode : includeMode) {
					String checkUrl = "/api/" + mode + "/";
					if (uri.contains(checkUrl)) {
						String sendurl = URLDecoder.decode(uri.replaceAll(checkUrl, ""), "UTF-8");
						if (uri.contains("/export/")) {
							redirectUrl = "/redirectApi/exportRequest?redirectUrl=" + sendurl + "&redirectMode=" + mode;
						} else {
							redirectUrl = "/redirectApi/sendRequest?redirectUrl=" + sendurl + "&redirectMode=" + mode;
						}
						redirect = true;
					}
				}
			}
			if (redirect) {
				req.getRequestDispatcher(redirectUrl).forward(req, res);
			} else {
				chain.doFilter(req, res);
				System.err.println("过滤器结束");
			}
		}
	}

	public void destroy() {
	}
}
