/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import static org.opendaylight.util.Log.stackTraceSnippet;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import javax.net.ssl.SSLContext;

/**
 * Abstraction of an I/O loop for accepting in-bound socket connections.
  *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public abstract class AcceptLoop extends SelectLoop {

    private static final String E_NO_ADDRESSES = "No socket addresses defined";
    private static final String E_CLOSE = "Unable to close server socket channel {}";

    /** SSL context for secure connections. */
    protected SSLContext sslContext;

    private SocketAddress[] addresses;
    private ServerSocketChannel[] channels;

    /**
     * Creates an accept loop bound to the specified socket address(es) with
     * default select timeout.
     *
     * @param addresses listen socket address(es)
     * @throws IllegalArgumentException if no addresses are specified
     * @throws IOException if unable to open selector
     */
    public AcceptLoop(SocketAddress... addresses) throws IOException {
        storeAddresses(addresses);
    }

    /**
     * Creates an accept loop bound to the specified socket address(es) with
     * the given select timeout.
     *
     * @param timeout select timeout in milliseconds
     * @param addresses listen socket address(es)
     * @throws IllegalArgumentException if no addresses are specified
     * @throws IOException if unable to open selector
     */
    public AcceptLoop(long timeout, SocketAddress... addresses)
            throws IOException {
        super(timeout);
        storeAddresses(addresses);
    }

    private void storeAddresses(SocketAddress... addresses) {
        if (addresses.length < 1)
            throw new IllegalArgumentException(E_NO_ADDRESSES);
        this.addresses = addresses.clone();
    }

    /**
     * Sets the SSL context for secure connections.
     *
     * @param context the SSL context
     */
    public void setSslContext(SSLContext context) {
        sslContext = context;
    }

    /**
     * Returns the SSL context used for secure connections.
     *
     * @return the SSL context
     */
    public SSLContext sslContext() {
        return sslContext;
    }

    /**
     * Processes the accept event pending on the specified socket channel.
     *
     * @param ssc server socket channel with pending accept operation
     * @throws IOException if unable to accept the incoming socket connection
     */
    protected abstract void accept(ServerSocketChannel ssc) throws IOException;

    /**
     * Allocates the server socket channel(s) in non-blocking mode, registers
     * it (them) with the selector and then binds to the configured listen
     * address(es).
     *
     * @throws IOException if channel could not be created or bound to address
     */
    protected synchronized void createChannels() throws IOException {
        channels = new ServerSocketChannel[addresses.length];
        int i = 0;
        for (SocketAddress addr: addresses) {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            channels[i++] = configureChannel(ssc, addr);
        }
    }

    /**
     * Configures the given server socket channel to be in an unblocking mode,
     * registers it and binds it to the specified socket address.
     *
     * @param ssc the server socket channel to configure
     * @param address the address to bind to
     * @return the channel, for chaining
     * @throws IOException if the channel could not be configured
     */
    protected ServerSocketChannel configureChannel(ServerSocketChannel ssc,
                                                   SocketAddress address)
            throws IOException {
        ssc.configureBlocking(false);
        ssc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        ssc.bind(address);
        return ssc;
    }

    /**
     * Closes the server socket channel(s).
     *
     * @throws IOException if any channel could not be closed
     */
    protected synchronized void closeChannels() throws IOException {
        if (channels == null)
            return;

        for (int i=0; i<channels.length; i++) {
            if (channels[i] != null) {
                channels[i].close();
                channels[i] = null;
            }
        }
        channels = null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation first closes the channels, then terminates the loop.
     */
    @Override
    public void cease() {
        try {
            closeChannels();
        } catch (IOException e) {
            log.warn(E_CLOSE, stackTraceSnippet(e));
        }
        super.cease();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation first allocates server socket channels via
     * {@link #createChannels()} before starting the loop, which simply accepts
     * any in-bound connections via {@link #accept} method. After breaking
     * out of the loop, the channels are closed.
     */
    @Override
    protected void loop() throws IOException {
        createChannels();
        signalStart();

        // Outer event loop
        int count;
        while (!stopped) {
           count = selector.select(timeout);
           if (count == 0 || stopped)
               continue;

           Iterator<SelectionKey> it = selector.selectedKeys().iterator();
           while (it.hasNext()) {
               SelectionKey key = it.next();
               it.remove();
               if (key.isAcceptable())
                   accept((ServerSocketChannel) key.channel());
           }
        }
    }
}
