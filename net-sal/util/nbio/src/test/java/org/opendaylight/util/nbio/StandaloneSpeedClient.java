/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import org.opendaylight.util.Task;
import org.opendaylight.util.ThroughputTracker;
import org.opendaylight.util.net.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.opendaylight.util.NamedThreadFactory.namedThreads;

/**
 * Auxiliary test fixture to measure speed of NIO-based channels.
 *
 * @author Thomas Vachuska
 */
public class StandaloneSpeedClient {

    private static Logger log = LoggerFactory.getLogger(StandaloneSpeedClient.class);

    private static final long TIMEOUT = 1000;

    private final IpAddress ip;
    private final int port;
    private final int msgCount;
    private final int msgLength;

    private final List<CustomIOLoop> iloops = new ArrayList<>();
    private final ExecutorService ipool;
    private final ExecutorService wpool;

    ThroughputTracker messages;
    ThroughputTracker bytes;

    /**
     * Main entry point to launch the server
     *
     * @param args command-line arguments
     * @throws IOException if unable to bind server socket
     * @throws InterruptedException if latch wait gets interrupted
     * @throws ExecutionException if wait gets interrupted
     * @throws TimeoutException if timeout occurred while waiting for completion
     */
    public static void main(String args[]) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        IpAddress ip = args.length > 0 ? IpAddress.valueOf(args[0]) : IpAddress.LOOPBACK_IPv4;
        int wc = args.length > 1 ? Integer.parseInt(args[1]) : 6;
        int mc = args.length > 2 ? Integer.parseInt(args[2]) : 50 * 1000000;
        int ml = args.length > 3 ? Integer.parseInt(args[3]) : 128;
        int to = args.length > 4 ? Integer.parseInt(args[4]) : 30;

        log.info("Setting up client with {} workers sending {} {}-byte messages to {} server... ",
                 wc, mc, ml, ip);
        StandaloneSpeedClient sc = new StandaloneSpeedClient(ip, wc, mc, ml, StandaloneSpeedServer.PORT);

        sc.start();
        Task.delay(2000);

        sc.await(to);
        sc.report();

