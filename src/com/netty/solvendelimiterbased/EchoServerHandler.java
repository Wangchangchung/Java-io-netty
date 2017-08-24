package com.netty.solvendelimiterbased;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by charse on 17-8-24.
 */
public class EchoServerHandler  extends ChannelHandlerAdapter{

    int count;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        String  body  = (String) msg;
        System.out.println("this is " + (++count) +" times receive client :[　" + body +"]");
        body += "$_";
        ByteBuf echo = Unpooled.copiedBuffer(body.getBytes());

        ctx.writeAndFlush(echo);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
