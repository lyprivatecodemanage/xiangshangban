package com.xiangshangban.transit_service.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xiangshangban.transit_service.bean.Uusers;
import com.xiangshangban.transit_service.service.UusersService;
@RestController
@RequestMapping("/isSavePasswordController")
public class IsSavePasswordController {
	private static final Logger logger = Logger.getLogger(IsSavePasswordController.class);
	
	@Autowired
	private UusersService uusersService;
	
	@RequestMapping(value="/isSavePasswd")
	public Map<String,Object> isSavePasswd(){
		Map<String,Object> result =new HashMap<String,Object>();
		try{
			Subject subject = SecurityUtils.getSubject();
			String phone = subject.getPrincipal().toString();
			Uusers user = uusersService.selectByPhone(phone);
			if(StringUtils.isEmpty(user.getUserpwd())){
				result.put("message", "目前没有设置密码,请设置密码");
				result.put("returnCode", "4026");
				return result;
			}
			result.put("returnCode", "3000");
			return result;
		}catch(Exception e){
			logger.info(e);
			result.put("returnCode", "3001");
			result.put("message", "服务器错误");
			return result;
		}
	}
}
