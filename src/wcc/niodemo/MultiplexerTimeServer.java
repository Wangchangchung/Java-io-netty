package wcc.niodemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

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
                selector = Selector.open();
                serverSocketChannel  = ServerSocketChannel.open();
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024);
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                System.out.println("The time server is strat in port:" + port);

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
    }


    public void stop(){
        this.stop  = true;
    }



    @Override
    public void run() {

    }
}
