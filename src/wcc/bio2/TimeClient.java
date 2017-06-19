package wcc.bio2;

import java.io.*;
import java.net.Socket;

/**
 * Created by charse on 17-6-19.
 *
 * 由于线程池和消息队列都是有界的, 因此无论客户端并发连接数多大
 * 他都不会导致线程个数过于膨胀或者内存溢出，相比于传统的一连接
 * 一线程模型，是一种改良。
 *
 * 伪异步I/O 通信框架采取了线程池实现, 因此避免可为每一个请求
 * 创建一个独立线程造成的线程资源耗尽问题, 但是由于它层的通信依然
 * 采用同步阻塞模型，因此无法从根本上解决BIO 的问题。
 *
 */
public class TimeClient {

    public static  void  main(String[] args) throws FileNotFoundException {
        int port  = 8080;
        String addr = "127.0.0.1";
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
                addr = args[1];
            } catch (NumberFormatException e) {

            }
        }

            Socket socket = null;
            BufferedReader  in = null;
            PrintWriter out = null;

            try {
                socket = new Socket(addr, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("QUERY TIME ORDER");
                System.out.println("Send order 2 server succeed.");
                String resp = in.readLine();

                System.out.println("Now is:" + resp);

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                //关闭操作
                if (out != null){
                    out.close();
                    out = null;
                }
                if (in != null){
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    in = null;
                }

                if (socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    socket = null;
                }
            }


        InputStream  inputStream = new FileInputStream("D://file.txt");
        OutputStream outputStream = new FileOutputStream("/");

        try {
            inputStream.read();
            outputStream.write(6);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
