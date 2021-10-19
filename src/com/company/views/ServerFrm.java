package com.company.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerFrm extends JFrame {
    private JButton btnStart;
    private JButton btnStop;
    private DefaultListModel<String> listModel;
    private ArrayList<String> fileName;
    private JList<String> list;
    private final int PORT = 3000;
    private final File folder = new File("ShareFolder");
    private ArrayList<File> listFiles;
    // SocketServerThread giao tiep truc tiep voi clients
    private ArrayList<SocketServerThread> listSocketServer;
    private static ExecutorService pool = Executors.newFixedThreadPool(100);
    public ServerFrm(String title) {
        super(title);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(400, 300);
        this.setLocationRelativeTo(null);

        listSocketServer = new ArrayList<>();
        listFiles = new ArrayList<>();

        Container mainContainer = this.getContentPane();
        mainContainer.setLayout(new BorderLayout(4,4));
        mainContainer.setBackground(Color.GRAY);

        //topPanel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(new Label("Server"));

        mainContainer.add(topPanel, BorderLayout.NORTH);

        //centerPanel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        listModel = new DefaultListModel<>();
        listFilesForFolder(folder);
        list = new JList<>(listModel);
        centerPanel.add(list);
        mainContainer.add(centerPanel, BorderLayout.CENTER);

        //bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        btnStart = new JButton("Start");
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Thread listenThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ServerSocket ss = new ServerSocket(PORT);
                            while (true) {
                                Socket s = ss.accept();
                                SocketServerThread socketServer = new SocketServerThread(s, ServerFrm.this, listSocketServer);
                                listSocketServer.add(socketServer);
                                pool.execute(socketServer);
                            }
                        } catch (IOException exc) {
                            exc.printStackTrace();
                        }
                    }
                });
                listenThread.start();
                JOptionPane.showMessageDialog(ServerFrm.this, "Server start");
            }
        });

        bottomPanel.add(btnStart);

        btnStop = new JButton("Stop");
        btnStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

            }
        });
        bottomPanel.add(btnStop);
        mainContainer.add(bottomPanel, BorderLayout.SOUTH);


    }

    public File getFolder() {
        return folder;
    }

    public ArrayList<File> getListFiles() {
        return listFiles;
    }

    public void listFilesForFolder(File folder) {
        for(File fileEntry : folder.listFiles()) {
            if(fileEntry.isDirectory()) {
                listFilesForFolder(folder);
            } else {
                listFiles.add(fileEntry);
                listModel.addElement(fileEntry.getName());
            }
        }
    }

    public static void main(String[] args) {
        ServerFrm frm = new ServerFrm("Server");
        frm.setVisible(true);
    }
}