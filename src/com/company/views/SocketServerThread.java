package com.company.views;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

public class SocketServerThread implements Runnable{
    private Socket s;
    private ArrayList<SocketServerThread> listSocketServer;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ServerFrm frm;
    public SocketServerThread(Socket s, ServerFrm frm, ArrayList<SocketServerThread> listSocketServer) {
        this.frm = frm;
        this.listSocketServer = listSocketServer;
        try {
            in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
            out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        try {
            Message msg = null;
            Message sendMsg = null;
            do {
                msg = (Message) in.readObject();
                switch (msg.getType()) {
                    case "GET_ALL_FILENAME":
                        sendMsg = new Message("RETURN_ALL_FILENAME", frm.getListFiles());
                        out.writeObject(sendMsg);
                        out.flush();
                        break;
                    case "DOWNLOAD":
                        System.out.println("Download file: " + msg.getPayload());
                        File matchedFile = null;
                        for (File file : frm.getListFiles()) {
                            if(file.getName().equals(msg.getPayload())) {
                                matchedFile = file;
                                break;
                            }
                            matchedFile = file;
                        }
                        FileInputStream fin = new FileInputStream(matchedFile.getAbsolutePath());
                        String fileName = matchedFile.getName();

                        byte[] fileContentBytes = new byte[(int) matchedFile.length()];
                        fin.read(fileContentBytes);
                        fin.close();

                        sendMsg = new Message("RETURN_FILE", fileName);
                        out.writeObject(sendMsg);
                        out.flush();
                        out.writeInt(fileContentBytes.length);
                        out.flush();
                        out.write(fileContentBytes);
                        out.flush();
                        break;
                    case "UPLOAD":
                        System.out.println("Upload file: " + msg.getPayload());
                        String fileUploadName = (String) msg.getPayload();

                        int fileContentLength =  in.readInt();
                        if(fileContentLength > 0) {
                            byte[] readBuffer = new byte[1024];
                            //in.readFully(fileUploadBytes, 0, fileContentLength);
                            FileOutputStream fout = new FileOutputStream("ShareFolder/" + fileUploadName);
                            int counter = 0;
                            int i = 0;
                            while (counter < fileContentLength) {
                                i = in.read(readBuffer);
                                fout.write(readBuffer, 0, i);
                                counter +=i;
                                Message uploadProgressMsg = new Message("UPLOAD_PROGRESS", String.valueOf(counter * 100/ fileContentLength));
                                out.writeObject(uploadProgressMsg);
                                out.flush();
                            }
                            fout.close();
                            System.out.println("Upload done!");
                        }
                        break;
                    default:
                        break;
                }
            } while (true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}