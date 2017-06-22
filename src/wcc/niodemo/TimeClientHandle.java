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
        try {
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

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
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
        }else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }

    }

    // 写入数据
    private  void doWrite(SocketChannel socketChannel) throws IOException {

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
        if (selectionKey.isValid()){
            // 判断是否是连接成功
            SocketChannel sc = (SocketChannel) selectionKey.channel();
            if (selectionKey.isConnectable()){
                if (sc.finishConnect()){
                    sc.register(selector, SelectionKey.OP_READ);
                    doWrite(sc);
                }else {
                    // 失败链接进程退出
                    System.exit(1);
                }
            }
            if (selectionKey.isReadable()){
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);

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
