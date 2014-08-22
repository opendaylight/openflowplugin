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
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.opendaylight.util.NamedThreadFactory.namedThreads;

/**
 * Auxiliary test fixture to measure speed of NIO-based channels.
 * <p>
 * Internal operation based on the algorithms used by the Beacon I/O loop
 * authored by David Erickson.
 *
 * @author Thomas Vachuska
 */
public class StandaloneSpeedServer {

    private static Logger log = LoggerFactory.getLogger(StandaloneSpeedServer.class);

    private static final int PRUNE_FREQUENCY = 1000;

    static final int PORT = 9696;
    static final long TIMEOUT = 1000;

    static final boolean SO_NO_DELAY = false;
    static final int SO_SEND_BUFFER_SIZE = 1024 * 1024;
    static final int SO_RCV_BUFFER_SIZE = 1024 * 1024;

    static final DecimalFormat format = new DecimalFormat("#,##0");

    private final AcceptLoop aloop;
    private final ExecutorService apool = Executors.newSingleThreadExecutor(namedThreads("accept"));

    private final List<CustomIOLoop> iloops = new ArrayList<>();
    private final ExecutorService ipool;

    private final int workerCount;
    private final int msgLength;
    private int lastWorker = -1;

    ThroughputTracker messages;
    ThroughputTracker bytes;

    /**
     * Main entry point to launch the server
     *
     * @param args command-line arguments
     * @throws IOException if unable to crate IO loops
     */
    public static void main(String args[]) throws IOException {
        IpAddress ip = args.length > 0 ? IpAddress.valueOf(args[0]) : IpAddress.LOOPBACK_IPv4;
        int wc = args.length > 1 ? Integer.parseInt(args[1]) : 6;
        int ml = args.length > 2 ? Integer.parseInt(args[2]) : 128;

        log.info("Setting up the server with {} workers, {} byte messages on {}... ",
                 wc, ml, ip);
        StandaloneSpeedServer ss = new StandaloneSpeedServer(ip, wc, ml, PORT);
        ss.start();

        // Start pruning clients.
        while (true) {
            Task.delay(PRUNE_FREQUENCY);
            ss.prune();
        }
    }

    /**
     * Creates a speed server.
     *
     * @param ip optional ip of the adapter where to bind
     * @param wc worker count
     * @param ml message length in bytes
     * @param port listen port
     * @throws IOException if unable to create IO loops
     */
    public StandaloneSpeedServer(IpAddress ip, int wc, int ml, int port) throws IOException {
        this.workerCount = wc;
        this.msgLength = ml;
        this.ipool = Executors.newFixedThreadPool(workerCount, namedThreads("io-loop"));

        this.aloop = new CustomAcceptLoop(new InetSocketAddress(ip.toInetAddress(), port));
        for (int i = 0; i < workerCount; i++)
            iloops.add(new CustomIOLoop());
    }

    /** Start the server IO loops and kicks off throughput tracking. */
    public void start() {
        messages = new ThroughputTracker();
        bytes = new ThroughputTracker();

        for (CustomIOLoop l : iloops)
            ipool.execute(l);
        apool.execute(aloop);

        for (CustomIOLoop l : iloops)
            l.waitForStart(TIMEOUT);
        aloop.waitForStart(TIMEOUT);
    }

    /** Stop the server IO loops and freezes throughput tracking. */
    public void stop() {
        aloop.cease();
        for (CustomIOLoop l : iloops)
            l.cease();

        for (CustomIOLoop l : iloops)
            l.waitForFinish(TIMEOUT);
        aloop.waitForFinish(TIMEOUT);

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

    /** Prunes the IO loops of stale message buffers. */
    public void prune() {
        for (CustomIOLoop l : iloops)
            l.prune();
    }

    // Get the next worker to which a client should be assigned
    private synchronized CustomIOLoop nextWorker() {
        lastWorker = (lastWorker + 1) % workerCount;
        return iloops.get(lastWorker);
    }

    // Loop for transfer of fixed-length messages
    private class CustomIOLoop extends IOLoop<FixedLengthMessage, FixedLengthMessageBuffer> {

        public CustomIOLoop() throws IOException {
            super();
        }

        @Override
        protected FixedLengthMessageBuffer createBuffer(ByteChannel ch,
                                                        SSLContext sslContext) {
            return new FixedLengthMessageBuffer(msgLength, ch, this, sslContext);
        }

        @Override
        protected void discardBuffer(MessageBuffer<FixedLengthMessage> b) {
            super.discardBuffer(b);

            messages.add(b.inMessages().total());
            bytes.add(b.inBytes().total());

            log.info("Disconnected client; inbound {} mps, {} Mbps; outbound {} mps, {} Mbps",
                     format.format(b.inMessages().throughput()),
                     format.format(b.inBytes().throughput() / (1024 * 128)),
                     format.format(b.outMessages().throughput()),
                     format.format(b.outBytes().throughput() / (1024 * 128)));
        }

        @Override
        protected void processMessages(MessageBuffer<FixedLengthMessage> b,
                                       List<FixedLengthMessage> messages) {
            try {
                b.queue(messages);
            } catch (IOException e) {
                log.error("Unable to echo messages", e);
            }
        }
    }

    // Loop for accepting client connections
    private class CustomAcceptLoop extends AcceptLoop {

        public CustomAcceptLoop(SocketAddress address) throws IOException {
            super(address);
            this.log = StandaloneSpeedServer.log;
        }

        @Override
        protected void accept(ServerSocketChannel ssc) throws IOException {
            SocketChannel sc = ssc.accept();
            sc.configureBlocking(false);

            Socket so = sc.socket();
            so.setTcpNoDelay(SO_NO_DELAY);
            so.setReceiveBufferSize(SO_RCV_BUFFER_SIZE);
            so.setSendBufferSize(SO_SEND_BUFFER_SIZE);

            nextWorker().registerAccept(sc, null);
            log.info("Connected client");
        }
    }

}
