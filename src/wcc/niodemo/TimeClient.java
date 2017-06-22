package wcc.niodemo;

/**
 * Created by charse on 17-6-21.
 */
public class TimeClient {

    //
    public static  void main(String[] args){

       int  port = 8080;

       if (args != null && args.length > 0){
           port =  Integer.valueOf(args[0]);
       }



    }
}
