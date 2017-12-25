package com.xiangshangban.transit_service.listener;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.web.context.support.WebApplicationContextUtils;

import com.xiangshangban.transit_service.bean.UniqueLogin;
import com.xiangshangban.transit_service.service.LoginService;
import com.xiangshangban.transit_service.service.UniqueLoginService;


@WebListener
public class SessionListener implements HttpSessionListener {  
	

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		//System.out.println("==================>session创建"+se.getSession().getId());
		/* UniqueLoginService uniqueLoginService = WebApplicationContextUtils.getWebApplicationContext(se.getSession().getServletContext()).getBean(UniqueLoginService.class);
		 UniqueLogin uniqueLogin = uniqueLoginService.selectBySessionId(se.getSession().getId());
		 System.out.println(uniqueLogin);*/
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		//System.out.println("==================>session清除"+se.getSession().getId());
		 LoginService loginService = WebApplicationContextUtils.getWebApplicationContext(se.getSession().getServletContext()).getBean(LoginService.class);
		 int i = loginService.deleteById(loginService.selectBySessionId(se.getSession().getId()).getId());
		 if(i>0){
		//System.out.println("session删除记录 :\t"+i+"\t"+se.getSession().getId());
		 }
	    }

}


  
