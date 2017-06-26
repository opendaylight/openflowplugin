/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for setting up TLS connection.
 *
 * @author michal.polkorab
 */
public class SslContextFactory {

    // "TLS" - supports some version of TLS
    // Use "TLSv1", "TLSv1.1", "TLSv1.2" for specific TLS version
    private static final String PROTOCOL = "TLS";
    private TlsConfiguration tlsConfig;

    private static final Logger LOG = LoggerFactory
            .getLogger(SslContextFactory.class);

    /**
     * @param tlsConfig
     *            TLS configuration object, contains keystore locations +
     *            keystore types
     */
    public SslContextFactory(TlsConfiguration tlsConfig) {
        this.tlsConfig = tlsConfig;
    }

    /**
     * @return servercontext
     */
    public SSLContext getServerContext() {
        String algorithm = Security
                .getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        SSLContext serverContext = null;
        try {
            KeyStore ks = KeyStore.getInstance(tlsConfig.getTlsKeystoreType().name());
            ks.load(SslKeyStore.asInputStream(tlsConfig.getTlsKeystore(), tlsConfig.getTlsKeystorePathType()),
                    tlsConfig.getKeystorePassword().toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, tlsConfig.getCertificatePassword().toCharArray());

            KeyStore ts = KeyStore.getInstance(tlsConfig.getTlsTruststoreType().name());
            ts.load(SslKeyStore.asInputStream(tlsConfig.getTlsTruststore(), tlsConfig.getTlsTruststorePathType()),
                    tlsConfig.getTruststorePassword().toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
            tmf.init(ts);

            serverContext = SSLContext.getInstance(PROTOCOL);
            serverContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (IOException e) {
            LOG.warn("IOException - Failed to load keystore / truststore."
                    + " Failed to initialize the server-side SSLContext", e);
        } catch (NoSuchAlgorithmException e) {
            LOG.warn("NoSuchAlgorithmException - Unsupported algorithm."
                    + " Failed to initialize the server-side SSLContext", e);
        } catch (CertificateException e) {
            LOG.warn("CertificateException - Unable to access certificate (check password)."
                    + " Failed to initialize the server-side SSLContext", e);
        } catch (Exception e) {
            LOG.warn("Exception - Failed to initialize the server-side SSLContext", e);
        }
        return serverContext;
    }
}
