package com.xiangshangban.transit_service.filter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebFilter(filterName="ServletFilter",urlPatterns="/*")
public class ServletFilter implements Filter{

	public static void uploadFile(byte[] file, String filePath, String fileName) throws Exception { 
        File targetFile = new File(filePath);  
        if(!targetFile.exists()){    
            targetFile.mkdirs();    
        }       
        FileOutputStream out = new FileOutputStream(filePath+fileName);
        out.write(file);
        out.flush();
        out.close();
    }
	
	
	public void init(FilterConfig filterConfig) throws ServletException {
		System.out.println("进来了");
	}


	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req=(HttpServletRequest) request;
		HttpServletResponse res=(HttpServletResponse) response;
		/*res.setHeader("Access-Control-Allow-Credentials","true");
		res.setHeader("Access-Control-Allow-Origin","http://192.168.0.114:8000");*/
		 //这里填写你允许进行跨域的主机ip
		res.setHeader("Access-Control-Allow-Origin", "*");
        //允许的访问方法
		res.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, PATCH");
        //Access-Control-Max-Age 用于 CORS 相关配置的缓存
		res.setHeader("Access-Control-Max-Age", "3600");
		res.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
		chain.doFilter(req, res);
		System.out.println("嘻嘻嘻");
	}

	public void destroy() {
	}

}
