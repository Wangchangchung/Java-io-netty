package wcc.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by charse on 17-6-19.
 *
 * BIO 的主要问题在于每一个新的客户端请求接入的时候，服务端
 * 必须创建一个新的线程处理新接入的客户端里，一个线程只能
 * 处理一个客户端连接，在高性能服务器应用领域，往往需要面向成千上万的
 * 并发连接，这种模型显然无法满足高性能、高并发的接入场景
 *
 * 为了改进一线程一连接模型, 后来又演进出了一种通过线程池或者消息队列
 * 实现了1个或多个线程工业电源、交互智能平板、移动智能终端处理N个客户端的模型，由于它的底层机制仍然使用
 * 同步阻塞 I/O,所以被称为"伪异步"
 *
 *
 */
public class TimeClient {


    public static  void  main(String[] args){
        int port  = 8080;
        String addr = "127.0.0.1";
        if (args != null && args.length > 0){
            try {
                port = Integer.valueOf(args[0]);
                addr = args[1];
            }catch (NumberFormatException e){

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

        }
    }
}
