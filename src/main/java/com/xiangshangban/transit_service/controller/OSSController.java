package com.xiangshangban.transit_service.controller;


import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.transit_service.bean.OSSFile;
import com.xiangshangban.transit_service.bean.ReturnData;
import com.xiangshangban.transit_service.bean.Uusers;
import com.xiangshangban.transit_service.service.CompanyService;
import com.xiangshangban.transit_service.service.OSSFileService;
import com.xiangshangban.transit_service.service.UusersService;
import com.xiangshangban.transit_service.util.OSSFileUtil;


@RestController
@RequestMapping(value = "/oss")
public class OSSController {
	@Autowired
	OSSFileService oSSFileService;
	@Autowired
	UusersService uusersService; 
	@Autowired
	CompanyService companyService;
	/**
	 * 上传文件到OSS
	 * @param file
	 * @param ACCESS_TOKEN 
	 * @param funcDirectory 存储模块名称
	 * @return
	 */
	@RequestMapping(value = "/upload",produces = "application/json;charset=UTF-8",method=RequestMethod.POST)
	public String appUpload(@RequestParam(value="file") MultipartFile file, 
			@RequestParam(value="funcDirectory") String funcDirectory,HttpServletRequest request){ 
		//根据token获得当前用户id,公司id
				String token = request.getHeader("ACCESS_TOKEN");
				Uusers user = new Uusers();
				if (StringUtils.isEmpty(token)) {
					String sessionId = request.getSession().getId();
					System.out.println("redirectController : "+sessionId);
					user = uusersService.selectCompanyBySessionId(sessionId);
				} else {
					user = uusersService.selectCompanyByToken(token);
				}

				if (user == null || StringUtils.isEmpty(user.getCompanyId()) || StringUtils.isEmpty(user.getUserid())) {
					ReturnData returnData = new ReturnData();
					returnData.setReturnCode("3003");
					returnData.setMessage("用户身份获取失败");
					return JSON.toJSONString(returnData);
				}
		
		String companyNo = companyService.selectByPrimaryKey(user.getCompanyId()).getCompany_no();//公司编号，此编号实际应用时，应根据token去查询
		//funcDirectory = "portrait";//portrait目录存储员工头像
		//此处判断上传的funcDirectory是否是固定的几个文件名
		/*boolean funcDirectoryFlag = false;
		String [] funcDirectoryArray = HttpClientUtil.getFuncDirectory();
		for(String directory:funcDirectoryArray){
			if(funcDirectory.equals(directory)){
				funcDirectoryFlag=true;
			}
		}*/
		String ossFileString = "";
		if(StringUtils.isNotEmpty(token) /*&& funcDirectoryFlag*/){
	        if(!file.isEmpty()){		
	        	OSSFile ossFile = oSSFileService.addOSSFile(companyNo, funcDirectory, file);
	        	//System.out.println(JSON.toJSONString(ossFile));
	        	ossFileString = JSON.toJSONString(ossFile);
	        	//将头像key保存
	        	if("protrait".equals(funcDirectory)){
	        		uusersService.updateEmployeeImgUrl(user.getUserid(),user.getCompanyId(),ossFileString);
	        	}
	        	return ossFileString;
	        }
		}
        return null;  
    }
	/**
	 * 根据文件名获取全路径
	 * @param key
	 * @param ACCESS_TOKEN
	 * @return
	 */
	@RequestMapping(value = "/getPath",produces = "application/json;charset=UTF-8",method=RequestMethod.GET)
	public String appGetPath(String key,@RequestParam String funcDirectory,HttpServletRequest request){
		
		//根据token获得当前用户id,公司id
		String token = request.getHeader("ACCESS_TOKEN");
		Uusers user = new Uusers();
		if (StringUtils.isEmpty(token)) {
			String sessionId = request.getSession().getId();
			System.out.println("redirectController : "+sessionId);
			user = uusersService.selectCompanyBySessionId(sessionId);
		} else {
			user = uusersService.selectCompanyByToken(token);
		}

		if (user == null || StringUtils.isEmpty(user.getCompanyId()) || StringUtils.isEmpty(user.getUserid())) {
			ReturnData returnData = new ReturnData();
			returnData.setReturnCode("3003");
			returnData.setMessage("用户身份获取失败");
			return JSON.toJSONString(returnData);
		}
		
		
		
		//Uusers user = uusersService.selectCompanyByToken(token);
		//String customerId = user.getCompanyId();
		//String customerId = "C001";//公司编号，此编号实际应用时，应根据token去查询
		String companyNo = companyService.selectByPrimaryKey(user.getCompanyId()).getCompany_no();//公司编号，此编号实际应用时，应根据token去查询
		//funcDirectory = "portrait";//portrait目录存储员工头像
				//此处判断上传的funcDirectory是否是固定的几个文件名
				/*boolean funcDirectoryFlag = false;
				String [] funcDirectoryArray = HttpClientUtil.getFuncDirectory();
				for(String directory:funcDirectoryArray){
					if(funcDirectory.equals(directory)){
						funcDirectoryFlag=true;
					}
				}*/
		
		String url = OSSFileUtil.getFilePath(companyNo, funcDirectory, key);
		return JSON.toJSONString(url);
	}
} 

