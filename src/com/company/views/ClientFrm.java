package com.company.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientFrm extends JFrame {
    private JButton btnDownLoad;
    private JButton btnUpload;
    private DefaultListModel<String> listModel;
    private ArrayList<File> listFile;
    private JList<String> list;
    private Socket s;
    private ObjectOutputStream out;
    private final int PORT = 3000;
    private final String hostName = "localhost";
    private StatusDialog dialog;
    public ClientFrm(String title) {
        super(title);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        try {
            s = new Socket("localhost", PORT);
            out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // start read thread;
        ClientReadThread readThread = new ClientReadThread(this, s);
        Thread t1 = new Thread(readThread);
        t1.start();

        // request all file in list
        getAllFileNameFromServer();

        Container mainContainer = this.getContentPane();
        mainContainer.setLayout(new BorderLayout(4,4));

        //topPanel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(new JLabel("Client"));
        mainContainer.add(topPanel, BorderLayout.NORTH);

        // CenterPanel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        listModel = new DefaultListModel<>();

        list = new JList<>(listModel);

        centerPanel.add(list);
        mainContainer.add(centerPanel, BorderLayout.CENTER);

        // bottomPanel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        btnDownLoad = new JButton("Download");
        btnUpload = new JButton("Upload");
        btnDownLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int index = list.getSelectedIndex();
                if(index != -1) {
                    String fileName = listFile.get(index).getName();
                    Message msg = new Message("DOWNLOAD", fileName);
                    ClientWriteThread write = new ClientWriteThread(s, msg, out);
                    Thread t = new Thread(write);
                    t.start();
                    dialog = new StatusDialog(ClientFrm.this);
                } else {
                    JOptionPane.showMessageDialog(ClientFrm.this, "Vui long chon file!");
                }
            }
        });

        btnUpload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogTitle("Select file to upload");
                int userSelection = jFileChooser.showOpenDialog(ClientFrm.this);
                if(userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToUpload = jFileChooser.getSelectedFile();
                    String fileName = fileToUpload.getName();

                    byte[] fileContentBytes = new byte[(int) fileToUpload.length()];
                    try {
                        FileInputStream fin = new FileInputStream(fileToUpload.getAbsolutePath());
                        fin.read(fileContentBytes);
                        fin.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    dialog = new StatusDialog(ClientFrm.this);
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                dialog.setVisible(true);
                                Message sendMsg = new Message("UPLOAD", fileName);
                                out.writeObject(sendMsg);
                                out.writeInt(fileContentBytes.length);
                                out.write(fileContentBytes);
                                out.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    t.start();

                } else {
                    JOptionPane.showMessageDialog(ClientFrm.this, "Chọn file không thành công!");
                }
            }
        });

        bottomPanel.add(btnDownLoad);
        bottomPanel.add(btnUpload);
        mainContainer.add(bottomPanel, BorderLayout.SOUTH);
    }

    public void setListFile(ArrayList<File> listFile) {
        listModel.clear();
        this.listFile = listFile;
        for (File fileEntry : listFile) {
            listModel.addElement(fileEntry.getName());
        }
    }

    public void getAllFileNameFromServer() {
        Message msg = new Message("GET_ALL_FILENAME");
        ClientWriteThread writer = new ClientWriteThread(s, msg, out);
        Thread t = new Thread(writer);
        t.start();
    }

    public StatusDialog getDialog() {
        return dialog;
    }

    public static void main(String[] args) {
        ClientFrm clientFrm = new ClientFrm("Client");
        clientFrm.setVisible(true);
    }
}
