/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import static java.util.Objects.requireNonNull;

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
import java.util.List;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for setting up TLS connection.
 *
 * @author michal.polkorab
 */
public class SslContextFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SslContextFactory.class);

    // "TLS" - supports some version of TLS
    // Use "TLSv1", "TLSv1.1", "TLSv1.2" for specific TLS version
    private static final String PROTOCOL = "TLS";

    private final TlsConfiguration tlsConfig;

    private volatile List<X509Certificate> switchCertificateChain;

    /**
     * Sets the TlsConfiguration.
     *
     * @param tlsConfig
     *            TLS configuration object, contains keystore locations +
     *            keystore types
     */
    public SslContextFactory(final TlsConfiguration tlsConfig) {
        this.tlsConfig = requireNonNull(tlsConfig);
    }

    @Nullable List<X509Certificate> getSwitchCertificateChain() {
        return switchCertificateChain;
    }

    void setSwitchCertificateChain(final X509Certificate[] chain) {
        switchCertificateChain = List.of(chain);
    }

    public SSLContext getServerContext() {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
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

            // A bit ugly: intercept trust checks to establish switch certificate
            final TrustManager[] delegates = tmf.getTrustManagers();
            final TrustManager[] proxies;
            if (delegates != null) {
                proxies = new TrustManager[delegates.length];
                for (int i = 0; i < delegates.length; i++) {
                    final TrustManager delegate = delegates[i];
                    if (delegate instanceof X509ExtendedTrustManager) {
                        proxies[i] = new ProxyExtendedTrustManager((X509ExtendedTrustManager) delegate);
                    } else if (delegate instanceof X509TrustManager) {
                        proxies[i] = new ProxyTrustManager((X509TrustManager) delegate);
                    } else {
                        LOG.debug("Cannot handle trust manager {}, passing through", delegate);
                        proxies[i] = delegate;
                    }
                }
            } else {
                proxies = null;
            }
            serverContext.init(kmf.getKeyManagers(), proxies, null);
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

    private final class ProxyTrustManager implements X509TrustManager {
        private final X509TrustManager delegate;

        ProxyTrustManager(final X509TrustManager delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType)
                throws CertificateException {
            setSwitchCertificateChain(chain);
            delegate.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType)
                throws CertificateException {
            delegate.checkServerTrusted(chain, authType);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return delegate.getAcceptedIssuers();
        }
    }

    private final class ProxyExtendedTrustManager extends X509ExtendedTrustManager {
        private final X509ExtendedTrustManager delegate;

        ProxyExtendedTrustManager(final X509ExtendedTrustManager trustManager) {
            delegate = requireNonNull(trustManager);
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
                throws CertificateException {
            setSwitchCertificateChain(chain);
            delegate.checkClientTrusted(chain, authType, socket);
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType)
                throws CertificateException {
            setSwitchCertificateChain(chain);
            delegate.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType, final SSLEngine sslEngine)
                throws CertificateException {
            setSwitchCertificateChain(chain);
            delegate.checkClientTrusted(chain, authType, sslEngine);
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType, final SSLEngine sslEngine)
                throws CertificateException {
            delegate.checkServerTrusted(chain, authType, sslEngine);
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType)
                throws CertificateException {
            delegate.checkServerTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
                throws CertificateException {
            delegate.checkServerTrusted(chain, authType, socket);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return delegate.getAcceptedIssuers();
        }
    }
}
