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
 * Class is used only for testing purposes - passwords are hard-coded.
 *
 * @author michal.polkorab
 */
public class TlsConfigurationImpl implements TlsConfiguration {

    private final KeystoreType trustStoreType;
    private final String trustStore;
    private final KeystoreType keyStoreType;
    private final String keyStore;
    private final PathType keystorePathType;
    private final PathType truststorePathType;
    private final List<String> cipherSuites;
    private final Boolean verifyHostname;

    /**
     * Default constructor.
     *
     * @param trustStoreType JKS or PKCS12
     * @param trustStore path to trustStore file
     * @param trustStorePathType truststore path type (classpath or path)
     * @param keyStoreType JKS or PKCS12
     * @param keyStore path to keyStore file
     * @param keyStorePathType keystore path type (classpath or path)
     */
    public TlsConfigurationImpl(KeystoreType trustStoreType, String trustStore,
            PathType trustStorePathType, KeystoreType keyStoreType,
            String keyStore, PathType keyStorePathType,
            List<String> cipherSuites, Boolean verifyHostname) {
        this.trustStoreType = trustStoreType;
        this.trustStore = trustStore;
        this.truststorePathType = trustStorePathType;
        this.keyStoreType = keyStoreType;
        this.keyStore = keyStore;
        this.keystorePathType = keyStorePathType;
        this.cipherSuites = cipherSuites;
        this.verifyHostname = verifyHostname;
    }

    @Override
    public KeystoreType getTlsTruststoreType() {
        return trustStoreType;
    }

    @Override
    public String getTlsTruststore() {
        return trustStore;
    }

    @Override
    public KeystoreType getTlsKeystoreType() {
        return keyStoreType;
    }

    @Override
    public String getTlsKeystore() {
        return keyStore;
    }

    @Override
    public PathType getTlsKeystorePathType() {
        return keystorePathType;
    }

    @Override
    public PathType getTlsTruststorePathType() {
        return truststorePathType;
    }

    @Override
    public String getKeystorePassword() {
        return "opendaylight";
    }

    @Override
    public String getCertificatePassword() {
        return "opendaylight";
    }

    @Override
    public String getTruststorePassword() {
        return "opendaylight";
    }

    @Override
    public List<String> getCipherSuites() {
        return cipherSuites;
    }

    @Override
    public Boolean isVerifyHostname() {
        return verifyHostname;
    }
}
