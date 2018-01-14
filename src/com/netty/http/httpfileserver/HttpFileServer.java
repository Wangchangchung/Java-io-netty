package com.netty.http.httpfileserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.channels.SocketChannel;

/**
 * Author: charse
 * Date  : 17-12-19
 * Desc  :
 */
public class HttpFileServer{

    private static final String  DEFAULT_URL= "";

    public void run(final int port , final String url){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup  = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        /*
        ServerBootstrap serverBootstrap = bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {

                    }
                });
                */
    }
}
