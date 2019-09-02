package com.yanglf.push.netty;

import com.yanglf.push.model.NettyChannel;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author yanglf
 * @description
 * @since 2019/8/30
 **/
@Component
@Slf4j
public class NettyHttpServer {

    @Value("${netty.boos.thread:10}")
    private int bossThread;

    @Value("${netty.worker.thread:50}")
    private int workerThread;

    @Value("${netty.server.port:8899}")
    private int serverPort;

    private int loss_read_time = 0;


    public void run() {
        NioEventLoopGroup boos = new NioEventLoopGroup(bossThread);
        NioEventLoopGroup worker = new NioEventLoopGroup(workerThread);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 绑定   boss worker 线程模型
            serverBootstrap.group(boos, worker);
            // 指定  服务端 IO 模型  NIO 和 BIO
            serverBootstrap.channel(NioServerSocketChannel.class);
            // childHandler() 用于指定处理新连接数据的读写处理逻辑
            // handler() 用于指定在服务端启动过程中的一些逻辑，通常情况下呢，我们用不着这个方法
            serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast("http-decoder",new HttpRequestDecoder());
                    ch.pipeline().addLast(new HttpObjectAggregator(65536));
                    ch.pipeline().addLast(new HttpResponseEncoder());
                    ch.pipeline().addLast(new ChunkedWriteHandler());

                    ch.pipeline().addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {


                        @Override
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) throws Exception {
                            if(!request.getDecoderResult().isSuccess()){
                                log.error("请求失败....:{}",request);
                                return;
                            }
                            log.info("请求成功-----:{}",request);

                        }
                    });
                }
            });
            // 指定  自定义 属性
            serverBootstrap.attr(AttributeKey.newInstance("serverName"), "nettyServer");
            // 给每一条连接 定义 属性  可以通过  channel.attr() 获取到
            serverBootstrap.childAttr(AttributeKey.newInstance("clientKey"), "clientValue");
            // 给每一条连接设置属性
            // SO_KEEPALIVE  表示是否开启TCP底层心跳机制，true为开启
            // TCP_NODELAY 是否开始Nagle算法，true表示关闭，false表示开启，通俗地说，如果要求高实时性，有数据发送时就马上发送，就关闭，如果需要减少发送次数减少网络交互，就开启。
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);
            //给服务端channel设置一些属性 系统用于临时存放已完成三次握手的请求的队列的最大长度
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            ChannelFuture channelFuture = serverBootstrap.bind("localhost",serverPort).sync();
            channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("netty server is  start success--------------------------------");
                    } else {
                        log.info("netty server is  start  fail --------------------------------");
                    }
                }
            });
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boos.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }
}
