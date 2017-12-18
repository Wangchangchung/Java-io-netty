package com.wcc.buffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Created by charse on 17-7-9.
 */
public class BufferDemo {

    public static void  printBuffer(){

        //15  个字节大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(15);

        System.out.println("limit=" + buffer.limit() +
                " capacity=" + buffer.capacity() +" position="+buffer.position());
        //存入10个字节数据
        for (int i = 0; i <10; ++i){
            buffer.put((byte)i);
            System.out.println("buffer.put(): limit=" + buffer.limit() +
                    " capacity=" + buffer.capacity() +" position="+buffer.position());
        }

        System.out.println("进行buffer.flip()之前：");
        System.out.println("limit=" + buffer.limit() +" capacity=" + buffer.capacity()
            +" position="+ buffer.position());

        //重置position
        buffer.flip();

        System.out.println("进行了buffer.flip()之后:");
        System.out.println("limit=" + buffer.limit() +" capacity=" + buffer.capacity()
                +" position="+ buffer.position());

        for (int i = 0; i < 5; ++i){
            System.out.println(buffer.get(i));
            System.out.println("buffer.get() : limit=" + buffer.limit() +" capacity=" + buffer.capacity()
                    +" position="+ buffer.position());

        }

        System.out.println("buffer.flip()之前：");
        System.out.println("limit=" + buffer.limit() +" capacity=" + buffer.capacity()
                +" position="+ buffer.position());

        buffer.flip();
        System.out.println("buffer.flip()之后：");
        System.out.println("limit=" + buffer.limit() +" capacity=" + buffer.capacity()
                +" position="+ buffer.position());

        System.out.println("又一次的使用了buffer.get()");

        //此时再进行读　出现了异常
        //System.out.println(buffer.get(7));
        System.out.println("buffer.get() : limit=" + buffer.limit() +" capacity=" + buffer.capacity()
                    +"嗯 position="+ buffer.position());

    }

    public static void main(String[] args){
        printBuffer();
    }
}
