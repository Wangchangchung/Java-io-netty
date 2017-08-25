package com.netty.decodeserial;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by charse on 17-8-25.
 */
public class TestUserInfoSize {

    public static void main(String[] args) throws IOException {

        UserInfo userInfo = new  UserInfo();
        userInfo.setUserID(1000);
        userInfo.setUserName("hello world ! ML");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(userInfo);
        objectOutputStream.flush();
        objectOutputStream.close();

        byte[] b = byteArrayOutputStream.toByteArray();
        System.out.println("JDK　serializable length is:" +  b.length);


        objectOutputStream.close();
        System.out.println("the bye array serial length is :" + userInfo.codeUser().length);



    }
}
