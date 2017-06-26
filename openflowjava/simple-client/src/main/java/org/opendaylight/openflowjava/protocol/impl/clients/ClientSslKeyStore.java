/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.clients;

import java.io.InputStream;

/**
 * Class for storing keys
 *
 * @author michal.polkorab
 */
public final class ClientSslKeyStore {

    private static final String KEY_STORE_FILENAME = "/selfSignedSwitch";

    private ClientSslKeyStore() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * InputStream instance of key
     *
     * @return key as InputStream
     */
    public static InputStream asInputStream() {
        InputStream in = ClientSslKeyStore.class.getResourceAsStream(KEY_STORE_FILENAME);
        if (in == null) {
            throw new IllegalStateException("KeyStore file not found: " + KEY_STORE_FILENAME);
        }
        return in;
    }

    /**
     * @return certificate password as char[]
     */
    public static char[] getCertificatePassword() {
        return "opendaylight".toCharArray();
    }

    /**
     * @return KeyStore password as char[]
     */
    public static char[] getKeyStorePassword() {
        return "opendaylight".toCharArray();
    }
}