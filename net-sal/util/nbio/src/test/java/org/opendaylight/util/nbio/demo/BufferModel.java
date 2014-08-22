/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio.demo;

import org.opendaylight.util.nbio.AcceptLoop;
import org.opendaylight.util.net.IpAddress;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.opendaylight.util.NamedThreadFactory.namedThreads;

/**
 * Encapsulates our IO loops and buffers
 *
 * @author Simon Hunt
 */
public class BufferModel {

    private static final long TIMEOUT = 1000;

    private final AcceptLoop aloop;
    private final ExecutorService apool =
            newSingleThreadExecutor(namedThreads("accept"));

    private final List<DemoIOLoop> iloops = new ArrayList<>();
    private final ExecutorService ipool;

    private final int workerCount;

    private int lastWorker = -1;
    private boolean running = false;

    /**
     * Creates a buffer model for the demo.
     *
     * @param port the listen port for client connections
     * @param sockBufSize the buffer size for send/receive socket buffers
     * @param workerCount the number of workers (IO Loops)
     * @throws IOException if there was a problem
     */
    BufferModel(int port, int sockBufSize, int workerCount) throws IOException {
        this.workerCount = workerCount;
        this.ipool = newFixedThreadPool(workerCount, namedThreads("io-loop"));

        this.aloop = new DemoAcceptLoop(this, serverSocket(port), sockBufSize);
        for (int i=0; i<workerCount; i++)
            iloops.add(new DemoIOLoop());

        System.out.println("BufferModel created.");
    }

    private SocketAddress serverSocket(int port) {
        return new InetSocketAddress(IpAddress.LOOPBACK_IPv4.toInetAddress(), port);
    }

    /**
     * Simple round-robin algorithm for choosing a worker IO loop.
     *
     * @return the next worker
     */
    synchronized DemoIOLoop nextWorker() {
        lastWorker = (lastWorker + 1) % workerCount;
        return iloops.get(lastWorker);
    }

    /**
     * Start the server IO loops and the accept loop.
     *
     * @return true if the loops were started
     */
    synchronized boolean start() {
        if (running) {
            print("Loops already running.");
            return false;
        }
        running = true;

        print("Starting up the IO loops...");
        for (DemoIOLoop loop: iloops)
            ipool.execute(loop);
        apool.execute(aloop);

        for (DemoIOLoop loop: iloops)
            loop.waitForStart(TIMEOUT);
        aloop.waitForStart(TIMEOUT);
        print("..Loops started");
        return true;
    }

    /**
     * Stops the accept loop and IO loops.
     *
     * @return true if the loops were stopped
     */
    synchronized boolean stop() {
        if (!running) {
            print("Loops already stopped.");
            return false;
        }
        running = false;

        print("Stopping the IO loops...");
        aloop.cease();
        for (DemoIOLoop loop: iloops)
            loop.cease();

        for (DemoIOLoop loop: iloops)
            loop.waitForFinish(TIMEOUT);
        aloop.waitForFinish(TIMEOUT);
        print("..Loops stopped");
        return true;
    }

    private void print(String s) {
        System.out.println(s);
    }
}
