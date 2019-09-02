package com.yanglf.push.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

/**
 * msgpack 跨语言通讯
 * @author yanglf
 * @description
 * @since 2019/9/2
 **/
public class MsgPackEncoder extends MessageToMessageEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, List<Object> list) throws Exception {


    }
}
