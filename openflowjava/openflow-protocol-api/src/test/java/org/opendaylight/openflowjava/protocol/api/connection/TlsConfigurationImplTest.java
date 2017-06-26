/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.connection;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.KeystoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType;

import com.google.common.collect.Lists;

/**
 * @author michal.polkorab
 *
 */
public class TlsConfigurationImplTest {

    /**
     * Test correct TlsConfigurationImpl creation
     */
    @Test
    public void test() {
        List<String> cipherSuites = Lists.newArrayList("TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA256");
        TlsConfigurationImpl config = new TlsConfigurationImpl(KeystoreType.JKS,
                "user/dir", PathType.CLASSPATH, KeystoreType.PKCS12, "/var/lib", PathType.PATH, cipherSuites);
        assertEquals("Wrong keystore location", "/var/lib", config.getTlsKeystore());
        assertEquals("Wrong truststore location", "user/dir", config.getTlsTruststore());
        assertEquals("Wrong keystore type", KeystoreType.PKCS12, config.getTlsKeystoreType());
        assertEquals("Wrong truststore type", KeystoreType.JKS, config.getTlsTruststoreType());
        assertEquals("Wrong keystore path type", PathType.PATH, config.getTlsKeystorePathType());
        assertEquals("Wrong truststore path type", PathType.CLASSPATH, config.getTlsTruststorePathType());
        assertEquals("Wrong certificate password", "opendaylight", config.getCertificatePassword());
        assertEquals("Wrong keystore password", "opendaylight", config.getKeystorePassword());
        assertEquals("Wrong truststore password", "opendaylight", config.getTruststorePassword());
        assertEquals("Wrong cipher suites", cipherSuites, config.getCipherSuites());
    }
}