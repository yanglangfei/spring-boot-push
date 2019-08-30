package com.yanglf.push.controller;

import com.yanglf.push.netty.NettyClient;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yanglf
 * @description
 * @since 2019/8/30
 **/
@RestController
@RequestMapping("/netty")
public class NettyController {

    @Autowired
    private NettyClient nettyClient;

    @RequestMapping("/send/{msg}")
    public String sendMsg(@PathVariable String msg) {
        try {
            Channel channel = nettyClient.init("127.0.0.1", 8888);
            channel.writeAndFlush(msg);
            channel.flush();
            channel.closeFuture();
            return "success";
        } catch (Exception e) {
            return "fail";
        }

    }

}
