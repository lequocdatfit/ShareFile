package com.company.views;

import javax.swing.*;
import java.awt.*;

public class StatusDialog extends JDialog {
    private ClientFrm frm;
    private JLabel lbStatus;
    public StatusDialog(Frame parent) {
        super(parent);
        frm = (ClientFrm) parent;
        this.setSize(200, 200);
        setLocationRelativeTo(parent);

        Container mainContainer = this.getContentPane();
        mainContainer.setLayout(new BorderLayout());

        // centerPanel
        JPanel centerPanel = new JPanel();
        lbStatus = new JLabel("0%");
        centerPanel.add(lbStatus);

        mainContainer.add(centerPanel, BorderLayout.CENTER);
    }

    public void setLbStatusText(String text) {
        this.lbStatus.setText(text);
    }
}
