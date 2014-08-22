/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import org.opendaylight.util.api.security.SecurityContext;
import org.opendaylight.util.junit.TestLogger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * Unit tests for SecureContextFactory.
 *
 * @author Simon Hunt
 */
public class SecureContextFactoryTest {
    private static final String FMT_EX = "EX> {}";
    private static final String ROOT_DIR = "src/test/resources/org/opendaylight/util/nbio/";

    private static final String KEY_STORE = "simon.jks";
    private static final String KEY_PASS = "skyline";
    // NOTE: truststore is same as keystore for these unit tests

    private static final String NO_SUCH_KEY_STORE = "noSuchKeyStore.jks";
    private static final String NO_SUCH_TRUST_STORE = "noSuchTrustStore.jks";

    private static final TestLogger tlog = new TestLogger();

    private SecurityContext context;
    private SecureContextFactory factory;

    @BeforeClass
    public static void classSetUp() {
        SecureContextFactory.setLogger(tlog);
    }

    @AfterClass
    public static void classTearDown() {
        SecureContextFactory.restoreLogger();
    }

    private String path(String filename) {
        return ROOT_DIR + filename;
    }

    @Test
    public void nullContext() {
        print(EOL + "nullContext()");
        // Implementation note: null needs to be allowed - we have so many
        //  unit tests that provide no context.
        factory = new SecureContextFactory(null);
        print(factory);
        assertNull(AM_HUH, factory.secureContext());
        assertFalse(AM_HUH, factory.hasErrors());
        assertEquals(AM_UXS, 0, factory.getErrors().size());
    }

    @Test
    public void defaultFactory() {
        print(EOL + "defaultFactory()");
        factory = new SecureContextFactory(new SecurityContext());
        print(factory);
        assertNull(AM_HUH, factory.secureContext());
        assertFalse(AM_HUH, factory.hasErrors());
        assertEquals(AM_UXS, 0, factory.getErrors().size());
    }

    @Test
    public void missingKeyStores() {
        print(EOL + "missingKeyStores()");
        context = new SecurityContext(path(NO_SUCH_KEY_STORE), KEY_PASS,
                path(NO_SUCH_TRUST_STORE), KEY_PASS);
        factory = new SecureContextFactory(context);
        assertNull(AM_HUH, factory.secureContext());
        assertTrue(AM_HUH, factory.hasErrors());
        assertEquals(AM_UXS, 2, factory.getErrors().size());
        tlog.assertErrorContains("Failed to load keystore:");
        for (Exception e: factory.getErrors())
            print(FMT_EX, e);
    }

    @Test
    public void simonStore() {
        print(EOL + "simonStore()");
        context = new SecurityContext(path(KEY_STORE), KEY_PASS,
                path(KEY_STORE), KEY_PASS);
        factory = new SecureContextFactory(context);
        print(factory);
        assertNotNull(AM_HUH, factory.secureContext());
        assertFalse(AM_HUH, factory.hasErrors());
        assertEquals(AM_UXS, 0, factory.getErrors().size());
    }
}
