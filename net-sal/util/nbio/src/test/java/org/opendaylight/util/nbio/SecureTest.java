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

import org.junit.Test;
import org.opendaylight.util.net.IpAddress;


/**
 * High-level unit test of Secure connection facilities.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public class SecureTest {

    private static final IpAddress IP = IpAddress.LOOPBACK_IPv4;
    private static final int PORT = 6789;

    private StandaloneSecureServer server;
    private StandaloneSecureClient client;

    @Test
    public void eagerClient() throws IOException {
        run(true);
    }

    @Test
    public void lazyClient() throws IOException {
        run(false);
    }

    private void run(boolean eager) throws IOException {
        print("Starting run ({}:{})", IP, PORT);
        server = new StandaloneSecureServer(IP, PORT, !eager);
        client = new StandaloneSecureClient(IP, PORT, eager);

        server.start();
        client.startAndStop();
        server.stop();
        print("Ending run");
    }

}
