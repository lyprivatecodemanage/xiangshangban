package com.xiangshangban.transit_service.bean;

public class UniqueLogin {
	private String  id;
	private String  phone;
	private String  sessionId;
	private String  token;
	private String  clientId;
	private String  status;
	private String  createTime;
	
	public UniqueLogin(){}
	public UniqueLogin(String id, String phone, String sessionId, String token, String clientId, String status,
			String createTime) {
		this.id = id;
		this.phone = phone;
		this.sessionId = sessionId;
		this.token = token;
		this.clientId = clientId;
		this.status = status;
		this.createTime = createTime;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	@Override
	public String toString() {
		return "UniqueLogin [id=" + id + ", phone=" + phone + ", sessionId=" + sessionId + ", token=" + token
				+ ", clientId=" + clientId + ", status=" + status + ", createTime=" + createTime + "]";
	}
	
	
	
	
}
