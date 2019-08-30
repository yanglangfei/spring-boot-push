package com.yanglf.push.handle;

import com.yanglf.push.model.UserBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author yanglf
 * @description
 * @since 2019/8/30
 **/
@Component
@Slf4j
public class MyWebSocketHandler implements WebSocketHandler {

    /**
     * 在线用户列表
     */
    private static final Map<Long, WebSocketSession> users = new HashMap<>();
    //用户标识
    private static final String SESSION_USER = "user";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //连接建立成功之后，记录用户的连接标识，便于后面发信息
        log.info("成功建立连接");
        Long userId = getUserId(session);
        System.out.println(userId);
        if (userId != null) {
            users.put(userId, session);
            session.sendMessage(new TextMessage("成功建立socket连接"));
            System.out.println(userId);
            System.out.println(session);
        }
    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable throwable) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        log.error("连接出错");
        users.remove(getUserId(session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        // 连接关闭
        users.remove(getUserId(session));
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


    /**
     * 发送信息给指定用户
     *
     * @param clientId
     * @param message
     * @return
     */
    public boolean sendMessageToUser(Long clientId, String message) {
        if (users.get(clientId) == null) {
            return false;
        }
        WebSocketSession session = users.get(clientId);
        log.info("sendMessage:" + session);
        if (!session.isOpen()) {
            return false;
        }
        try {
            int count = 1;
            TextMessage textMessage;
            String newMessage;
            // 循环向客户端发送数据
            while (true) {
                newMessage = message + count;
                textMessage = new TextMessage(newMessage);
                session.sendMessage(textMessage);
                Thread.sleep(5000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 广播信息
     *
     * @param message
     * @return
     */
    public boolean sendMessageToAllUsers(TextMessage message) {
        boolean allSendSuccess = true;
        Set<Long> clientIds = users.keySet();
        WebSocketSession session;
        for (Long clientId : clientIds) {
            try {
                session = users.get(clientId);
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
                allSendSuccess = false;
            }
        }
        return allSendSuccess;
    }

    private Long getUserId(WebSocketSession session) {
        try {
            UserBean userBean = (UserBean) session.getAttributes().get(SESSION_USER);
            return userBean.getId();
        } catch (Exception e) {
            return null;
        }
    }


}
