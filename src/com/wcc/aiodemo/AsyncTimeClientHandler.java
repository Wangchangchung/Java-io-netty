package com.wcc.aiodemo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Created by charse on 17-6-24.
 */
public class AsyncTimeClientHandler  implements CompletionHandler<Void, AsyncTimeClientHandler>, Runnable{

    private AsynchronousSocketChannel client;

    private String host;

    private int port;

    private CountDownLatch latch;

    public AsyncTimeClientHandler(String host, int port){
        // 初始化
        this.host  = host;
        this.port = port;
        try {
            // 创建一个新的AsynchronousSocketChannel
            client  = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // 创建 CountDownLatch进行等待， 防止异步操作没有执行完线程
        // 就退出,
        latch = new CountDownLatch(1);
        // 发起异步请求
        /**
         * attachment: AsynchronousSocletChannel的附件,用于回调通知时作为入参数被传递，调用者可以自定义
         *
         * handler: 异步操作回调通知接口，由调用者实现
         *
         * 在本例中 这两个参数都使用 AsyncTimeClientHandler 类本身，因为它实现了CompletionHandler 接口
         *
         */
        client.connect(new InetSocketAddress(host, port), this, this);

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 异步连接之后成功的方法回调--- completed方法
    @Override
    public void completed(Void result, AsyncTimeClientHandler attachment) {
        // 构造请求消息体，对其进行编码，然后复制到发送缓冲区writeBuffer中
        byte[] req = "QUERY TIME ORDER".getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);

        writeBuffer.put(req);
        writeBuffer.flip();
        // 异步写入                             //操作完成后的回调
        client.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                // 如果发送缓冲区中还有尚未发送的字节,将继续异步法送
                if (buffer.hasRemaining()){
                    client.write(buffer, buffer, this);
                }else {
                    // 如果已经发送完成, 则执行异步读取操作
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    //
                    client.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer buffer) {
                            buffer.flip();
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            String body;

                            try {
                                body = new String(bytes, "UTF-8");
                                System.out.println("Now is: " + body);

                                latch.countDown();

                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            //当读取出现异常的时候，关闭链路，同时调用CountDownLatch的countDown方法让
                            //AsyncTimeClientHandler线程执行完毕，客户端退出执行。
                            try {
                                client.close();
                                latch.countDown();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    client.close();
                    latch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void failed(Throwable exc, AsyncTimeClientHandler attachment) {
        exc.printStackTrace();
        try {
            client.close();
            latch.countDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
