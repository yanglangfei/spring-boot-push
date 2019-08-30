package com.yanglf.push.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author yanglf
 * @description
 * @since 2019/8/30
 **/
@Component
@Slf4j
public class NettyServer {

    @Value("${netty.boos.thread:10}")
    private int bossThread;

    @Value("${netty.worker.thread:50}")
    private int workerThread;

    @Value("${netty.server.port}")
    private int serverPort;

    @PostConstruct
    public void init() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup boos = new NioEventLoopGroup(bossThread);
        NioEventLoopGroup worker = new NioEventLoopGroup(workerThread);
        serverBootstrap.group(boos, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                log.info("服务端接受到消息:[{}]", msg);
                            }
                        });
                    }
                }).bind(serverPort);
        log.info("netty server is  start--------------------------------");
    }
}
