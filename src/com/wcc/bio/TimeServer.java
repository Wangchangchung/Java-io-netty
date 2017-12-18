package com.wcc.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by charse on 17-6-19.
 */
public class TimeServer {

    public  static  void main(String[] args){
        int port = 8080;
        if (args !=null && args.length > 0){
            try {
                port = Integer.valueOf(args[0]);
            }catch (NumberFormatException e){

            }
        }

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("The Server is start in port:" + port);
            Socket socket = null;

            while (true) {

                socket = serverSocket.accept();
                new Thread();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (serverSocket != null){
                System.out.println("The Server is close！");
                try {
                    serverSocket.close();
                    serverSocket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
