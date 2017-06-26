/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.clients;

import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Class for setting up TLS connection.
 *
 * @author michal.polkorab
 */
public final class ClientSslContextFactory {

    private ClientSslContextFactory() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    // "TLS" - supports some version of TLS
    // Use "TLSv1", "TLSv1.1", "TLSv1.2" for specific TLS version
    private static final String PROTOCOL = "TLS";
    private static final SSLContext CLIENT_CONTEXT;

    static {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }

        SSLContext clientContext;
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(ClientSslKeyStore.asInputStream(),
                    ClientSslKeyStore.getKeyStorePassword());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, ClientSslKeyStore.getCertificatePassword());

            KeyStore ts = KeyStore.getInstance("JKS");
            ts.load(ClientSslTrustStore.asInputStream(),
                    ClientSslTrustStore.getKeyStorePassword());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
            tmf.init(ts);

            clientContext = SSLContext.getInstance(PROTOCOL);
            clientContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (Exception e) {
            throw new Error(
                    "Failed to initialize the client-side SSLContext", e);
        }

        CLIENT_CONTEXT = clientContext;
    }

    /**
     * @return client context
     */
    public static SSLContext getClientContext() {
        return CLIENT_CONTEXT;
    }
}