/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio.demo;

import org.opendaylight.util.nbio.AcceptLoop;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Loop for accepting client connections.
 *
 * @author Simon Hunt
 */
public class DemoAcceptLoop extends AcceptLoop {

    private final BufferModel model;
    private final int sockBufsize;

    public DemoAcceptLoop(BufferModel model, SocketAddress address, int bufSize)
            throws IOException {
        super(address);
        this.model = model;
        sockBufsize = bufSize;
    }

    @Override
    protected void accept(ServerSocketChannel ssc) throws IOException {
        SocketChannel sc = ssc.accept();
        sc.configureBlocking(false);

        Socket so = sc.socket();
        so.setTcpNoDelay(false);
        so.setReceiveBufferSize(sockBufsize);
        so.setSendBufferSize(sockBufsize);

        model.nextWorker().registerAccept(sc, null);
    }
}
