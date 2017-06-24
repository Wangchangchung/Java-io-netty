package wcc.aiodemo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;

/**
 * Created by charse on 17-6-23.
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

            byte[] bytes = currentTime.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();

            channel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    if (attachment.hasRemaining()){
                        channel.write(attachment, attachment, this);
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
