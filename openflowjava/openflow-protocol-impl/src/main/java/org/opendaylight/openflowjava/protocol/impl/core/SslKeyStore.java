/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for storing keys
 *
 * @author michal.polkorab
 */
public final class SslKeyStore {

    private static final Logger LOG = LoggerFactory.getLogger(SslKeyStore.class);

    private SslKeyStore() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * InputStream instance of key - key location is on classpath
     * @param filename keystore location
     * @param pathType keystore location type - "classpath" or "path"
     *
     * @return key as InputStream
     */
    public static InputStream asInputStream(String filename, PathType pathType) {
        InputStream in;
        switch (pathType) {
        case CLASSPATH:
            in = SslKeyStore.class.getResourceAsStream(filename);
            if (in == null) {
                throw new IllegalStateException("KeyStore file not found: "
                        + filename);
            }
            break;
        case PATH:
            LOG.debug("Current dir using System:"
                    + System.getProperty("user.dir"));
            File keystorefile = new File(filename);
            try {
                in = new FileInputStream(keystorefile);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("KeyStore file not found: "
                        + filename,e);
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown path type: " + pathType);
        }
        return in;
    }
}
