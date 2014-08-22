/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.util.nbio.AbstractMessage;
import org.opendaylight.util.nbio.IOLoop;
import org.opendaylight.util.nbio.MessageBuffer;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.BufferId;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.Task;
import org.opendaylight.util.ThroughputTracker;
import org.opendaylight.util.junit.TestTools;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;

import static org.opendaylight.of.controller.impl.StandaloneSpeedServer.format;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.NamedThreadFactory.namedThreads;

/**
 * Auxiliary test fixture to measure speed of packet-in encoding & packet-out
 * decoding from/to NBIO message buffers.
 *
 * @author Thomas Vachuska
 */
public class StandaloneSpeedClient {

    private static Logger log = LoggerFactory.getLogger(StandaloneSpeedClient.class);

    private static final ClassLoader CL = SpeedSwitch.class.getClassLoader();
    protected static final String IMPL_ROOT = "org/opendaylight/of/controller/impl/";
    protected static final String ETH2 = IMPL_ROOT + "eth2-arp-req.hex";
//    protected static final String ETH2 = IMPL_ROOT + "eth2-ip-tcp.hex";

    private static final int PORT = 6633;

    private final IpAddress ip;
    private final int workerCount;
    private final int msgCount;

    private final List<CustomIOLoop> iloops = new ArrayList<CustomIOLoop>();
    private final ExecutorService ipool;
    private final ExecutorService wpool;

    ThroughputTracker messages;
    ThroughputTracker bytes;

    private final byte[] arp;

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
        int mc = args.length > 2 ? Integer.parseInt(args[2]) : 20 * 1000000;
        int to = args.length > 4 ? Integer.parseInt(args[4]) : 120;

