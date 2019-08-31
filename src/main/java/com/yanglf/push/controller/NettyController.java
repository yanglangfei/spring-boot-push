package com.yanglf.push.controller;

import com.yanglf.push.netty.NettyClient;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yanglf
 * @description
 * @since 2019/8/30
 **/
@RestController
@RequestMapping("/netty")
public class NettyController {

    private Map<Long, Channel> channelMap = new HashMap<>();


    @RequestMapping("/connect/{userId}")
    public String connect(@PathVariable Long userId) {
        try {
            NettyClient nettyClient = new NettyClient();
            Channel channel = nettyClient.connect("127.0.0.1", 8888,userId);
           if(channel!=null){
               channelMap.put(userId, channel);
           }
            return "success";
        } catch (Exception e) {
          e.printStackTrace();
        }
        return "fail";
    }


    @RequestMapping("/send/{msg}")
    public String send(@PathVariable String msg, Long userId) {
        try {
            Channel channel = channelMap.get(userId);
            if (channel != null) {
                channel.writeAndFlush(msg);
                return "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "fail";
    }


}
