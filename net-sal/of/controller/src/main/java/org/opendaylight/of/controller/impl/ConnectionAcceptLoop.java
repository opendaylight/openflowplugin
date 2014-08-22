/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.util.nbio.AcceptLoop;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ResourceBundle;

import static org.opendaylight.util.Log.stackTraceSnippet;
import static org.opendaylight.util.ResourceUtils.getBundledResource;

/**
 * I/O loop for accepting TCP, TLS and UDP connections from OpenFlow switches.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class ConnectionAcceptLoop extends AcceptLoop {

    private static final ResourceBundle RES = 
            getBundledResource(ConnectionAcceptLoop.class, "connectionAcceptLoop");

    private static final String E_ACCEPT = RES.getString("e_accept");
    
    static final boolean SO_NO_DELAY = false;
    static final int SO_SEND_BUFFER_SIZE = 1024 * 1024;
    static final int SO_RCV_BUFFER_SIZE = 1024 * 1024;

    private static final long SELECT_TIMEOUT_MS = 50;
    

    // Parent controller
    private final OpenflowController controller;
    // the address on which we are accepting secure connections
    private int securePort = 0;

    int sendBufferSize = SO_SEND_BUFFER_SIZE;
    int receiveBufferSize = SO_RCV_BUFFER_SIZE;
//    int udpRcvBufSize = SO_RCV_BUFFER_SIZE;

    /**
     * Creates a new connection accept loop.
     *
     * @param controller parent controller
     * @param addresses listen address(es)
     * @throws IOException if unable to open IO selector
     */
    public ConnectionAcceptLoop(OpenflowController controller,
                                SocketAddress... addresses)
            throws IOException {
        super(SELECT_TIMEOUT_MS, addresses);
        this.controller = controller;
        this.log = LoggerFactory.getLogger(OpenflowController.class);
    }

    /**
     * Sets the port number to be used for secure connections, so we can
     * identify channels originating from the secure port. This may be 0 (zero)
     * if the user has disabled the secure port.
     *
     * @param securePortNumber the secure port number
     */
    void setSecureAddress(int securePortNumber) {
        securePort = securePortNumber;
    }

    @Override
    protected void accept(ServerSocketChannel ssc) throws IOException {
        try {
            SocketChannel sc = ssc.accept();
            configureChannel(sc);
            SSLContext ctx = isSecureChannel(ssc) ? sslContext : null;
            controller.nextWorker().registerAccept(sc, ctx);
            
        } catch (Exception e) {
            log.warn(E_ACCEPT, stackTraceSnippet(e));
        } catch (Error e) {
            log.warn(E_ACCEPT, stackTraceSnippet(e));
        }
    }

    private void configureChannel(SocketChannel sc) throws IOException {
        sc.configureBlocking(false);
        Socket so = sc.socket();
        so.setTcpNoDelay(SO_NO_DELAY);
        so.setReceiveBufferSize(receiveBufferSize);
        so.setSendBufferSize(sendBufferSize);
    }

    private boolean isSecureChannel(ServerSocketChannel ssc) {
        SocketAddress incoming = ssc.socket().getLocalSocketAddress();
        if (securePort <= 0 || !InetSocketAddress.class.isInstance(incoming))
            return false;

        return securePort == ((InetSocketAddress)incoming).getPort();
    }
}
