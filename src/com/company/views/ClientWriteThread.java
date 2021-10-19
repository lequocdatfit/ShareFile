package com.company.views;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientWriteThread implements Runnable{
    private Socket s;
    private ObjectOutputStream out;
    private Message msg;

    public ClientWriteThread(Socket s, Message msg, ObjectOutputStream out) {
        this.s = s;
        this.msg = msg;
        this.out = out;
    }
    @Override
    public void run() {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
