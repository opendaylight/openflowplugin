/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.connection;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.KeystoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType;


/**
 * @author michal.polkorab
 *
 */
public interface TlsConfiguration {

    /**
     * @return keystore location
     */
    String getTlsKeystore();

    /**
     * @return keystore type
     */
    KeystoreType getTlsKeystoreType();

    /**
     * @return truststore location
     */
    String getTlsTruststore();

    /**
     * @return truststore type
     */
    KeystoreType getTlsTruststoreType();

    /**
     * @return keystore path type (CLASSPATH or PATH)
     */
    PathType getTlsKeystorePathType();

    /**
     * @return truststore path type (CLASSPATH or PATH)
     */
    PathType getTlsTruststorePathType();

    /**
     * @return password protecting specified keystore
     */
    String getKeystorePassword();

    /**
     * @return password protecting certificate
     */
    String getCertificatePassword();

    /**
     * @return password protecting specified truststore
     */
    String getTruststorePassword();

    /**
     * @return list of cipher suites for TLS connection
     */
    List<String> getCipherSuites();
}
