package com.yanglf.push.netty;

import com.yanglf.push.model.NettyChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
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

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author yanglf
 * @description
 * @since 2019/8/30
 **/
@Slf4j
public class NettyClient {

    private static final int TRY_TIMES = 3;

    private int currentTime = 0;

    public static final AttributeKey<NettyChannel> NETTY_CHANNEL_KEY = AttributeKey.valueOf("netty.channel");


    public Channel connect(String host, int port, Long userId) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture channelFuture = null;
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            // 降低网络延迟
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) {
                    log.info("当前  channel:{}", ch.id().asShortText());
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
                    ch.pipeline().addLast("ping", new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
                    ch.pipeline().addLast(new LengthFieldPrepender(2));
                    ch.pipeline().addLast("decoder", new StringDecoder());
                    ch.pipeline().addLast("encoder", new StringEncoder());
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                            Long senderId = setAttr(ctx, userId);
                            log.info("receiverId:{}----------------senderId:{}", userId, senderId);
                            log.info("客户端收到消息:[{}]------,senderId:{}", msg, senderId);
                            ctx.fireChannelRead(msg);

                        }

                        private Long setAttr(ChannelHandlerContext ctx, Long userId) {
                            Attribute<NettyChannel> attribute = ctx.attr(NETTY_CHANNEL_KEY);
                            NettyChannel nettyChannel = attribute.get();
                            if (nettyChannel == null) {
                                attribute.setIfAbsent(NettyChannel.builder()
                                        .id(UUID.randomUUID().toString().replace("-", ""))
                                        .createTime(new Date())
                                        .userId(userId)
                                        .build());
                                return null;
                            } else {
                                log.info("attr:{}", nettyChannel);
                                return nettyChannel.getUserId();
                            }
                        }

                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            if (evt instanceof IdleStateEvent) {
                                IdleStateEvent event = (IdleStateEvent) evt;
                                if (event.state() == IdleState.WRITER_IDLE) {
                                    if (currentTime <= TRY_TIMES) {
                                        currentTime++;
                                        //在服务端设置的心跳时间内 发送心跳消息  验证服务器是否存活
                                        ctx.channel().writeAndFlush("channel:[" + ch.id().asShortText() + "]心跳消息");
                                    }
                                }
                            }
                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            log.info("客户端 channel 激活");
                            setAttr(ctx, userId);

                            ctx.fireChannelActive();
                        }

                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            log.info("客户端 channel 失效");
                        }


                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            ctx.close();
                        }
                    });
                }
            });
            channelFuture = bootstrap.connect(host, port);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (f.isSuccess()) {
                        log.info("connect  success");
                    } else {
                        // 重连
                        log.info("connect  fail ");
                        f.channel().pipeline().fireChannelInactive();
                    }
                }
            });
            Channel channel = channelFuture.channel();
            if (channel != null) {
                Thread.sleep(2000);
                channel.writeAndFlush("发起连接");
                // channel.closeFuture().sync();
            }
            return channel;
        } catch (Exception e) {
            group.shutdownGracefully();
            e.printStackTrace();
          /*  if (null != channelFuture) {
                if (channelFuture.channel() != null && channelFuture.channel().isOpen()) {
                    channelFuture.channel().close();
                }
            }*/
            //    log.info("失败重连 ------");
            // connect(host, port, userId);
            return null;
        }
    }

}
