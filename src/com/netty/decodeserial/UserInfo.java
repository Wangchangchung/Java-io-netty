package com.netty.decodeserial;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created by charse on 17-8-25.
 */
public class UserInfo implements Serializable{

    private static final long serialVersionUID = 1L;

    private String userName;

    private int userID;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }


    public byte[] codeUser(){
        ByteBuffer buf = ByteBuffer.allocate(1024);
        byte[] value = this.userName.getBytes();
        buf.putInt(value.length);
        buf.put(value);
        buf.putInt(this.userID);
        buf.flip();

        byte[] reslut = new byte[buf.remaining()];
        buf.get(reslut);
        return  reslut;
    }
}
