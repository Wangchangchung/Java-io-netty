package com.wcc.niodemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by charse on 17-6-20.
 */
public class MultiplexerTimeServer implements  Runnable {


    private Selector  selector;

    private ServerSocketChannel serverSocketChannel;

    private volatile boolean stop;

    /**
     * 初始化多路复用器, 绑定监听端口
     * @param port
     */
    public MultiplexerTimeServer(int port){
            try {
                // 创建多路复用器 selector
                selector = Selector.open();
                //创建爱 ServerSocketChannel
                serverSocketChannel  = ServerSocketChannel.open();
                // 对 Channel 和 TCP参数进行设置
                // 1、设置为异步非阻塞模式
                serverSocketChannel.configureBlocking(false);
                // 2、 设置 backlog 为 1024
                serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024);

                // 系统资源初始化成功之后, 将 selector 注册到Channel 中,  监听Selectionkey 中
                // OP_ACCEPT 操作位
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                System.out.println("The time server is strat in port:" + port);

            } catch (IOException e) {
                e.printStackTrace();
                // 如果系统资源初始化失败(如果端口被占用) 则退出
                System.exit(1);
            }
    }


    public void stop(){
        this.stop  = true;
    }


    @Override
    public void run() {
        while (!stop){
                try {
                    //  无论是否有读写等事件发生, 设置  休眠时间是每隔 1 秒被唤醒一次
                    /**
                     *
                     * selector也提供了一个无参的select 方法: 当有处于就绪状态的Channel时
                     * selector将返回 Channel 的 SelectionKey集合,通过对就绪状态的Channel
                     * 集合进行迭代，可以进行网络的异步读写操作.
                     */

                    selector.select(1000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Set<SelectionKey>  selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterable = selectionKeys.iterator();
                SelectionKey  key = null;

                while (iterable.hasNext()){
                    key = iterable.next();
                    iterable.remove();

                    try {

                        handleInput(key);
                    } catch (IOException e) {
                        if (key != null){
                            key.cancel();
                            if (key.channel() != null){
                                try {
                                    key.channel().close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                        e.printStackTrace();
                    }


                }

        }

        // 多路复用器关闭之后, 所有注册在上面的channel 和 pipe等资源都会
        // 被自动区注册并关闭，所以不需要重复释放资源
        if (selector != null){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private  void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()){
            /* 处理新接入的请求信息
                根据SelectionKey的操作位进行判断即可获知网络
                事件的类型
             */
            if (key.isAcceptable()){
                //接受一个新的连接
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                // 接受客户端的请求, 并创建 SocketChannel实例
                SocketChannel sc = ssc.accept();
                // 设置为非阻塞的
                sc.configureBlocking(false);

                sc.register(selector, SelectionKey.OP_READ);

                /* 完成上诉操作之后，相当于完成了TCP 的三次握手
                 TCP 物理链路正式建立
                 注意, 我们需要将新建的SocketChannel 设置为异步非阻塞，同时
                 也可以堆器TCP 参数进行设置， 例如设置 TCP接受和发送缓冲区的大小等
                 */
            }

            // 用于读取客户端的请求消息
            if (key.isReadable()){
                /**
                 * 创建一个ByteBuffer,由于我们事先无法得知客户端发送的
                 * 码流大小，作为例程, 我们开辟一个1MB 的缓冲区。然后再调用
                 * SocketChannel 的 read 方法，读取请求流码, 因为我们已经将
                 * SocketChannael设置为异步非阻塞的方式，使用返回值进行判断
                 * ，看读取到的字节数, 返回值进行判断, 看读取到的字节数
                 * 返回值有三种可能:
                 * 1、 返回值 大于 0: 读取到字节，对字节进行编码
                 * 2、返回值等于1： 没有读取到字节，属于正常的现象,
                 * 3、 返回值为-1： 链路已经关闭了，需要关闭SocketChannel
                 *
                 */
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    /*
                    将缓冲区当前的limit 设置为 position, position设置为0
                    用于后续对缓冲区的读取操作
                     */

                    readBuffer.flip();
                    // 通过缓冲区的可读的字节数大小 创建字节数组
                    byte[] bytes = new byte[readBuffer.remaining()];
                    // 调用缓冲区可读的字节数组复制到新创建的字节数组中
                    readBuffer.get(bytes);
                    // 构造字符串, 字符编码为 utf-8
                    String body = new String(bytes, "UTF-8");
                    //
                    System.out.println("Time Server receive order :" + body);
                    // 如果请求的指令是 "QUERY TIME ORDER" 则把服务器当前的时间编码返回给客户端
                    String currentTime = "QUERY TIME ORDER".equals(body) ? new Date(System.currentTimeMillis()).toString()
                            : "BAD ORDER";

                    // 将消息异步的发送给客户端
                    doWrite(sc, currentTime);

                }else if (readBytes < 0){
                    //对链路端关闭
                    key.cancel();
                    sc.close();
                }else{
                     //读到 0 字节, 忽略
                }
            }
        }
    }

    //
    private  void doWrite(SocketChannel channel, String response) throws IOException {
        if (response  != null  && response.trim().length() > 0){
            /**
             *  首先 将字符 编成字节数组,
             *
             *  调用 ByteBuffer中的put 操作将字节数组复制到 缓冲区中
             *  然后对缓冲区进行 flip 操作， 最后调用 SocketChannel的
             *  write 方法将缓冲区中的数据发送出去.
             *
             */
            byte[] bytes = response.getBytes();
            ByteBuffer  writerBuffer = ByteBuffer.allocate(bytes.length);
            writerBuffer.put(bytes);
            writerBuffer.flip();
            channel.write(writerBuffer);
            /**
             *  由于 SocketChannel 是异步非阻塞的, 它并不保证一次能够
             *  把需要发送的字节诉则发送完，此时会出现 "写半包"问题，
             *  我们需要注册写操作，不断轮询 Selector将没有发送完的ByteBuffer
             *  的 hasRemain() 方法判断消息是否发送完成。
             *  因为此处是一个简单的入门的教程,所以这里是不先做处理
             *  后续的案例中再做处理
             *
             */
        }
    }
}
