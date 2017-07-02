package com.netty.timedemo;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

/**
 * Created by charse on 17-7-1.
 *
 * 　用于对网络事件进行读写操作
 */
public class TimeServerHandler  extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //　Byte　类似于JDK　中的ByteBuffer对象,不过它提供了更加强大和灵活的功能
        ByteBuf buf = (ByteBuf) msg;
        //　buf.readableBytes()　方法可以获取缓冲区可读字节数，根据可读的字节数创建byte　数组
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        //　获取请求消息
        String body = new String(req, "UTF-8");

        System.out.println("The time server receive order :" + body);
        //
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)
                ? new Date(System.currentTimeMillis()).toString() :"BAD ORDER";
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        //write()　方法只是把代发送的消息放到发送缓冲数组中, 再通过调用flush方法将发送缓存中的
        //　消息全部写到SocketChannel中
        ctx.write(resp);

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 调用flush　方法将消息发送队列中的消息写入到　SocketChannel 中发送给对方
        /**
         * 　从性能的角度出发，为了防止频繁的唤醒Selector进行消息发送，Netty的write
         * 　方法并不直接将消息写入SocketChannel中，调用write　方法只是把代发送的消息放入大到
         * 　发送缓存数组中，再通过调用flush方法，将发送缓冲区的消息全部写到SocketChannel中
         * 　
         */
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //当发生异常的时候，关闭ChannelHandlerContext, 释放和ChannelHandlerContext相关的资源
        ctx.close();
    }
}
