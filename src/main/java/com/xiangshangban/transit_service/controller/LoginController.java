package com.xiangshangban.transit_service.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.transit_service.bean.Company;
import com.xiangshangban.transit_service.bean.Login;
import com.xiangshangban.transit_service.bean.Uroles;
import com.xiangshangban.transit_service.bean.Uusers;
import com.xiangshangban.transit_service.service.CompanyService;
import com.xiangshangban.transit_service.service.LoginService;
import com.xiangshangban.transit_service.service.UserCompanyService;
import com.xiangshangban.transit_service.service.UusersRolesService;
import com.xiangshangban.transit_service.service.UusersService;
import com.xiangshangban.transit_service.util.FormatUtil;
import com.xiangshangban.transit_service.util.PropertiesUtils;
import com.xiangshangban.transit_service.util.RedisUtil;
import com.xiangshangban.transit_service.util.YtxSmsUtil;
import com.xiangshangban.transit_service.util.RedisUtil.Hash;
@RestController
@RequestMapping("/loginController")
public class LoginController {
	private static final Logger logger = Logger.getLogger(LoginController.class);
	@Autowired
	private LoginService loginService;
	@Autowired
	private UusersService uusersService;
	@Autowired
	CompanyService companyService;
	@Autowired
	private UusersRolesService uusersRolesService;
	@Autowired
	UserCompanyService userCompanyService;
	/**
	 * @author 李业/获取二维码
	 * @param session
	 * @return
	 */
	@RequestMapping("/getQrcode")
	public Map<String, Object> getQrcode(String type,HttpSession session,HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			String qrcode = "";
			// 登录
			if (Integer.valueOf(type) == 0) {
				String sessionId = session.getId();
				// 产生二维码(UUID)
				qrcode = FormatUtil.createUuid();
				RedisUtil redis = RedisUtil.getInstance();
				// 将二位码存入redis,设置有效时间300秒
				redis.new Hash().hset("qrcode_" + qrcode, "qrcode", qrcode);
				redis.expire("qrcode_" + qrcode, 300);
				Login login = new Login();
				login.setSessionId(sessionId);
				login.setQrcode(qrcode);
				login.setQrcodeStatus("0");
				login.setId(FormatUtil.createUuid());
				loginService.insertSelective(login);
				qrcode = "http://www.xiangshangban.com/show?shjncode=login_" + qrcode;
			}
			// 注册
			if (Integer.valueOf(type) == 1) {

				String format = "http://www.xiangshangban.com/show?shjncode=invite_";
				
				String token = request.getHeader("token");
				String WebAppType = request.getHeader("type");

				// 初始化redis
				RedisUtil redis = RedisUtil.getInstance();
				// 从redis取出短信验证码
				String phone = redis.new Hash().hget(token, "token");
				
				Uusers user = uusersService.selectByPhone(phone,WebAppType);
				
				String companyid = userCompanyService.selectBySoleUserId(user.getUserid(),WebAppType).getCompanyId();
				
				// 根据公司ID查询出公司编号 生成二维码
				Company company = companyService.selectByPrimaryKey(companyid);
				Map<String, String> invite = new HashMap<>();
				invite.put("companyNo", company.getCompany_no());
				invite.put("companyName", company.getCompany_name());
				invite.put("companyPersonalName", company.getCompany_personal_name());
				qrcode = format + JSON.toJSONString(invite);
			}
			result.put("qrcode", qrcode);
			result.put("message", "成功");
			result.put("returnCode", "3000");
			return result;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			logger.info(e);
			result.put("returnCode", "3007");
			result.put("message", "参数格式不正确");
			return result;
		} catch (NullPointerException e) {
			e.printStackTrace();
			logger.info(e);
			result.put("returnCode", "3006");
			result.put("message", "参数为null");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e);
			result.put("returnCode", "3001");
			result.put("message", "失败");
			return result;
		}
	}
	/**
	 * @author 李业/app扫描二维码
	 * @param qrcode
	 * @param request
	 * @return
	 */
	@RequestMapping("/scanCode")
	public Map<String, Object> scanCode(String qrcode, HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			RedisUtil redis = RedisUtil.getInstance();
			String token = request.getHeader("token");
			// 二维码是否过期(过期时间300秒)
			String redisQrcode = redis.new Hash().hget("qrcode_" + qrcode, "qrcode");
			if (redisQrcode == null) {
				result.put("message", "二维码已过期");
				result.put("returnCode", "4001");
				return result;
			}
			if (redisQrcode.equals(qrcode)) {
				Login webLogin = loginService.selectByQrcode(qrcode);
				Login appLogin = loginService.selectByToken(token);
				// Uusers user =
				// uusersService.selectByPhone(appLogin.getPhone());
				List<Uroles> listRole = uusersService.selectRoles(appLogin.getPhone());
				// 判断是否是企业管理员,'0':不是,'1':是
				int i = 0;
				for (Uroles role : listRole) {
					if ("admin".equals(role.getRolename())) {
						i = i + 1;
					}
				}
				if (i == 1) {
					// 建立qrcode,token,sessionId的关联
					webLogin.setToken(token);
					webLogin.setSalt(appLogin.getSalt());
					webLogin.setEffectiveTime(appLogin.getEffectiveTime());
					webLogin.setPhone(appLogin.getPhone());
					// 设置未扫描状态
					webLogin.setQrcodeStatus("1");
					loginService.updateByPrimaryKeySelective(webLogin);
				} else {
					loginService.deleteById(webLogin.getId());
					result.put("message", "没有企业管理员的权限");
					result.put("returnCode", "4002");
					return result;
				}
			} else {
				result.put("message", "二维码不正确");
				result.put("returnCode", "4001");
				return result;
			}
			result.put("message", "扫码成功,请确认登录");
			result.put("returnCode", "3000");
			return result;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			logger.info(e);
			result.put("returnCode", "3007");
			result.put("message", "参数格式不正确");
			return result;
		} catch (NullPointerException e) {
			e.printStackTrace();
			logger.info(e);
			result.put("returnCode", "3006");
			result.put("message", "参数为null");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e);
			result.put("returnCode", "3001");
			result.put("message", "失败");
			return result;
		}
	}
	/**
	 * @author 李业/app确认二维码登录
	 * @param request
	 * @return
	 */
	@RequestMapping("/confirmLogin")
	public Map<String, Object> confirmLogin(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			String token = request.getHeader("token");
			Login login = loginService.selectByToken(token);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// 设置登录时间
			login.setCreateTime(sdf.format(new Date()));
			// 更改二维码扫描状态
			login.setQrcodeStatus("2");
			loginService.updateByPrimaryKeySelective(login);
			result.put("message", "登录成功");
			result.put("returnCode", "3000");
			return result;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			logger.info(e);
			result.put("returnCode", "3007");
			result.put("message", "参数格式不正确");
			return result;
		} catch (NullPointerException e) {
			e.printStackTrace();
			logger.info(e);
			result.put("returnCode", "3006");
			result.put("message", "参数为null");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e);
			result.put("returnCode", "3001");
			result.put("message", "失败");
			return result;
		}
	}

	/**
	 * @author 李业/web二维码轮询接口
	 * @return
	 */
	@RequestMapping("/training")
	public Map<String, Object> training(HttpServletRequest request, HttpSession session) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			String sessionId = session.getId();
			// 获取app扫码状态
			Login login = loginService.selectBySessionId(sessionId);
			if (Integer.valueOf(login.getQrcodeStatus()) != 2) {
				result.put("message", "二维码未确认登录,请稍后...");
				result.put("returnCode", "4003");
				return result;
			}
			login.setQrcodeStatus("3");
			loginService.updateByPrimaryKeySelective(login);
			result.put("message", "登录成功");
			result.put("returnCode", "3000");
			return result;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			logger.info(e);
			result.put("returnCode", "3007");
			result.put("message", "参数格式不正确");
			return result;
		} catch (NullPointerException e) {
			e.printStackTrace();
			logger.info(e);
			result.put("returnCode", "3006");
			result.put("message", "参数为null");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e);
			result.put("returnCode", "3001");
			result.put("message", "失败");
			return result;
		}
	}
	
	@RequestMapping(value="/success",method=RequestMethod.GET)
	public Map<String, Object> success(HttpServletRequest request){
		Map<String, Object> result = new HashMap<String ,Object>();
		try{
		Subject subject = SecurityUtils.getSubject();
		String phone = subject.getPrincipal().toString();
		System.out.println("============>\t"+phone);
		RedisUtil redis = RedisUtil.getInstance();
		String type = request.getHeader("type");
		String token = request.getHeader("token");
		String clientId = request.getHeader("clientId");
		if(StringUtils.isEmpty(type)){
			result.put("message", "必传参数为空!");
			result.put("returnCode", "3006");
			return result;
		}
		if("1".equals(type)){
			if(StringUtils.isEmpty(token)){
				token = FormatUtil.createUuid();
				redis.getJedis().hset(token, "token", phone);
				redis.getJedis().expire(token, 1800);
			}else{
				String redisPhone = String.valueOf(redis.getJedis().hget(token, "token"));
				if(StringUtils.isEmpty(redisPhone)){
					token = FormatUtil.createUuid();
				}
				redis.getJedis().hset(token, "token", phone);
				redis.getJedis().expire(token, 1800);
			}
			redis.getJedis().hset("token"+phone, "token", clientId);
			this.changeLogin(phone, token, clientId, type);
			result.put("token", token);
		}
		if("0".equals(type)){
			String sessionId = request.getSession().getId();
			System.out.println("success\t:"+sessionId);
			redis.getJedis().hset(sessionId, "session", phone);
			redis.getJedis().expire(sessionId, 1800);
			redis.getJedis().hset("session"+phone, "session", sessionId);
			this.changeLogin(phone, sessionId, clientId, type);
		}
		
		Uusers user = uusersService.selectByPhone(phone,type);
		Uroles roles = uusersRolesService.SelectRoleByUserId(user.getUserid(), user.getCompanyId());
		if(roles==null || StringUtils.isEmpty(roles.getRolename())){
			result.put("message", "用户身份信息缺失");
			result.put("returnCode", "3003");
			return result;
		}
		result.put("roles", roles.getRolename());
		result.put("message", "登录成功!");
		result.put("returnCode", "3000");
		return result;
		}catch(Exception e){
			e.printStackTrace();
			result.put("message", "服务器错误");
			result.put("returnCode", "3001");
			return result;
		}
	}
	/**
	 * @author 李业/短信验证码登录
	 * @param phone
	 * @param smsCode
	 * @param type
	 * @param token
	 * @param session
	 * @param request
	 * @return
	 */
	@Transactional
	@RequestMapping(value = "/loginUser")
	public Map<String, Object> loginUser(HttpSession session,
			HttpServletRequest request)throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		//如果登陆失败从request中获取认证异常信息，shiroLoginFailure就是shiro异常类的全限定名
				String exceptionClassName = (String) request.getAttribute("shiroLoginFailure");
				String message = "请登录";
				//根据shiro返回的异常类路径判断，抛出指定异常信息
				if(exceptionClassName!=null){
					if (UnknownAccountException.class.getName().equals(exceptionClassName)) {
						//最终会抛给异常处理器
						//throw new CustomException("账号不存在");
						message ="账号不存在";
					} else if (IncorrectCredentialsException.class.getName().equals(
							exceptionClassName)) {
						//throw new CustomException("用户名/密码错误");
						message = "用户名/密码错误";
					} else if("randomCodeError".equals(exceptionClassName)){
						//throw new CustomException("验证码错误 ");
					}else {
						//throw new Exception();//最终在异常处理器生成未知错误
					}
				}
				result.put("message", message);
				System.out.println("message \t"+message);
				return result;
	}

	/**
	 * @author 李业/检查登录信息是否改变
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/offsiteLogin")
	public Map<String, Object> offsiteLogin(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			//request.getSession().invalidate();
			result.put("message", "账号在别处登录,请重新登录");
			result.put("returnCode", "4021");
			return result;
		} catch (Exception e) {
			logger.info(e);
			result.put("message", "服务器错误");
			result.put("returnCode", "3001");
			return result;
		}

	}

	/**
	 * @author 李业/shiro退出
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/logOuterr",produces="application/json;charset=utf-8",method=RequestMethod.POST)
	public Map<String, Object> logOut(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			Subject subject = SecurityUtils.getSubject();
			subject.logout();
			/*String phone = "";
			String type = request.getHeader("type");*/
			/*if("0".equals(type)){
				Object obj = request.getSession().getAttribute("phone");
				if(obj!=null){
					phone = obj.toString();
					uniqueLoginService.deleteByPhoneFromWeb(phone);
					request.getSession().invalidate();
				}
			}else{
				String token = request.getHeader("token");
				String clientId = request.getHeader("clientId");
				uniqueLoginService.deleteByTokenAndClientId(token, clientId);
			}*/
			result.put("message", "退出成功");
			result.put("returnCode", "3000");
			return result;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			result.put("returnCode", "3007");
			result.put("message", "参数格式不正确");
			logger.info(e);
			return result;
		} catch (NullPointerException e) {
			e.printStackTrace();
			result.put("returnCode", "3006");
			result.put("message", "参数为null");
			logger.info(e);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("returnCode", "3001");
			result.put("message", "失败");
			logger.info(e);
			return result;
		}
	}
	/**
	 * @author 校验手机验证码
	 * @param phone
	 * @param smsCode
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/confirmSms",method = RequestMethod.POST)
	public Map<String,Object> confirmSms(String phone,String smsCode,HttpServletRequest request){
		Map<String,Object> result = new HashMap<String,Object>();
		if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(smsCode)){
			result.put("message", "必传参数为空");
			result.put("returnCode", "3006");
			return result;
		}
		boolean phoneFlag = Pattern.matches("1[345678]\\d{9}", phone);
		if(!phoneFlag){
			result.put("message", "手机号格式不正确");
			result.put("returnCode", "4024");
			return result;
		}
		boolean smsCodeFlag = Pattern.matches("[0-9]{4}", smsCode);
		if(!smsCodeFlag){
			result.put("message", "验证码不正确");
			result.put("returnCode", "4002");
			return result;
		}
		// 初始化redis
		RedisUtil redis = RedisUtil.getInstance();
		// 从redis取出短信验证码
		String redisSmsCode = redis.new Hash().hget("smsCode_" + phone, "smsCode");
		if (StringUtils.isEmpty(redisSmsCode)) {
			result.put("message", "验证码过期");
			result.put("returnCode", "4001");
			return result;
		} else if (!redisSmsCode.equals(smsCode)) {
			result.put("message", "验证码不正确");
			result.put("returnCode", "4002");
			return result;
		}
		result.put("message", "成功");
		result.put("returnCode", "3000");
		return result;
	}
	/**
	 * @author 李业/获取短信验证码
	 * @param phone
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/sendSms")
	public Map<String, Object> sendSms(String phone, HttpServletRequest request, HttpSession session) {
		Map<String, Object> result = new HashMap<String, Object>();
		YtxSmsUtil sms = new YtxSmsUtil("LTAIcRopzlp5cbUd", "VnLMEEXQRukZQSP6bXM6hcNWPlphiP");
		String type = request.getHeader("type");
		try {
			Uusers user = uusersService.selectByPhone(phone,type);
			// 获取验证码
			String smsCode = "";
			//测试环境或者测试账号
			if("test".equals(PropertiesUtils.ossProperty("ossEnvironment")) || "15995611270".equals(phone)){
				smsCode = "6666";
			}else{
				smsCode = sms.sendIdSms(phone);
			}
			// user不为null,说明是登录获取验证码
			if (user != null) {
				// 更新数据库验证码记录,当做登录凭证
				uusersService.updateSmsCode(phone, smsCode);
			}
			RedisUtil redis = RedisUtil.getInstance();
			// 设值
			redis.new Hash().hset("smsCode_" + phone, "smsCode", smsCode);
			// 设置redis保存时间
			redis.expire("smsCode_" + phone, 60);
			// 设置返回结果
			//result.put("smsCode", smsCode);
			result.put("message", "成功");
			result.put("returnCode", "3000");
			return result;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			result.put("returnCode", "3007");
			result.put("message", "参数格式不正确");
			logger.info(e);
			return result;
		} catch (NullPointerException e) {
			e.printStackTrace();
			result.put("returnCode", "3006");
			result.put("message", "参数为null");
			logger.info(e);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put("returnCode", "3001");
			result.put("message", "失败");
			logger.info(e);
			return result;
		}
	}

	/**
	 * @author 李业/无权限请求统一返回接口
	 * @return
	 */
	@RequestMapping(value = "/unAuthorizedUrl")
	public Map<String, Object> unAuthorizedUrl(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("message", "没有权限");
		result.put("returnCode", "4000");
		String url = request.getRequestURI();
		logger.info("url :" + url + "message : 没有权限");
		return result;
	}
	private void changeLogin(String phone,String token,String clientId,String type){
		Login login = loginService.selectOneByPhone(phone);
		if(login!=null){
			loginService.deleteById(login.getId());
		}
		login = new Login();
		if("0".equals(type)){
			login.setSessionId(token);
		}else{
			login.setToken(token);
		}
			login.setId(FormatUtil.createUuid());
			login.setPhone(phone);
			login.setClientId(clientId);
			login.setStatus(type);
			loginService.insertSelective(login);
		
	}
}
