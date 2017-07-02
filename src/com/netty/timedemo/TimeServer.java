package com.netty.timedemo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;



/**
 * Created by charse on 17-7-1.
 */
public class TimeServer {

    public void bind(int port){
        //　配置服务端的NIO线程组　专门用于网络事件的处理，实际上他们就是Reactor线程组
        // 这里创建两个的原因是一个用于服务端接受客户端的连接，另一个用于进行SocketChannel的网络读写
        //
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // ServerBootstrap对象，它是Netty用于启动NIO服务端的辅助启动类，目的设计降低服务端的开发复杂度

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)//　类似JDK　NIO 类库中的ServerSocketChannel　
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChildChannelHandler());//　绑定I/O事件的处理类ChilaChannelHandlerder,它的
                //　作用类似与Reactor模式中的Handler类，主要用于处理网络I/O事件,例如：记录日志,　对消息进行编解码等
            //绑定端口，同步等待成功
            ChannelFuture future =  bootstrap.bind(port).sync();

            //等待服务端监听端口关闭
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            //　优雅的退出．　释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        //等待服务器监听端口关闭
    }

    private  class ChildChannelHandler extends ChannelInitializer<SocketChannel>{

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }
    }

    public  static  void  main(String[] args){
        int port = 8080;
        if (args!= null && args.length > 0){
            try {
                port = Integer.valueOf(args[0]);
            }catch (NumberFormatException e){
                //采用默认值
            }
        }
        new TimeServer().bind(port);
    }
}
