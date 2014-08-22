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
 * Our UI display.
 *
 * @author Simon Hunt
 */
public class Display extends JFrame {

    private final BufferModel model;

    private Canvas canvas;
    private DemoControls controls;

    /**
     * Constructs the display and makes it visible.
     *
     * @param title the title to display in the JFrame window
     * @param width the display width, in pixels
     * @param height the display height, in pixels
     * @param model the buffer model we are going to display
     */
    Display(String title, int width, int height, BufferModel model) {
        super(title);
        this.model = model;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new MyListener());

        setBounds(0, 0, width, height);
        setUpContent();
        setVisible(true);
    }

    private void setUpContent() {
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(canvas = new Canvas(model), BorderLayout.CENTER);
        c.add(controls = new DemoControls(model), BorderLayout.SOUTH);
    }

    private class MyListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            System.out.println("Exiting...");
            model.stop();
            controls.closeAllClients();
            System.out.println("DONE");
        }
    }
}
