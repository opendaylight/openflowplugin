/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import static org.opendaylight.util.junit.TestTools.print;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.junit.Assert;

import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.api.security.SecurityContext;
import org.opendaylight.util.net.IpAddress;

/**
 * Auxiliary test fixture to illustrate secure connections (TLS).
 *
 * @author Simon Hunt
 */
public class StandaloneSecureClient {
    private static final String ROOT_DIR = "src/test/resources/org/opendaylight/util/nbio/";
    private static final String KEY_FILE = "server.jks";
    private static final String KEY_PATH = ROOT_DIR + KEY_FILE;
    private static final String KEY_PASS = "skyline";

    private static final int NUM_MESSAGES = 4;

    private static final byte FILLER = 0x7f;
    private static final byte[] PREFIX_ALPHA = { 0, 1, 2, 3, 4 };
    private static final byte[] PREFIX_BETA = { 1, 2, 3, 5, 7, 11, 13 };

    private static final byte[][] PREFIXES = {PREFIX_ALPHA, PREFIX_BETA};
    private static int prefixIndex = 0;

    private final IpAddress serverIp;
    private final int serverPort;
    private final boolean eager;

    private final SSLContext sslContext;

    /**
     * Constructs the secure client, which will connect to the given
     * address and port.
     *
     * @param serverIp the server address
     * @param serverPort the server port
     * @param eager flag indicating whether client should be eager or lazy
     * @throws IOException if issues arise
     */
    public StandaloneSecureClient(IpAddress serverIp, int serverPort,
                                  boolean eager) throws IOException {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.eager = eager;

        // Use our canned keystore/truststore...
        SecurityContext context =
                new SecurityContext(KEY_PATH, KEY_PASS, KEY_PATH, KEY_PASS);
        SecureContextFactory factory = new SecureContextFactory(context);
        sslContext = factory.secureContext();
    }

    public void startAndStop() throws IOException {
        // Open connection
        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(true); // we are the client
        Socket sock = sslContext.getSocketFactory()
                .createSocket(serverIp.toInetAddress(), serverPort);

        OutputStream out = sock.getOutputStream();
        InputStream in = sock.getInputStream();

        if (eager)
            eagerPump(in, out);
        else
            lazyPump(in, out);
        
        // Close connection
        sock.close();
    }

    private void eagerPump(InputStream in, OutputStream out) throws IOException {
        // Pump out a couple of messages
        for (int i=0; i<NUM_MESSAGES; i++) {
            byte[] msg = genMsg();
            print("[{}] OUT: {}", i, ByteUtils.toHexArrayString(msg));
            out.write(msg);
            byte[] copy = readMsg(in, msg.length);

            print("[{}] IN:  {}", i, ByteUtils.toHexArrayString(copy));
            Assert.assertArrayEquals("message not same", msg, copy);
            print("");
        }
    }
    
    private void lazyPump(InputStream in, OutputStream out) throws IOException {
        // Pump out a couple of messages
        for (int i=0; i<NUM_MESSAGES; i++) {
            byte[] msg = readMsg(in, StandaloneSecureServer.FIXED_MSG_LEN);
            print("[{}] IN:  {}", i, ByteUtils.toHexArrayString(msg));
            
            out.write(msg);
            print("[{}] OUT: {}", i, ByteUtils.toHexArrayString(msg));
            print("");
        }
    }
    
    private static byte[] readMsg(InputStream is, int length) throws IOException {
        byte[] copy = new byte[length];
        int offset = 0;
        while (offset < length)
            offset += is.read(copy, offset, length - offset);
        return copy;
    }

    static synchronized byte[] genMsg() {
        byte[] bytes = new byte[StandaloneSecureServer.FIXED_MSG_LEN];
        prefixIndex++;
        if (prefixIndex == PREFIXES.length)
            prefixIndex = 0;
        byte[] pfx = PREFIXES[prefixIndex];
        int index = 0;
        while (index < pfx.length) {
            bytes[index] = pfx[index];
            index++;
        }
        while (index < bytes.length)
            bytes[index++] = FILLER;
        return bytes;
    }
    
}
