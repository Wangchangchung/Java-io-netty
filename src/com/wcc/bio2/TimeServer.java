package com.wcc.bio2;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by charse on 17-6-19.
 *
 *
 *
 */
public class TimeServer {

    public  static  void  main(String[] args){
        int port = 8080;

        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {

            }
        }
            ServerSocket serverSocket  = null;

            try {
                    //创建线程池
                    TimeServerHandlerExecutePool singleExecutor = new
                            TimeServerHandlerExecutePool(50, 10000);
                    serverSocket = new ServerSocket(port);
                    System.out.println("The time server is start in port: " +  port);

                    Socket socket = null;
                    while (true) {
                        // 接受一个线程请求
                        socket = serverSocket.accept();
                        //  当接收到 新的客户端请求的时候, 将Socket 封装成一个Task
                        // 然后调用线程池的 execute 方法执行，从而避免了每个请求接入都创建一个新的线程
                        singleExecutor.execute(new TimeServerHandler(socket));

                        System.out.println("------------------");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    // 关闭Socket
                    if (serverSocket != null){
                        System.out.println("The time server close");
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        serverSocket = null;
                    }
                }
    }
}
