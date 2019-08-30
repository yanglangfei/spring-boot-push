package com.yanglf.push;

import com.yanglf.push.netty.NettyClient;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SpringBootPushApplication implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(SpringBootPushApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        NettyClient nettyClient = new NettyClient();
        Channel channel = nettyClient.init("127.0.0.1", 8888);
        channel.writeAndFlush("hello netty");

    }

}