        System.exit(0);
    }

    /**
     * Creates a speed client.
     *
     * @param ip ip address of server
     * @param wc worker count
     * @param mc message count to send per client
     * @param ml message length in bytes
     * @param port socket port
     * @throws IOException if unable to create IO loops
     */
    public StandaloneSpeedClient(IpAddress ip, int wc, int mc, int ml, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        this.msgCount = mc;
        this.msgLength = ml;
        this.wpool = Executors.newFixedThreadPool(wc, namedThreads("worker"));
        this.ipool = Executors.newFixedThreadPool(wc, namedThreads("io-loop"));

        for (int i = 0; i < wc; i++)
            iloops.add(new CustomIOLoop());
    }

    /**
     * Starts the client workers.
     *
     * @throws IOException if unable to open connection
     */
    public void start() throws IOException {
        messages = new ThroughputTracker();
        bytes = new ThroughputTracker();

        // First start up all the IO loops
        for (CustomIOLoop l : iloops)
            ipool.execute(l);

        // Wait for all of them to get going
        for (CustomIOLoop l : iloops)
            l.waitForStart(TIMEOUT);

        // ... and Next open all connections; one-per-loop
        for (CustomIOLoop l : iloops)
            openConnection(l);
    }


    /**
     * Initiates open connection request and registers the pending socket
     * channel with the given IO loop.
     *
     * @param loop loop with which the channel should be registered
     * @throws IOException if the socket could not be open or connected
     */
    private void openConnection(CustomIOLoop loop) throws IOException {
        SocketAddress sa = new InetSocketAddress(ip.toInetAddress(), port);
        SocketChannel ch = SocketChannel.open();
        ch.configureBlocking(false);
        loop.registerConnect(ch, null);
        ch.connect(sa);
    }


    /**
     * Waits for the client workers to complete.
     *
     * @param secs timeout in seconds
     * @throws ExecutionException if execution failed
     * @throws InterruptedException if interrupt occurred while waiting
     * @throws TimeoutException if timeout occurred
     */
    public void await(int secs) throws InterruptedException,
                                    ExecutionException, TimeoutException {
        for (CustomIOLoop l : iloops)
            if (l.worker.task != null)
                l.worker.task.get(secs, TimeUnit.SECONDS);
        messages.freeze();
        bytes.freeze();
    }

    /** Reports on the accumulated throughput trackers. */
    public void report() {
        DecimalFormat f = new DecimalFormat("#,##0");
        log.info("{} messages; {} bytes; {} mps; {} Mbs",
                 f.format(messages.total()),
                 f.format(bytes.total()),
                 f.format(messages.throughput()),
                 f.format(bytes.throughput() / (1024 * 128)));
    }


    // Loop for transfer of fixed-length messages
    private class CustomIOLoop extends IOLoop<FixedLengthMessage, FixedLengthMessageBuffer> {

        Worker worker = new Worker();

        public CustomIOLoop() throws IOException {
            super();
        }


        @Override
        protected FixedLengthMessageBuffer createBuffer(ByteChannel ch,
                                                        SSLContext sslContext) {
            return new FixedLengthMessageBuffer(msgLength, ch, this, sslContext);
        }

        @Override
        protected synchronized void discardBuffer(MessageBuffer<FixedLengthMessage> b) {
            super.discardBuffer(b);

            messages.add(b.inMessages().total());
            bytes.add(b.inBytes().total());
            b.inMessages().reset();
            b.inBytes().reset();

            log.info("Disconnected client; inbound {} mps, {} Mbps; outbound {} mps, {} Mbps",
                     StandaloneSpeedServer.format.format(b.inMessages().throughput()),
                     StandaloneSpeedServer.format.format(b.inBytes().throughput() / (1024 * 128)),
                     StandaloneSpeedServer.format.format(b.outMessages().throughput()),
                     StandaloneSpeedServer.format.format(b.outBytes().throughput() / (1024 * 128)));
        }

        @Override
        protected void processMessages(MessageBuffer<FixedLengthMessage> b,
                                       List<FixedLengthMessage> messages) {
            worker.release(messages.size());
        }

        @Override
        protected void connect(SelectionKey key) {
            super.connect(key);

            FixedLengthMessageBuffer b = (FixedLengthMessageBuffer) key.attachment();
            Worker w = ((CustomIOLoop) b.loop()).worker;
            w.pump(b);
        }

    }

    /**
     * Auxiliary worker to connect and pump batched messages using blocking I/O.
     */
    private class Worker implements Runnable {

        private static final int BATCH_SIZE = 1000;
        private static final int PERMITS = 2 * BATCH_SIZE;

        private FixedLengthMessageBuffer b;
        private FutureTask<Worker> task;

        // Stuff to throttle pump
        private final Semaphore semaphore = new Semaphore(PERMITS);
        private int msgWritten;

        void pump(FixedLengthMessageBuffer b) {
            this.b = b;
            wpool.execute(task = new FutureTask<>(this, this));
        }

        @Override
        public void run() {
            try {
                log.info("Worker started...");

                List<FixedLengthMessage> batch = new ArrayList<>();
                for (int i = 0; i < BATCH_SIZE; i++)
                    batch.add(new FixedLengthMessage(msgLength));

                while (msgWritten < msgCount)
                    msgWritten += writeBatch(b, batch);

                // Now try to get all the permits back before sending poison pill
                semaphore.acquireUninterruptibly(PERMITS);
                b.discard();

                log.info("Worker done...");

            } catch (IOException e) {
                log.error("Worker unable to perform I/O", e);
            }
        }


        private int writeBatch(FixedLengthMessageBuffer b,
                               List<FixedLengthMessage> batch)
                throws IOException {
            int count = Math.min(BATCH_SIZE, msgCount - msgWritten);
            acquire(count);
            if (count == BATCH_SIZE)
                b.queue(batch);
            else {
                for (int i = 0; i < count; i++)
                    b.queue(batch.get(i));
            }
            return count;
        }


        // Release permits based on the specified number of message credits
        private void release(int permits) {
            semaphore.release(permits);
        }

        // Acquire permit for a single batch
        private void acquire(int permits) {
            semaphore.acquireUninterruptibly(permits);
        }

    }

}
