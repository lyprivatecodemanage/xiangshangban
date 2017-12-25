package com.xiangshangban.transit_service.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import com.xiangshangban.transit_service.util.RedisUtil;

/**
 * @author 李业 /认证拦截器
 * @author cachee
 *
 */
public class CustomFormAuthenticationFilter extends FormAuthenticationFilter {
	
	private static final Logger log = Logger.getLogger(CustomFormAuthenticationFilter.class);
	
	@Override
	public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse)response;
		RedisUtil redis = RedisUtil.getInstance();
		String type = req.getHeader("type");
		String phone = "";
		//boolean flag = true;//账号是否在别处登录的标志
		if(StringUtils.isNotEmpty(type) && "0".equals(type)){
			String sessionId = req.getSession().getId();
			phone = redis.getJedis().hget(sessionId,"session");
			String sessionCache = redis.getJedis().hget("session"+phone, "session");
			if(StringUtils.isNotEmpty(sessionCache) ){
				if(sessionId.equals(sessionCache)){
					return isAccessAllowed(request, response, mappedValue) || onAccessDenied(request, response, mappedValue);
				}else{
					res.setCharacterEncoding("UTF-8");
					res.getWriter().println("账号在别处登录!");
					return false;
				}
			}
		}
		if(StringUtils.isNotEmpty(type) && "1".equals(type)){
			String token = req.getHeader("token");
			String clientId = req.getHeader("clientId");
			//登录不要传token
			if(StringUtils.isNotEmpty(token)){
				phone = redis.getJedis().hget(token, "token");
				String tokenClientIdCache = redis.getJedis().hget("token"+phone, "token");
				if(StringUtils.isNotEmpty(tokenClientIdCache)){
					if(tokenClientIdCache.equals(clientId)){
						return isAccessAllowed(request, response, mappedValue) || onAccessDenied(request, response, mappedValue);
					}else{
						res.setCharacterEncoding("UTF-8");
						res.getWriter().println("账号在别处登录!");
						return false;
					}
				}
			}
		}
	        return isAccessAllowed(request, response, mappedValue) || onAccessDenied(request, response, mappedValue);
	    }
	
	protected boolean onAccessDenied(ServletRequest request,
			ServletResponse response) throws Exception {
			boolean flag = super.onAccessDenied(request, response);
			return flag;
	
	}
	 @Override
	    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response)
	            throws Exception {
	        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
	        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
	        String url = this.getSuccessUrl();
	        httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + url);    //页面跳转
	        return false;
	    }
	@Override
	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
		String username = getUsername(request);
		String password = getPassword(request);
		HttpServletRequest req = (HttpServletRequest)request;
		String type = req.getHeader("type");
		String token = req.getHeader("token");
		String clientId = req.getHeader("clientId");
		String phone = "";
		RedisUtil redis = RedisUtil.getInstance();
		if("1".equals(type)){
			if(StringUtils.isEmpty(username)&&StringUtils.isEmpty(password)){
				username = clientId;
				password = token;
			}
		}
		
       return createToken(username, password, request, response);
   }
		
}
