package com.company.views;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientReadThread implements Runnable{
    private ObjectInputStream in;
    private Socket s;
    private ClientFrm frm;

    public ClientReadThread(ClientFrm frm, Socket s) {
        this.frm = frm;
        this.s = s;
    }

    @Override
    public void run() {

        try {
            in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Message msg = null;
        System.out.println("Read thread started!");
        try {
            while (true) {
                msg = (Message) in.readObject();
                switch (msg.getType()) {
                    case "RETURN_ALL_FILENAME":
                        frm.setListFile((ArrayList<File>) msg.getPayload());
                        System.out.println("Server return all file!");
                        break;
                    case "RETURN_FILE":
                        String fileName = (String) msg.getPayload();
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setDialogTitle("Chọn nơi bạn cần lưu");
                        fileChooser.setSelectedFile(new File(fileName));
                        int userSelection = fileChooser.showSaveDialog(frm);
                        if (userSelection == JFileChooser.APPROVE_OPTION) {
                            File fileToDownLoad = fileChooser.getSelectedFile();
                            frm.getDialog().setVisible(true);
                            int fileContentLength = in.readInt();
                            try {
                                FileOutputStream fout = new FileOutputStream(fileToDownLoad);
                                byte[] readBuffer = new byte[1024];
                                int counter = 0;
                                int i = 0;
                                while (counter < fileContentLength) {
                                    i = in.read(readBuffer);
                                    fout.write(readBuffer, 0, i);
                                    counter+=i;
                                    int finalCounter = counter;
                                    SwingUtilities.invokeLater(()-> {
                                        frm.getDialog().setLbStatusText(String.valueOf(finalCounter * 100/ fileContentLength) + "%");
                                    });
                                }
                                fout.close();

                                JOptionPane.showMessageDialog(frm, "Lưu thành công!");
                            } catch (IOException err) {
                                err.printStackTrace();
                            }

                        }
                        break;
                    case "UPLOAD_PROGRESS":
                        Message finalMsg = msg;
                        SwingUtilities.invokeLater(() -> {
                            frm.getDialog().setLbStatusText((String) finalMsg.getPayload() + "%");
                        });
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
