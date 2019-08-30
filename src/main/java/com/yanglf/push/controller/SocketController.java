package com.yanglf.push.controller;

import com.yanglf.push.handle.MyWebSocketHandler;
import com.yanglf.push.model.UserBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yanglf
 * @description
 * @since 2019/8/30
 **/
@RestController
@Slf4j
@RequestMapping("/socket")
public class SocketController {


    private static final String SESSION_USER = "user";

    @Autowired
    private MyWebSocketHandler myWebSocketHandler;

    @RequestMapping("/login")
    public  String login(UserBean userBean, HttpServletRequest request) {
        System.out.println("========================== 开始登录 ===================");
        System.out.println("userId="+userBean.getId());
        System.out.println("userName="+userBean.getUserName());
        System.out.println("phone="+userBean.getPhone());

        request.getSession().setAttribute(SESSION_USER, userBean);
        System.out.println("========================== 登录成功 ===================");
        return "success";
    }

    @RequestMapping("/send/message")
    public String sendMessage(HttpServletRequest request) {
        UserBean userBean = (UserBean) request.getSession().getAttribute(SESSION_USER);
        boolean isSuccess = myWebSocketHandler.sendMessageToUser(userBean.getId(), "测试发送消息");
        log.info(isSuccess+"------");
        return "message";
    }

}
