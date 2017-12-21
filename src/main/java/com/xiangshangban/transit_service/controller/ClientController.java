package com.xiangshangban.transit_service.controller;

import com.xiangshangban.transit_service.bean.Client;
import com.xiangshangban.transit_service.service.ClientService;
import com.xiangshangban.transit_service.util.FormatUtil;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mian on 2017/11/3.
 */
@RestController
@RequestMapping("/ClientIdController")
public class ClientController {

    @Autowired
    ClientService clientService;

    Logger logger = Logger.getLogger(RegisterController.class);
    /***
     * 焦振/生成极光推送的clientId
     * @param type
     * @param imei
     * @param model
     * @return
     */
    @Transactional
    @RequestMapping(value = "/createClient",produces = "application/json;charset=utf-8",method = RequestMethod.GET)
    public Map<String,Object> createClient(String type,String imei,String model){

        Map<String,Object> map = new HashMap<String,Object>();
        String clientId = "";
        try {
        	//根据imei查询数据库是否存在clientId
        	Client selectclient = clientService.SelectByImei(imei);
        	
        	//不存在则创建  存在则返回
        	if(selectclient == null){
        		clientId = FormatUtil.createUuid();
                Client client = new Client(clientId,type,imei,model);
                clientService.insertSelective(client);
        	}else{
        		clientId=selectclient.getClientId();
        	}

            map.put("clientId",clientId);
            map.put("returnCode","3000");
            map.put("message","数据请求成功");
            return map;
        }catch(Exception e){
            e.printStackTrace();
            logger.info(e);
            map.put("returnCode","3001");
            map.put("message","服务器错误");
            return map;
        }
    }


}