        log.info("Setting up client with {} workers sending {} packet-ins to {} server... ",
                 wc, mc, ip);
        StandaloneSpeedClient sc = new StandaloneSpeedClient(ip, wc, mc);

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
     * @throws IOException if unable to launch IO loops
     */
    public StandaloneSpeedClient(IpAddress ip, int wc, int mc) throws IOException {
        this.ip = ip;
        this.workerCount = wc;
        this.msgCount = mc;
        this.wpool = Executors.newFixedThreadPool(workerCount, namedThreads("worker"));
        this.ipool = Executors.newFixedThreadPool(workerCount, namedThreads("io-loop"));

        arp = ByteUtils.slurpBytesFromHexFile(ETH2, CL);

        for (int i = 0; i < workerCount; i++)
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

        // Next open all connections; one-per-loop
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
        SocketAddress sa = new InetSocketAddress(ip.toInetAddress(), PORT);
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


    private static final int BATCH_SIZE = 1000;
    private static final int PERMITS = 3 * BATCH_SIZE;

    // Fake message
    private class FakeMessage extends AbstractMessage {

        private final byte data[];

        public FakeMessage() {
            data = new byte[1024];
        }

        public FakeMessage(OpenflowMessage om) throws Exception {
            data = MessageFactory.encodeMessage(om);
            length = data.length;
        }

        private void setLength(int length) {
            this.length = length;
        }

    }

    // Auxiliary message buffer to produce canned packet-ins and fake-consume packet outs
    private class FakeMessageBuffer extends MessageBuffer<FakeMessage> {

        private static final int LENGTH_MASK = 0xffff;
        private final FakeMessage fakeResponse = new FakeMessage();
        private final List<FakeMessage> fakeRequests = new ArrayList<FakeMessage>();

        // Generate a random packet in
        private FakeMessage randomRequest() throws Exception {
            OfmMutablePacketIn packetIn = (OfmMutablePacketIn)
                    MessageFactory.create(V_1_0, PACKET_IN);
            packetIn.bufferId(BufferId.valueOf(0))
                    .inPort(BigPortNumber.valueOf(1))
                    .data(arp).reason(PacketInReason.NO_MATCH);
            return new FakeMessage(packetIn.toImmutable());
        }

        private void sendFeatures() throws Exception {
            OfmMutableFeaturesReply reply = (OfmMutableFeaturesReply)
                    MessageFactory.create(V_1_0, FEATURES_REPLY);
            reply.dpid(DataPathId.valueOf(TestTools.random().nextLong()));
            reply.numBuffers(128);
            reply.numTables(0);

            Set<PortState> state = new HashSet<PortState>();
            state.add(PortState.STP_LISTEN);
            MutablePort p = PortFactory.createPort(V_1_0)
                    .portNumber(BigPortNumber.valueOf(1))
                    .hwAddress(MacAddress.valueOf("00:00:00:00:00:01"))
                    .state(state);
            reply.addPort((Port) p.toImmutable());
            queue(new FakeMessage(reply.toImmutable()));
        }

        private void sendHello() throws Exception {
            Set<ProtocolVersion> versions = new HashSet<ProtocolVersion>(Arrays.asList(V_1_0));
            HelloElement versionBitmap =
                    HelloElementFactory.createVersionBitmapElement(versions);
            OfmMutableHello mh = (OfmMutableHello)
                    create(versionBitmap.getVersion(), HELLO);
            mh.clearXid();
            mh.addElement(versionBitmap);
            queue(new FakeMessage(mh.toImmutable()));
        }

        public FakeMessageBuffer(ByteChannel ch, IOLoop<FakeMessage, ?> loop) {
            super(ch, loop);
            try {
                for (int i = 0; i < BATCH_SIZE; i++)
                    fakeRequests.add(randomRequest());
            } catch (Exception e) {
                log.error("Unable to prepare fake request", e);
            }
        }

        @Override
        protected FakeMessage get(ByteBuffer rb) {
            if (rb.remaining() < 4)
                return null;
            int length = rb.getInt(0) & LENGTH_MASK;
            if (rb.remaining() < length)
                return null;
            rb.get(fakeResponse.data, 0, length);
            fakeResponse.setLength(length);
            return fakeResponse;
        }

        @Override
        protected void put(FakeMessage msg, ByteBuffer wb) {
            if (msg.length() != msg.data.length)
                log.error("WTH?  {} != {}", msg.length(), msg.data.length);
            if (msg.data.length > wb.remaining())
                log.error("WTH?  not enough room? {} pos, {} lim", wb.position(), wb.limit());
            wb.put(msg.data);
        }

    }

    // Loop for transfer of fixed-length messages
    private class CustomIOLoop extends IOLoop<FakeMessage, FakeMessageBuffer> {

        final Worker worker = new Worker();

        public CustomIOLoop() throws IOException {
            super();
        }

        @Override
        protected FakeMessageBuffer createBuffer(ByteChannel ch,
                                                 SSLContext sslContext) {
            return new FakeMessageBuffer(ch, this);
        }

        @Override
        protected synchronized void discardBuffer(MessageBuffer<FakeMessage> b) {
            super.discardBuffer(b);

            messages.add(b.inMessages().total());
            bytes.add(b.inBytes().total());
            b.inMessages().reset();
            b.inBytes().reset();

            log.info("Disconnected client; inbound {} pps, {} Mbps; outbound {} pps, {} Mbps",
                     format.format(b.inMessages().throughput()),
                     format.format(b.inBytes().throughput() / (1024 * 128)),
                     format.format(b.outMessages().throughput()),
                     format.format(b.outBytes().throughput() / (1024 * 128)));
        }

        @Override
        protected void processMessages(MessageBuffer<FakeMessage> b,
                                       List<FakeMessage> messages) {
            try {
                worker.process(messages);
            } catch (Exception e) {
                log.warn("Unable to process message", e);
            }
        }

        @Override
        protected void connect(SelectionKey key) {
            super.connect(key);
            FakeMessageBuffer b = (FakeMessageBuffer) key.attachment();
            Worker w = ((CustomIOLoop) b.loop()).worker;
            w.pump(b);
        }

    }

    /**
     * Auxiliary worker to connect and pump batched messages using blocking I/O.
     */
    private class Worker implements Runnable {

        private FakeMessageBuffer b;
        private FutureTask<Worker> task;

        // Stuff to throttle pump
        private final Semaphore semaphore = new Semaphore(PERMITS);
        private int msgWritten;
        private boolean handshook;

        void pump(FakeMessageBuffer b) {
            if (this.b != null)
                log.error("WTH?  We already have a buffer");
            this.b = b;
            wpool.execute(task = new FutureTask<Worker>(this, this));
        }

        public void process(List<FakeMessage> messages) throws Exception {
            if (handshook)
                release(messages.size());
            else {
                for (FakeMessage m: messages) {
                    // Is this a feature request
                    if (m.data[1] == 5) {
                        b.sendFeatures();
                        synchronized (this) {
                            handshook = true;
                            notifyAll();
                        }
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                log.info("Worker started...");

                handshake();
                while (msgWritten < msgCount)
                    msgWritten += queueBatch(b);

                // Now try to get all the permits back before closing up.
                semaphore.tryAcquire(PERMITS, 5, TimeUnit.SECONDS);
                b.discard();

                log.info("Worker done...");

            } catch (Exception e) {
                log.error("Worker unable to perform I/O", e);
            }
        }

        // Queue a batch of packet-ins
        private int queueBatch(FakeMessageBuffer b) throws IOException {
            int count = Math.min(BATCH_SIZE, msgCount - msgWritten);
            acquire(count);
            if (count == BATCH_SIZE)
                b.queue(b.fakeRequests);
            else {
                for (int i = 0; i < count; i++)
                    b.queue(b.fakeRequests.get(i));
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

        public void handshake() throws Exception {
            Task.delay(10);
            b.sendHello();
            synchronized (this) {
                try {
                    wait(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
