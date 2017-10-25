/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType;

/**
 *
 * @author jameshall
 */
public class SslKeyStoreTest {

    /**
     * Sets up test environment
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test keystore file access - via classpath
     * @throws Exception
     */
    @Test
    public void testAsInputStream() throws Exception {
        InputStream inputStream = SslKeyStore.asInputStream("/key.bin", PathType.CLASSPATH);
        assertNotNull( inputStream );
        inputStream.close();
    }

    /**
     * Test keystore file access - via relative path
     * @throws Exception
     */
    @Test
    public void testAsInputStream2() throws Exception {
        InputStream inputStream = SslKeyStore.asInputStream("src/test/resources/key.bin", PathType.PATH);
        assertNotNull( inputStream );
        inputStream.close();
    }
}
