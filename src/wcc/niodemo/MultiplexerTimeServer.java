package wcc.niodemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
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
        while (!stop){
                try {
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

                }

        }
    }


    private  void handleInput(SelectionKey key){
        if (key.isValid()){
            if (key.isAcceptable()){

                //接受一个新的
            }
        }
    }
}
