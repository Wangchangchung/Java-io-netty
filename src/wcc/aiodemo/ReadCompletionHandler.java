package wcc.aiodemo;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by charse on 17-6-23.
 */
public class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

    public  AsynchronousSocketChannel  channel;

    public  ReadCompletionHandler(AsynchronousSocketChannel channel){
        if (this.channel == null){
            this.channel = channel;
        }
    }


    @Override
    public void completed(Integer result, ByteBuffer attachment) {

    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {

    }
}
