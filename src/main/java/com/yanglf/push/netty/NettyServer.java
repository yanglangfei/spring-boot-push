package com.yanglf.push.netty;

import com.yanglf.push.model.NettyChannel;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;
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
public class NettyServer {

    @Value("${netty.boos.thread:10}")
    private int bossThread;

    @Value("${netty.worker.thread:50}")
    private int workerThread;

    @Value("${netty.server.port:8888}")
    private int serverPort;

    private int loss_read_time = 0;

    public static final AttributeKey<NettyChannel> NETTY_CHANNEL_KEY = AttributeKey.valueOf("netty.channel");

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
                    // DelimiterBasedFrameDecoder 使用特定字符切分消息
                    // FixedLengthFrameDecoder 按帧数据的字节 切分消息
                    // LineBasedFrameDecoder 行解码器   根据换行符区分是否结束
                    // LengthFieldBasedFrameDecoder添加到pipeline的首位，因为其需要对接收到的数据
                    // 进行长度字段解码，这里也会对数据进行粘包和拆包处理
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
                    // LengthFieldPrepender是一个编码器，主要是在响应字节数据前面添加字节长度字段
                    ch.pipeline().addLast(new LengthFieldPrepender(2));
                    // 心跳
                    ch.pipeline().addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                    ch.pipeline().addLast("decoder", new StringDecoder());
                    ch.pipeline().addLast("encoder", new StringEncoder());
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                            if (msg.startsWith("HB:")) {
                                log.info("HB---------" + ctx.channel().id().asShortText());
                            } else {
                                log.info("服务端接受到消息:[{}]====================channel:{}", msg, ctx.channel().id().asShortText());
                                ctx.writeAndFlush(msg);
                            }
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            ctx.close();
                        }

                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            // 触发心跳事件
                            if (evt instanceof IdleStateEvent) {
                                IdleStateEvent event = (IdleStateEvent) evt;
                                if (event.state() == IdleState.READER_IDLE) {
                                    log.info("30 秒没有接收到客户端的信息了,channel:{}", ctx.channel().id().asShortText());
                                    loss_read_time++;
                                    if (loss_read_time > 2) {
                                        ctx.channel().close();
                                        log.info("超过两次没收到消息,关闭这个连接 -----channel:{}", ctx.channel().id().asShortText());
                                    }
                                }
                            } else {
                                super.userEventTriggered(ctx, evt);
                            }
                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            log.info("服务端 channel:{} 激活", ctx.channel().id().asShortText());
                            ctx.fireChannelActive();
                        }

                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            log.info("服务端 channel:{} 失效", ctx.channel().id().asShortText());
                        }

                        @Override
                        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                            log.info("注册 channel:{} ", ctx.channel().id().asShortText());
                        }

                        @Override
                        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                            log.info(" 反注册  channel :{}", ctx.channel().id().asShortText());
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
            ChannelFuture channelFuture = serverBootstrap.bind(serverPort).sync();
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
