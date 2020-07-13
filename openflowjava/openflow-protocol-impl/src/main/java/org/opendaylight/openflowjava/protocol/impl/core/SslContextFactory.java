/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
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
    private final TlsConfiguration tlsConfig;
    private static X509Certificate switchCertificate = null;
    private static boolean isCustomTrustManagerEnabled;

    private static final Logger LOG = LoggerFactory
            .getLogger(SslContextFactory.class);

    /**
     * Sets the TlsConfiguration.
     *
     * @param tlsConfig
     *            TLS configuration object, contains keystore locations +
     *            keystore types
     */
    public SslContextFactory(TlsConfiguration tlsConfig) {
        this.tlsConfig = tlsConfig;
    }

    public X509Certificate getSwitchCertificate() {
        return switchCertificate;
    }

    public boolean isCustomTrustManagerEnabled() {
        return isCustomTrustManagerEnabled;
    }

    public static void setSwitchCertificate(X509Certificate certificate) {
        switchCertificate = certificate;
    }

    public static void setIsCustomTrustManagerEnabled(boolean customTrustManagerEnabled) {
        isCustomTrustManagerEnabled = customTrustManagerEnabled;
    }

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
            if (isCustomTrustManagerEnabled) {
                CustomTrustManager[] customTrustManager = new CustomTrustManager[tmf.getTrustManagers().length];
                for (int i = 0; i < tmf.getTrustManagers().length; i++) {
                    customTrustManager[i] = new CustomTrustManager((X509ExtendedTrustManager)
                            tmf.getTrustManagers()[i]);
                }
                serverContext.init(kmf.getKeyManagers(), customTrustManager, null);
            } else {
                serverContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            }
        } catch (IOException e) {
            LOG.warn("IOException - Failed to load keystore / truststore."
                    + " Failed to initialize the server-side SSLContext", e);
        } catch (NoSuchAlgorithmException e) {
            LOG.warn("NoSuchAlgorithmException - Unsupported algorithm."
                    + " Failed to initialize the server-side SSLContext", e);
        } catch (CertificateException e) {
            LOG.warn("CertificateException - Unable to access certificate (check password)."
                    + " Failed to initialize the server-side SSLContext", e);
        } catch (KeyManagementException | KeyStoreException | UnrecoverableKeyException e) {
            LOG.warn("Exception - Failed to initialize the server-side SSLContext", e);
        }
        return serverContext;
    }


    private static class CustomTrustManager extends X509ExtendedTrustManager {
        private final X509ExtendedTrustManager trustManager;

        CustomTrustManager(final X509ExtendedTrustManager trustManager) {
            this.trustManager = trustManager;
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] x509Certificates, final String authType,
                final Socket socket) throws CertificateException {
            SslContextFactory.setSwitchCertificate(x509Certificates[0]);
            trustManager.checkClientTrusted(x509Certificates, authType, socket);
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] x509Certificates, final String authType)
                throws CertificateException {
            SslContextFactory.setSwitchCertificate(x509Certificates[0]);
            trustManager.checkClientTrusted(x509Certificates, authType);
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] x509Certificates, final String authType,
                final SSLEngine sslEngine) throws CertificateException {
            SslContextFactory.setSwitchCertificate(x509Certificates[0]);
            trustManager.checkClientTrusted(x509Certificates, authType, sslEngine);
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] x509Certificates, final String authType,
                final SSLEngine sslEngine) throws CertificateException {
            trustManager.checkServerTrusted(x509Certificates, authType, sslEngine);
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] x509Certificates, final String authType)
                throws CertificateException {
            trustManager.checkServerTrusted(x509Certificates, authType);
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] x509Certificates, final String authType,
                final Socket socket) throws CertificateException {
            trustManager.checkServerTrusted(x509Certificates, authType, socket);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return trustManager.getAcceptedIssuers();
        }

    }

}
