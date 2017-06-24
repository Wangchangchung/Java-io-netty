package wcc.aiodemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * Created by charse on 17-6-23.
 */
public class AsyncTimeServerHandler implements Runnable {

    private int port;

    public CountDownLatch latch;

    AsynchronousServerSocketChannel asynchronousServerSocketChannel;

    public AsyncTimeServerHandler(int port){
        this.port = port;
        try {
            asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
            asynchronousServerSocketChannel.bind(new InetSocketAddress(port));

            System.out.println("The time  server is  start in port:" + port);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        latch =  new CountDownLatch(1);

        doAccpet();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doAccpet(){
        asynchronousServerSocketChannel.accept(this, new AcceptCompletionHandler());
    }

}
