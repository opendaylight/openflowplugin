/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A client to connect to the demo server.
 *
 * @author Simon Hunt
 */
public class DemoClient extends JFrame {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 200;

    private static final String CLIENT = "Client ";

    private final int id;
    private final ClientPane pane = new ClientPane(this);
    private final ClientControls controls = new ClientControls(this);

    private boolean disposedOf = false;


    DemoClient(int id) {
        super(CLIENT + id);
        this.id = id;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new MyListener());

        setBounds(0, 0, WIDTH, HEIGHT);
        setUpContent();
        setVisible(true);
        System.out.println(name() + " created.");
    }

    private String name() {
        return CLIENT + id;
    }

    private void setUpContent() {
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(pane, BorderLayout.CENTER);
        c.add(controls, BorderLayout.SOUTH);
    }

    /**
     * Closes this client. Invoked when the demo application is shutting down.
     */
    public void close() {
        if (!disposedOf) {
            System.out.println(name() + " closed.");
            dispose();
            disposedOf = true;
        }
    }

    /**
     * Invoked when the connect button is pressed.
     */
    public void connect() {
        System.out.println(name() + " attempting to connect...");
    }

    private class MyListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            close();
        }
    }

}
