package com.yanglf.push.config;

import com.yanglf.push.handle.MyWebSocketHandler;
import com.yanglf.push.interceptor.WebSocketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 *
 * 注册  web-socket 处理类
 * @author yanglf
 * @description
 * @since 2019/8/30
 **/
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new MyWebSocketHandler(), "/websocket").addInterceptors(new WebSocketInterceptor());
    }
}
