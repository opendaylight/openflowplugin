/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import org.opendaylight.util.Log;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.api.security.SecurityContext;
import org.slf4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A factory for creating {@link SSLContext}s for TLS connections.
 * <p>
 * During construction, the factory attempts to create and initialize an
 * {@code SSLContext} for the given {@link SecurityContext}. Any exceptions
 * encountered along the way will be logged and captured. The state of the
 * factory can be examined with {@link #hasErrors()}, and the captured
 * exceptions examined with {@link #getErrors()}. Assuming all went well, the
 * {@code SSLContext} will be available via {@link #secureContext()}.
 *
 * @author Sudheer Duggisetty
 * @author Simon Hunt
 */
public class SecureContextFactory {

    static Logger log = Log.NBIO.getLogger();

    static void setLogger(Logger testLogger) {
        log = testLogger;
    }

    static void restoreLogger() {
        log = Log.NBIO.getLogger();
    }

    private static final String SECURITY_PROTOCOL = "TLS";
    private static final String KEY_STORE_TYPE = "JKS";
    // FIXME: do not hardcode ALGORITHM
    private static final String ALGORITHM = "SunX509";

    private static final String E_KEYSTORE_LOAD =
            "Failed to load keystore: {} {}";
    private static final String E_FIS_CLOSE =
            "Failed to close file input stream: {} {}";
    private static final String E_SSL_INIT =
            "Failed to initialize the SSL Context: {}";
    private static final String E_TLS_CONTEXT =
            "TLS context initialization failed: {}";

    private final SecurityContext ctx;
    private final List<Exception> errors = new ArrayList<Exception>();
    private SSLContext sslCtx;

    /**
     * Creates a secure context factory that generates an {@link SSLContext}
     * for the given crypt context.
     * <p>
     * Any exceptions encountered while attempting to create the
     * {@code SSLContext} will be logged and captured. A caller can discover
     * if there were any errors via {@link #hasErrors()}, and can examine the
     * logged exceptions via {@link #getErrors()}.
     *
     * @param context the crypt context
     */
    public SecureContextFactory(SecurityContext context) {
        this.ctx = context;
        if (context != null)
            init();
    }

    private void init() {
        KeyManagerFactory kmf = createKeyManagerFactory();
        TrustManagerFactory tmf = createTrustManagerFactory();
        boolean storesLoaded = (kmf != null && tmf != null);
        if (storesLoaded)
            createAndInitSslContext(kmf, tmf);
    }

    private KeyManagerFactory createKeyManagerFactory() {
        KeyManagerFactory kmf = null;
        KeyStore ks = getKeyStore(ctx.keyStoreName(), ctx.keyStorePass());
        if (ks != null) {
            try {
                char[] ksPassChars = ctx.keyStorePass().toCharArray();
                kmf = KeyManagerFactory.getInstance(ALGORITHM);
                kmf.init(ks, ksPassChars);
            } catch (Exception e) {
                recordError(e, E_TLS_CONTEXT);
            }
        }
        return kmf;
    }

    private TrustManagerFactory createTrustManagerFactory() {
        TrustManagerFactory tmf = null;
        KeyStore ts = getKeyStore(ctx.trustStoreName(), ctx.trustStorePass());
        if (ts != null) {
            try {
                tmf = TrustManagerFactory.getInstance(ALGORITHM);
                tmf.init(ts);
            } catch (Exception e) {
                recordError(e, E_TLS_CONTEXT);
            }
        }
        return tmf;
    }

    private void createAndInitSslContext(KeyManagerFactory kmf,
                                         TrustManagerFactory tmf) {
        try {
            sslCtx = SSLContext.getInstance(SECURITY_PROTOCOL);
            sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (Exception e) {
            recordError(e, E_SSL_INIT);
        }
    }

    private KeyStore getKeyStore(String fileName, String password) {
        if (StringUtils.isEmpty(fileName))
            return null;

        KeyStore ks = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
            ks = KeyStore.getInstance(KEY_STORE_TYPE);
            ks.load(fis, password.toCharArray());
        } catch (Exception e) {
            recordError(e, E_KEYSTORE_LOAD, fileName);
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                recordError(e, E_FIS_CLOSE, fis);
            }
        }
        return ks;
    }

    private void recordError(Exception e, String format, Object... optional) {
        int nopt = optional.length;
        Object[] params = Arrays.copyOf(optional, nopt + 1);
        params[nopt] = Log.stackTraceSnippet(e);
        log.error(format, params);
        errors.add(e);
    }

    /**
     * Returns {@code true} if any exceptions were caught and logged during
     * construction.
     *
     * @return true, if errors during construction
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns the exceptions that were caught and logged during construction.
     *
     * @return the logged exceptions
     */
    public List<Exception> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Returns the SSL context.
     *
     * @return the SSL context
     */
    public SSLContext secureContext() {
        return sslCtx;
    }

    @Override
    public String toString() {
        return "{SecureContextFactory: SSLCtx=" + sslCtx + "}";
    }
}
