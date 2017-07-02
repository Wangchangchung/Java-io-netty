package com.netty.timedemo;

import com.sun.org.apache.xpath.internal.SourceTree;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Logger;

/**
 * Created by charse on 17-7-2.
 */
public class TimeClientHandler extends ChannelHandlerAdapter {

    private static  final Logger logger = Logger.getLogger(TimeClientHandler.class.getName());

    private final ByteBuf firstMessage;

    public TimeClientHandler(){
        byte[] req = "QUERY TIME ORDER".getBytes();
        firstMessage = Unpooled.buffer(req.length);
        firstMessage.writeBytes(req);
    }

    //　客户端和服务器TCP　链路建立成功之后，netty的NIO　线程会调用channelActive方法
    //　发送查询时间的指令给服务端，调用　ChannelHandlerContext的writeAndFlush　
    // 方法将请求消息发送给服务端

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(firstMessage);
    }

    //当服务端返回应答消息的时候, 该方法被调用,
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);

        String body = new String(req, "UTF-8");

        System.out.print("Now is :" + body);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //　释放资源
        logger.warning("Unexpected execption from downstream:" + cause.getMessage());
        ctx.close();
    }
}
