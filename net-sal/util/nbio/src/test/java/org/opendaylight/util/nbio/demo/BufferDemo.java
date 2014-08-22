/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio.demo;

import org.opendaylight.util.StringUtils;

import java.io.IOException;

/**
 * A graphical demonstration of how the NBIO buffers / IO Loops work.
 *
 * @author Simon Hunt
 */
public class BufferDemo {

    // ****** CONFIGURATION ******
    private static final String TITLE = "BufferDemo v1.0";

    // size of the display window
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    // number of worker threads (IO Loops)
    private static final int NUM_WORKERS = 2;

    // listen port
    private static final int PORT = 8888;

    // size of socket send and receive buffers
    private static final int SOCK_BUFFER_SIZE = 256;

    private BufferModel model;
    private Display display;

    public BufferDemo() {
        try {
            model = new BufferModel(PORT, SOCK_BUFFER_SIZE, NUM_WORKERS);
            display = new Display(TITLE, WIDTH, HEIGHT, model);
        } catch (IOException e) {
            System.err.println("Ooopsies!!!");
            e.printStackTrace();
        }
    }

    /**
     * Main entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        printConfig();
        new BufferDemo();
    }

    private static void printConfig() {
        print(TITLE);
        print("Listen port: {}", PORT);
        print("Socket buffer size: {}", SOCK_BUFFER_SIZE);
        print("# Workers: {}", NUM_WORKERS);
    }

    private static void print(String fmt, Object... items) {
        System.out.println(StringUtils.format(fmt, items));
    }

}
