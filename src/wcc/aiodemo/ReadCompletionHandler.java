package wcc.aiodemo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;

/**
 * Created by charse on 17-6-23.ThreadPoolExecutor来执行回调通知，异步回调通知类sun.nio.ch.AsynchronousChannelGroupImpl
 * 实现, 它进过层层调用，最终回调com.phei.netty.aio.AsyncTimeClinetHandler$1.completed 方法，完成回调通知
 * 由此我们也可以得出结论： 异步Socket Channel是被动执行对象, 我们不需要向NIO编程那样创建一恶搞独立的I/O
 * 线程来处理读写操作， 对于AsynchronousServerSocketChannel和AsynchronousSocketChannel,他们都由JDK底层的线程池负责回调
 * 并驱动读写操作，正是因为如此，基于NIO 2.0 新的异步非阻塞Channal 进行编程比NIO编程更加简单
 *
 */
public class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

    public  AsynchronousSocketChannel  channel;
    /*
        构造方法 AsynchronousSocketChannel 通过参数传递到ReadCompletionHandler中
        当作成员变量来使用，主要用于读取半包消息和发送应答，
     */

    public  ReadCompletionHandler(AsynchronousSocketChannel channel){
        if (this.channel == null){
            this.channel = channel;
        }
    }


    // 读取到消息后的处理，首先对 attachment 进行 flip操作
    // 为了后续从缓冲区读取请求消息，对请求消息进行判断，如果是QUERY TIME ORDER
    // 则获取当前的系统的服务器的时间，调用doWirte 方法发送给客户端
    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        attachment.flip();
        byte[] body = new byte[attachment.remaining()];
        attachment.get(body);
        String req = null;
        try {
            req = new String(body, "UTF-8");
            System.out.println("The time server receive order:" + req );
            String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(req) ? new Date(System.currentTimeMillis()).toString()
                    : "BAD ORDER";

            doWrite(currentTime);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        try {
            this.channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void doWrite(String currentTime){
        // 对当前时间的合法性进行校验，如果是合法的，我
        if (currentTime != null && currentTime.trim().length() > 0){

            // 如果合法 调用字符串的解码方法，将应答消息编码成字节数组，然后将它复制到
            // 发送缓冲区中 writeBuffer中, 最后调用AsynchronousSocketChannel 的异步write
            // 方法
            byte[] bytes = currentTime.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();

            channel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    // 如果还没有发送完成，那么就再进行发送
                    if (attachment.hasRemaining()){
                        channel.write(attachment, attachment, this);
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        // 当发生异常的时候,对异常Throwable 进行判断:  如果是其他异常, 按照
                        // 自己的业务逻辑来处理即可。
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
