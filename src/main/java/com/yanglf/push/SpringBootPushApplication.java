package com.yanglf.push;
import com.yanglf.push.netty.NettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SpringBootPushApplication implements CommandLineRunner {

    @Autowired
    private NettyServer nettyServer;


    public static void main(String[] args) {
        SpringApplication.run(SpringBootPushApplication.class, args);
    }

    @Override
    public void run(String... args)  {
        nettyServer.run();
    }
}
