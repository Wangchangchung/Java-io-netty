package com.netty.solvendelimiterbased;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by charse on 17-8-24.
 */
public class EchoServer {

    public void bind(int port) throws InterruptedException {
        //配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //首先创建分隔符缓冲对象ByteBuf,这里使用 "$_"作为 分隔符 ,将其加入到ChannelPipeline中.
                            ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
                            socketChannel.pipeline().addLast(
                                    new DelimiterBasedFrameDecoder(1024, delimiter));
                            /**
                             * 参数说明名：　　1024 表示单条消息的最大长度，　当
                             * 达到该长度后仍然没有查找到分隔符，就抛出TooLongFrameExecption
                             * 防止由于异常码流缺失分隔符导致的内存溢出．这个是解码器的可靠性保护措施
                             *
                             * 第二个参数：分隔符缓冲对象．
                             */
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new EchoServerHandler());
                        }
                    });
            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(port).sync();
            //等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        }finally {
            //优雅的退出, 释放线程池资源.
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public static void  main(String[] args) throws InterruptedException {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            }catch (NumberFormatException e){

            }
        }
        new EchoServer().bind(port);
    }
}
