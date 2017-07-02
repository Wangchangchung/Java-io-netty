package com.wcc.niodemo;

/**
 * Created by charse on 17-6-21.
 */
public class TimeClient {

    /**
     * 我们通过这些项目的练习发现: NIO编程的难度的确是比同步阻塞BIO
     * BIO大很多,我们的NIO并没有考虑"半包读" 和"半包写"
     *
     * 如果加上这些，代码将会更加复杂，NIO 编程既然这个复杂，为什么它的
     * 应用缺越来越广泛呢？ 使用NIO编程的优点哟与如下几点：
     * 1、客户端发起的连接操作是异步的, 可以通过在多路复用器上注册OP_CONNECT等后续结果
     * 不需要像之前的客户端那样被同步阻塞。
     *
     * 2、SocketChannal 的读写操作都是异步的，如果没有可读可写的数据，
     * 它是不会同步等待，直接返回，这样I/O线程就可以处理其他的链路，不需要同步等待
     * 这个链路可用
     *
     * 3、线程模型的优化: 由于JDK的Selector 在Linux等主流操作系统上通过epoll 实现
     * 它没有连接句柄数的限制(只受限于操作系统的最大句柄数或者堆单个进程的句柄守限制)
     * 这意味着一个Selector线程可以同时处理成千上万个客户端连接，而且性能不会
     * 随着客户端的增加而线性下降，因此，非常适合做高性能、高负载的网络服务器
     *
     *
     * jdk1.7 中升级了 NIO 类库, 升级后的NIO 类库被称为NIO 2.0
     *
     * @param args
     */
    public static  void main(String[] args){

       int  port = 8080;

       if (args != null && args.length > 0){
           port =  Integer.valueOf(args[0]);
       }

       for (int i = 0; i < 2000; i++) {
           new Thread(new TimeClientHandle("127.0.0.1", port), "Timeclient-00" + i).start();
       }
    }
}
