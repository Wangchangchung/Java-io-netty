package wcc.niodemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by charse on 17-6-22.
 */
public class TimeClientHandle  implements  Runnable{

    private  String host;
    private  int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private  volatile  boolean stop;


    /* 初始化NIO 的多路复用器 和SocketChannel 对象
        需要注意的是, 创建SocketChannel之后，需要将其设置为异步非阻塞模式
        我们可以设置SocketChannel 的TCP参数，例如接收和发送的TCP缓冲区大小

     */

    public TimeClientHandle(String host, int port){

        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;
        try {
            selector  = Selector.open();
            socketChannel  = SocketChannel.open();
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
            // 退出  释放系统资源
            System.exit(1);
        }
    }


    @Override
    public void run() {

        //用于发送连接请求
        try {
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //在循环体中轮询多路复用器Selector, 当有就绪的Channel时,执行handleInput()方法进行分析
        while (!stop){
            try {
                selector.select(1000);
                Set<SelectionKey>  selectionKeys = selector.selectedKeys();

                Iterator<SelectionKey> iterator =  selectionKeys.iterator();
                SelectionKey key = null;
                while (iterator.hasNext()){
                    key = iterator.next();
                    iterator.remove();
                    try {
                        handleInput(key);
                    }catch (Exception e){

                        if (key != null){
                            key.cancel();
                            if (key.channel() != null){
                                key.channel().close();
                            }
                        }
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        // 多路复用器关闭后, 所有注册在上面的 Channel 和Pipe 等资源都会被自动去注册并关闭
        // 所以不需要重复释放资源
        if (selector != null){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // 做连接操作
    private  void  doConnect() throws IOException {
        // 如果连接成功,则注册到多路复用器上，发送消息，读应答
        if (socketChannel.connect(new InetSocketAddress(host,port))){
            //如果连接成功, 则将 SocketChannal注册大多路复用器select 上,
            // 注册SelectionKey.OP_READ;如果没有直接连接成功，则说明服务端没有返回TCP
            // 握手应答消息，但是这并不代表连接失败.
            //当服务端返回TCP syn-ack 消息后，Selector就能轮询到这个SocketChannel处于就绪状态
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
        }else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }

    }

    // 写入数据
    private  void doWrite(SocketChannel socketChannel) throws IOException {
        /**
         *  在doWrite 中构造请求消息体 再将字节数组
         *  写入到发送缓冲区中，最后调用SocketChannel的write 方法进行发送
         *
         */
        byte[] req = "QUERY TIME ORDER".getBytes();
        ByteBuffer  writeBuffer = ByteBuffer.allocate(req.length);

        writeBuffer.put(req);
        writeBuffer.flip();

        socketChannel.write(writeBuffer);

        if (!writeBuffer.hasRemaining()){
            System.out.println("Send order to Server Secceed !");
        }
    }

    private  void handleInput(SelectionKey selectionKey) throws IOException {
        /*对SelectionKey  进行判断，看他是处于什么状态，
         如果是处于连接状态，说明服务端已经返回ACK 应答消息
         这个时候,我们需要堆连接结果进行判断，调用SocketChannel 的finishConnect() 方法
         如果返回值为true, 说明客户端连接成功
         如果返回值为false或者连接异常，说明连接失败
         */
        // 判断是否链接成功
        if (selectionKey.isValid()){
            SocketChannel sc = (SocketChannel) selectionKey.channel();
            // 连接成功之后将 SocketChannel注册到多路复用器上，注册OP_READ 操作。

            if (selectionKey.isConnectable()){
                if (sc.finishConnect()){
                    sc.register(selector, SelectionKey.OP_READ);
                    doWrite(sc);
                }else {
                    // 失败链接进程退出
                    System.exit(1);
                }
            }

            // 客户端  是如何读取时间服务器应答消息的?

            /**
             * 如果客户端接收到了服务器的应答消息，则SocketChannel 是可读的，由于
             * 无法事先判断应答码流的大小, 我们就预分配1MB的接收缓冲区用于读取应答消息
             *
             */
            if (selectionKey.isReadable()){
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                // 由于是异步操作所以 必须堆读取结果进行判断。

                if (readBytes > 0){
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];

                    readBuffer.get(bytes);

                    String body = new String(bytes, "UTF-8");
                    System.out.println("Now is :" + body);

                    this.stop = true;
                }else if (readBytes < 0){
                    // 对链路进行关闭
                    selectionKey.cancel();

                    sc.close();
                }else {
                     // 读到的是0 字节 忽略
                }
            }
        }
    }
}
