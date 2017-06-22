package wcc.niodemo;

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
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    //
                    readBuffer.flip();

                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);

                    String body = new String(bytes, "UTF-8");

                    System.out.println("Time Server receive order :" + body);

                    String currentTime = "QUERY TIME ORDER".equals(body) ? new Date(System.currentTimeMillis()).toString()
                            : "BAD ORDER";

                    doWrite(sc, currentTime);
                }else if (readBytes < 0){
                    //对链路端关闭
                    key.cancel();
                    sc.close();
                }
            }else {
                ; //读到0 字节, 忽略
            }
        }
    }

    private  void doWrite(SocketChannel channel, String response) throws IOException {
        if (response  != null  && response.trim().length() > 0){
            byte[] bytes = response.getBytes();
            ByteBuffer  writerBuffer = ByteBuffer.allocate(bytes.length);
            writerBuffer.put(bytes);
            writerBuffer.flip();
            channel.write(writerBuffer);
        }
    }
}
