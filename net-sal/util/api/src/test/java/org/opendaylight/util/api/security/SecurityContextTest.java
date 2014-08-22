/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api.security;

import org.junit.Test;
import org.opendaylight.util.junit.TestTools;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link SecurityContext}.
 *
 * @author Sudheer Duggisetty
 * @author Simon Hunt
 */
public class SecurityContextTest {

    private static final String EMPTY = "";
    private static final String DEFAULT_KS_NAME = "controllerKs.jks";
    private static final String DEFAULT_KS_PASS = "skyline";
    private static final String DEFAULT_TS_NAME = "controllerTs.jks";
    private static final String DEFAULT_TS_PASS = "skyline";
    private static final String OTHER_KS_NAME = "flareKs.jks";
    private static final String OTHER_KS_PASS = "flare123";
    private static final String OTHER_TS_NAME = "flareTs.jks";
    private static final String OTHER_TS_PASS = "flare123";

    private SecurityContext scA;
    private SecurityContext scB;

    @Test
    public void basic() {
        print(TestTools.EOL + "basic()");
        scA = new SecurityContext(DEFAULT_KS_NAME, DEFAULT_KS_PASS,
                DEFAULT_TS_NAME, DEFAULT_TS_PASS);
        assertEquals(TestTools.AM_NEQ, DEFAULT_KS_NAME, scA.keyStoreName());
        assertEquals(TestTools.AM_NEQ, DEFAULT_KS_PASS, scA.keyStorePass());
        assertEquals(TestTools.AM_NEQ, DEFAULT_TS_NAME, scA.trustStoreName());
        assertEquals(TestTools.AM_NEQ, DEFAULT_TS_PASS, scA.trustStorePass());
    }

    @Test
    public void constructNullSecurityContext() {
        print(TestTools.EOL + "constructNullSecurityContext");
        scA = new SecurityContext();
        assertEquals(TestTools.AM_NEQ, EMPTY, scA.keyStoreName());
        assertEquals(TestTools.AM_NEQ, EMPTY, scA.keyStorePass());
        assertEquals(TestTools.AM_NEQ, EMPTY, scA.trustStoreName());
        assertEquals(TestTools.AM_NEQ, EMPTY, scA.trustStorePass());
    }

    @Test
    public void constructEmptySecurityContext() {
        print(TestTools.EOL + "constructEmptySecurityContext()");
        scA = new SecurityContext("", "", "", "");
        assertEquals(TestTools.AM_NEQ, EMPTY, scA.keyStoreName());
        assertEquals(TestTools.AM_NEQ, EMPTY, scA.keyStorePass());
        assertEquals(TestTools.AM_NEQ, EMPTY, scA.trustStoreName());
        assertEquals(TestTools.AM_NEQ, EMPTY, scA.trustStorePass());
    }

    @Test
    public void eqSame() {
        print(TestTools.EOL + "eqSame()");
        scA = new SecurityContext(DEFAULT_KS_NAME, DEFAULT_KS_PASS,
                DEFAULT_TS_NAME, DEFAULT_TS_PASS);
        scB = new SecurityContext(DEFAULT_KS_NAME, DEFAULT_KS_PASS,
                DEFAULT_TS_NAME, DEFAULT_TS_PASS);
        TestTools.verifyEqual(scA, scB);
        TestTools.verifyEqual(scA, scA);
    }

    @Test
    public void eqDifferent() {
        print(TestTools.EOL + "eqDifferent()");
        scA = new SecurityContext(DEFAULT_KS_NAME, DEFAULT_KS_PASS,
                DEFAULT_TS_NAME, DEFAULT_TS_PASS);
        scB = new SecurityContext(OTHER_KS_NAME, OTHER_KS_PASS,
                OTHER_TS_NAME, OTHER_TS_PASS);
        TestTools.verifyNotEqual(scA, scB);
    }

    @Test
    public void notEqNull() {
        print(TestTools.EOL + "notEqNull()");
        scA = new SecurityContext();
        scB = new SecurityContext(OTHER_KS_NAME, OTHER_KS_PASS,
                OTHER_TS_NAME, OTHER_TS_PASS);
        TestTools.verifyNotEqual(scA, scB);
    }

    @Test
    public void eqNullNull() {
        print(TestTools.EOL + "eqNullNull()");
        scA = new SecurityContext();
        scB = new SecurityContext(null, null, null, null);
        TestTools.verifyEqual(scA, scB);
    }
}
