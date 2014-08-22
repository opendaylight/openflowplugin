/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import org.opendaylight.util.api.security.SecurityContext;
import org.opendaylight.util.net.IpAddress;
import org.slf4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.opendaylight.util.nbio.StandaloneSecureClient.genMsg;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.opendaylight.util.NamedThreadFactory.namedThreads;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Auxiliary test fixture to illustrate secure connections (TLS).
 *
 * @author Simon Hunt
 */
public class StandaloneSecureServer {
    private final Logger log = getLogger(StandaloneSecureServer.class);

    // Note, key store and trust store are the same file
    private static final String ROOT_DIR = "src/test/resources/org/opendaylight/util/nbio/";
    private static final String KEY_FILE = "server.jks";
    private static final String KEY_PATH = ROOT_DIR + KEY_FILE;
    private static final String KEY_PASS = "skyline";

    static final int FIXED_MSG_LEN = 128;
    private static final int WORKER_COUNT = 2;

    static final long TIMEOUT_MS = 1000;
    static final boolean SO_NO_DELAY = false;
    static final int SO_SEND_BUFFER_SIZE = 1024 * 1024;
    static final int SO_RCV_BUFFER_SIZE = 1024 * 1024;

    private final boolean eager;
    
    private final AcceptLoop aloop;
    private final ExecutorService apool =
            newSingleThreadExecutor(namedThreads("sec-accept"));

    private final List<CustomIOLoop> iloops = new ArrayList<CustomIOLoop>();
    private final ExecutorService ipool;

    private int lastWorker = -1;
    private boolean queueSingly = false;

    /**
     * Constructs the secure server, binding on the given adapter and port.
     *
     * @param ip the adapter to bind to
     * @param port the port to bind to
     * @param eager flag indicating whether client should be eager or lazy
     * @throws IOException blah
     */
    public StandaloneSecureServer(IpAddress ip, int port, 
                                  boolean eager) throws IOException {
        this.eager = eager;
        
        // create an SSLContext
        SecurityContext sc =
                new SecurityContext(KEY_PATH, KEY_PASS, KEY_PATH, KEY_PASS);
        SecureContextFactory factory = new SecureContextFactory(sc);

        aloop = new CustomAcceptLoop(ip, port);
        aloop.setSslContext(factory.secureContext());
        
        ipool = newFixedThreadPool(WORKER_COUNT, namedThreads("sec-io-loop"));
        for (int i=0; i<WORKER_COUNT; i++)
            iloops.add(new CustomIOLoop());
    }

    public void start() {
        log.info("Starting");
        // Optional throughput trackers go here

        for (CustomIOLoop loop : iloops)
            ipool.execute(loop);
        apool.execute(aloop);

        for (CustomIOLoop loop : iloops)
            loop.waitForStart(TIMEOUT_MS);
        aloop.waitForStart(TIMEOUT_MS);
    }



    public void stop() {
        log.info("Stopping");
        aloop.cease();
        for (CustomIOLoop loop : iloops)
            loop.cease();

        for (CustomIOLoop loop : iloops)
            loop.waitForFinish(TIMEOUT_MS);
        aloop.waitForFinish(TIMEOUT_MS);

        // Optional freeze of trackers
    }


    //========================================================================
    // Get the next worker to which a client should be assigned
    private synchronized CustomIOLoop nextWorker() {
        lastWorker = (lastWorker + 1) % WORKER_COUNT;
        return iloops.get(lastWorker);
    }
    
    private class EagerOrLazyMessageBuffer extends FixedLengthMessageBuffer {

        public EagerOrLazyMessageBuffer(ByteChannel ch,
                                        IOLoop<FixedLengthMessage, ?> loop,
                                        SSLContext sslContext) {
            super(FIXED_MSG_LEN, ch, loop, sslContext);
        }

        @Override
        public void setKey(SelectionKey key) {
            super.setKey(key);
            if (eager) {
                try {
                    queue(new FixedLengthMessage(genMsg()));
                } catch (IOException e) {
                    e.printStackTrace();
                }            
            }
        }
        
    }


    // Loop for transfer of fixed-length messages
    private class CustomIOLoop
            extends IOLoop<FixedLengthMessage, EagerOrLazyMessageBuffer> {

        public CustomIOLoop() throws IOException {
            super();
        }

        @Override
        protected EagerOrLazyMessageBuffer createBuffer(ByteChannel ch,
                                                        SSLContext context) {
            // create concrete buffer, stamped with SSLContext
            return new EagerOrLazyMessageBuffer(ch, this, context);
        }

        @Override
        protected void processMessages(MessageBuffer<FixedLengthMessage> b,
                                       List<FixedLengthMessage> messages) {
            try {
                if (eager) {
                    // When eager and we received response, send a new message
                    // for every response we got.
                    for (FixedLengthMessage flm: messages)
                        b.queue(new FixedLengthMessage(genMsg()));
                } else {
                    if (queueSingly) {
                        for (FixedLengthMessage flm: messages)
                            b.queue(flm);
                    } else {
                        b.queue(messages);
                    }
                    queueSingly = !queueSingly; // alternate the method
                }

            } catch (IOException e) {
                log.error("unable to echo messages", e);
            }

        }
    }

    // Loop for accepting client connections
    // TODO: confirm this is just a "regular" accept loop
    private class CustomAcceptLoop extends AcceptLoop {

        public CustomAcceptLoop(IpAddress ip, int port) throws IOException {
            super(new InetSocketAddress(ip.toInetAddress(), port));
        }

        @Override
        protected void accept(ServerSocketChannel ssc) throws IOException {
            SocketChannel sc = ssc.accept();
            sc.configureBlocking(false);

            Socket so = sc.socket();
            so.setTcpNoDelay(SO_NO_DELAY);
            so.setReceiveBufferSize(SO_RCV_BUFFER_SIZE);
            so.setSendBufferSize(SO_SEND_BUFFER_SIZE);

            nextWorker().registerAccept(sc, sslContext);
            log.info("Connected client");
        }
    }

}
