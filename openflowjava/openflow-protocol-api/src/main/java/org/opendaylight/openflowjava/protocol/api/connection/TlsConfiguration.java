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
 * Tls configuration.
 *
 * @author michal.polkorab
 */
public interface TlsConfiguration {

    /**
     * Returns the key store location.
     *
     * @return key store location
     */
    String getTlsKeystore();

    /**
     * Returns the key store type.
     *
     * @return key store type
     */
    KeystoreType getTlsKeystoreType();

    /**
     * Returns the trust store location.
     *
     * @return trust store location
     */
    String getTlsTruststore();

    /**
     * Returns the trust store type.
     *
     * @return trust store type
     */
    KeystoreType getTlsTruststoreType();

    /**
     * Returns the key store path type.
     *
     * @return key store path type (CLASSPATH or PATH)
     */
    PathType getTlsKeystorePathType();

    /**
     * Returns the trust store path type.
     *
     * @return trust store path type (CLASSPATH or PATH)
     */
    PathType getTlsTruststorePathType();

    /**
     * Returns the password protecting the key store.
     *
     * @return password protecting the specified key store
     */
    String getKeystorePassword();

    /**
     * Returns the password protecting the certificate.
     *
     * @return password protecting certificate
     */
    String getCertificatePassword();

    /**
     * Returns the password protecting the trust store.
     *
     * @return password protecting specified trust store
     */
    String getTruststorePassword();

    /**
     * Returns the list of cipher suites for TLS connection.
     *
     * @return list of cipher suites for TLS connection
     */
    List<String> getCipherSuites();
}
