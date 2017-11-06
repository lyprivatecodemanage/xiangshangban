package com.xiangshangban.transit_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xiangshangban.transit_service.bean.Uusers;
import com.xiangshangban.transit_service.dao.UusersMapper;
@Service("usersService")
public class UusersServiceImpl implements UusersService {
	@Autowired
	private UusersMapper uusersMapper;
	
	@Override
	public Uusers selectByPhone(String phone) {
		
		return uusersMapper.selectByPhone(phone);
	}
	@Override
	public Uusers selectByAccount(String account) {
		// TODO Auto-generated method stub
		return uusersMapper.selectByAccount(account);
	}
	
	@Override
	public int updateSmsCode(String phone,String smsCode) {
		// TODO Auto-generated method stub
		return uusersMapper.updateSmsCode(phone,smsCode);
	}
	

	@Override
	public int deleteByPrimaryKey(String userid) {
		return uusersMapper.deleteByPrimaryKey(userid);
	}

	@Override
	public int insert(Uusers record) {
		return uusersMapper.insert(record);
	}
	@Override
	public int updateByPrimaryKey(Uusers record) {
		return uusersMapper.updateByPrimaryKey(record);
	}

	@Override
	public Uusers selectByPrimaryKey(String userid) {
		
		return uusersMapper.selectByPrimaryKey(userid);
	}

	@Override
	public int updateByPrimaryKeySelective(Uusers record) {
		return uusersMapper.updateByPrimaryKeySelective(record);
	}

	@Override
	public int insertSelective(Uusers record) {
		return uusersMapper.insertSelective(record);
	}
	
	@Override
	public List<String> selectRoles(String phone) {
		// TODO Auto-generated method stub
		return uusersMapper.selectRoles(phone);
	}

	@Override
	public int SelecCountByPhone(String phone) {
		return uusersMapper.SelecByPhone(phone);
	}
	@Override
	public Uusers selectCompanyByToken(String token) {
		// TODO Auto-generated method stub
		return uusersMapper.selectCompanyByToken(token);
	}

	
	

}