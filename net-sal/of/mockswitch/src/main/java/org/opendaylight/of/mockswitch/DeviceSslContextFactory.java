/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.ResourceBundle;

/**
 * Switch(Client) side TLS context factory.
 *
 * @author Sudheer Duggisetty
 */
class DeviceSslContextFactory {
    
    private final Logger log = LoggerFactory.getLogger(DeviceSslContextFactory.class);

    static final String ALGORITHM = "SunX509";
    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            DeviceSslContextFactory.class, "deviceSslContextFactory");

    static final String E_TLS_CONTEXT = RES.getString("e_tls_context");

    private SSLContext sslContext;

    DeviceSslContextFactory(MockOpenflowSwitch sw) {
        try {
            // Load and create key store
            KeyStore ks = getKeyStore(sw.getKeystoreName(), sw.getKeystorePass());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(ALGORITHM);
            kmf.init(ks, sw.getKeystorePass().toCharArray());
            // Load and create trust store
            KeyStore ts = getKeyStore(sw.getKeystoreName(), sw.getKeystorePass());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(ALGORITHM);
            tmf.init(ts);

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            sslContext = ctx;

        } catch (Exception e) {
            log.error(E_TLS_CONTEXT, e);
        }
    }


    private KeyStore getKeyStore(String fileName, String password) {
        KeyStore ks = null;
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(fileName);
            ks = KeyStore.getInstance("JKS");
            ks.load(fs, password.toCharArray());
        } catch (Exception e) {
            log.info(E_TLS_CONTEXT, e);
        } finally {
            try {
                if (fs != null)
                    fs.close();
            } catch (IOException e) {
                log.info(E_TLS_CONTEXT, e);
            }
        }
        return ks;
    }

    public SSLContext getClientContext() {
        return sslContext;
    }
}