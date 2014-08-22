/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.Test;
import org.opendaylight.util.api.security.SecurityContext;
import org.opendaylight.util.net.IpAddress;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.*;
import static org.opendaylight.of.controller.impl.ControllerConfig.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link ControllerConfig}.
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Sudheer Duggisetty
 */
public class ControllerConfigTest extends AbstractTest {

    private static final Set<IpAddress> ADDR =
            new TreeSet<>(Arrays.asList(ip("15.45.1.2"), ip("16.12.13.14")));
    private static final Set<IpAddress> ADDR2 =
            new TreeSet<>(Arrays.asList(ip("15.45.1.2")));

    private static final Set<IpAddress> EMPTY = new TreeSet<>();

    private static final int PORT = 1234;
    private static final int TLS_PORT = 5678;
    private static final int UDP_PORT = 9012;
    private static final int RCV_BUF_SIZE = 104857;
    private static final int UDP_RCV_BUF_SIZE = 2048;
    private static final int SOME_NUMBER = 7787;
    private static final int MORE_WORKERS = 100;

    private static final String KS_NAME = "controller.jks";
    private static final String KS_PASS = "skyline";
    private static final String TS_NAME = "controller.jks";
    private static final String TS_PASS = "skyline";


    private static final SecurityContext SEC_CTX =
            new SecurityContext(KS_NAME, KS_PASS, TS_NAME, TS_PASS);
    private static final SecurityContext SEC_CTX_2 =
            new SecurityContext(KS_NAME, KS_PASS, "foo", "bar");


    private static final String EXP_DEF_STR =
        "{ControllerConfig: i/f=ALL,port=6633,tls=6634,udp=6635,secCtx=null," +
                "rcvBufSize=1048576,udpRcvBufSize=1024," +
                "workerCount=16,idleCheckMs=500,maxIdleMs=5000," +
                "maxEchoMs=5000,maxEchoAttempts=5}";
    private static final String EXP_NON_DEF_STR =
        "{ControllerConfig: i/f=[15.45.1.2, 16.12.13.14],port=1234,tls=5678," +
                "udp=9012,secCtx=null,suppressSetConfig,hybridMode," +
                "rcvBufSize=104857,udpRcvBufSize=2048,workerCount=16," +
                "idleCheckMs=500,maxIdleMs=5000," +
                "maxEchoMs=5000,maxEchoAttempts=5}";
    private static final String EXP_SEC_STR =
        "{ControllerConfig: i/f=ALL,port=6633,tls=6634,udp=6635,secCtx=****," +
                "rcvBufSize=1048576,udpRcvBufSize=1024,workerCount=16," +
                "idleCheckMs=500,maxIdleMs=5000," +
                "maxEchoMs=5000,maxEchoAttempts=5}";

    ControllerConfig cfg;
    ControllerConfig old;

    private ControllerConfig.Builder bld() {
        return new ControllerConfig.Builder();
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        cfg = bld().build();
        print(cfg);
        assertEquals(AM_NEQ, EXP_DEF_STR, cfg.toString());

        assertNull(AM_HUH, cfg.addresses());
        assertEquals(AM_NEQ, DEF_PORT, cfg.listenPort());
        assertEquals(AM_NEQ, DEF_TLS_PORT, cfg.tlsListenPort());
        assertEquals(AM_NEQ, DEF_UDP_PORT, cfg.udpPort());
        assertNull(AM_HUH, cfg.securityContext());
        assertFalse(AM_HUH, cfg.suppressSetConfig());
        assertEquals(AM_NEQ, DEF_RCV_BUF_SIZE, cfg.rcvBufSize());
        assertEquals(AM_NEQ, DEF_UDP_RCV_BUF_SIZE, cfg.udpRcvBufSize());
        assertEquals(AM_NEQ, DEF_WORKER_COUNT, cfg.workerCount());
        assertEquals(AM_NEQ, DEF_IDLE_CHECK_MS, cfg.idleCheckMs());
        assertEquals(AM_NEQ, DEF_MAX_IDLE_MS, cfg.maxIdleMs());
        assertEquals(AM_NEQ, DEF_MAX_ECHO_MS, cfg.maxEchoMs());
        assertEquals(AM_NEQ, DEF_MAX_ECHO_ATTEMPTS, cfg.maxEchoAttempts());
        assertFalse(AM_HUH, cfg.strictMessageParsing());
        assertFalse(AM_HUH, cfg.hybridMode());
    }

    @Test
    public void nonDefault() {
        print(EOL + "nonDefault()");
        cfg = bld().addresses(ADDR).listenPort(PORT).tlsListenPort(TLS_PORT)
                .udpPort(UDP_PORT).suppressSetConfig().hybridMode(true)
                .rcvBufSize(RCV_BUF_SIZE).udpRcvBufSize(UDP_RCV_BUF_SIZE)
                .build();
        print(cfg);
        assertEquals(AM_NEQ, EXP_NON_DEF_STR, cfg.toString());

        assertEquals(AM_NEQ, ADDR, cfg.addresses());
        assertEquals(AM_NEQ, PORT, cfg.listenPort());
        assertEquals(AM_NEQ, TLS_PORT, cfg.tlsListenPort());
        assertEquals(AM_NEQ, UDP_PORT, cfg.udpPort());
        assertNull(AM_HUH, cfg.securityContext());
        assertTrue(AM_HUH, cfg.suppressSetConfig());
        assertEquals(AM_NEQ, RCV_BUF_SIZE, cfg.rcvBufSize());
        assertEquals(AM_NEQ, UDP_RCV_BUF_SIZE, cfg.udpRcvBufSize());
    }

    @Test
    public void withSecurity() {
        print(EOL + "withSecurity()");
        cfg = bld().securityContext(SEC_CTX).build();
        print(cfg);
        assertEquals(AM_NEQ, EXP_SEC_STR, cfg.toString());

        assertNull(AM_HUH, cfg.addresses());
        assertEquals(AM_NEQ, DEF_PORT, cfg.listenPort());
        assertEquals(AM_NEQ, DEF_TLS_PORT, cfg.tlsListenPort());
        assertEquals(AM_NEQ, DEF_UDP_PORT, cfg.udpPort());
        verifyEqual(SEC_CTX, cfg.securityContext());
        assertFalse(AM_HUH, cfg.suppressSetConfig());
        assertEquals(AM_NEQ, DEF_RCV_BUF_SIZE, cfg.rcvBufSize());
        assertEquals(AM_NEQ, DEF_UDP_RCV_BUF_SIZE, cfg.udpRcvBufSize());
    }

    @Test
    public void nullAddresses() {
        cfg = bld().addresses(null).build();
        assertNull(AM_HUH, cfg.addresses());
    }

    @Test
    public void emptyAddresses() {
        // make sure that an empty set is always converted to null
        cfg = bld().addresses(EMPTY).build();
        assertNull(AM_HUH, cfg.addresses());
    }

    @Test
    public void addressesDiffer() {
        cfg = bld().build();
        assertFalse(AM_HUH, cfg.addressesDiffer(null));
        assertTrue(AM_HUH, cfg.addressesDiffer(ADDR));
        assertTrue(AM_HUH, cfg.addressesDiffer(ADDR2));

        cfg = bld().addresses(ADDR).build();
        assertTrue(AM_HUH, cfg.addressesDiffer(null));
        assertFalse(AM_HUH, cfg.addressesDiffer(ADDR));
        assertTrue(AM_HUH, cfg.addressesDiffer(ADDR2));
    }

    @Test
    public void securityContextsDiffer() {
        cfg = bld().build();
        assertFalse(AM_HUH, cfg.securityContextsDiffer(null));
        assertTrue(AM_HUH, cfg.securityContextsDiffer(SEC_CTX));
        assertTrue(AM_HUH, cfg.securityContextsDiffer(SEC_CTX_2));

        cfg = bld().securityContext(SEC_CTX).build();
        assertTrue(AM_HUH, cfg.securityContextsDiffer(null));
        assertFalse(AM_HUH, cfg.securityContextsDiffer(SEC_CTX));
        assertTrue(AM_HUH, cfg.securityContextsDiffer(SEC_CTX_2));
    }

    @Test
    public void bounceRequired() {
        cfg = bld().build();
        assertTrue(AM_HUH, cfg.bounceRequired(null));

        old = bld().build();
        assertFalse(AM_HUH, cfg.bounceRequired(old));

        old = bld().suppressSetConfig().build();
        assertFalse(AM_HUH, cfg.bounceRequired(old));

        old = bld().listenPort(PORT).build();
        assertTrue(AM_HUH, cfg.bounceRequired(old));
        old = bld().tlsListenPort(TLS_PORT).build();
        assertTrue(AM_HUH, cfg.bounceRequired(old));
        old = bld().udpPort(UDP_PORT).build();
        assertTrue(AM_HUH, cfg.bounceRequired(old));

        old = bld().addresses(ADDR).build();
        assertTrue(AM_HUH, cfg.bounceRequired(old));
        old = bld().securityContext(SEC_CTX).build();
        assertTrue(AM_HUH, cfg.bounceRequired(old));

        old = bld().rcvBufSize(RCV_BUF_SIZE).build();
        assertTrue(AM_HUH, cfg.bounceRequired(old));
        old = bld().udpRcvBufSize(UDP_RCV_BUF_SIZE).build();
        assertTrue(AM_HUH, cfg.bounceRequired(old));

        old = bld().hybridMode(false).build();
        assertFalse(AM_HUH, cfg.bounceRequired(old));
        old = bld().hybridMode(true).build();
        assertTrue(AM_HUH, cfg.bounceRequired(old));
    }

    @Test
    public void workerCount() {
        print(EOL + "workerCount()");
        cfg = bld().workerCount(MORE_WORKERS).build();
        print(cfg);
        assertEquals(AM_NEQ, MORE_WORKERS, cfg.workerCount());
    }

    @Test
    public void idleCheckMs() {
        print(EOL + "idleCheckMs()");
        cfg = bld().idleCheckMs(SOME_NUMBER).build();
        print(cfg);
        assertEquals(AM_NEQ, SOME_NUMBER, cfg.idleCheckMs());
    }

    @Test
    public void maxIdleMs() {
        print(EOL + "maxIdleMs()");
        cfg = bld().maxIdleMs(SOME_NUMBER).build();
        print(cfg);
        assertEquals(AM_NEQ, SOME_NUMBER, cfg.maxIdleMs());
    }

    @Test
    public void maxEchoMs() {
        print(EOL + "maxEchoMs()");
        cfg = bld().maxEchoMs(SOME_NUMBER).build();
        print(cfg);
        assertEquals(AM_NEQ, SOME_NUMBER, cfg.maxEchoMs());
    }

    @Test
    public void maxEchoAttempts() {
        print(EOL + "maxEchoAttempts()");
        cfg = bld().maxEchoAttempts(SOME_NUMBER).build();
        print(cfg);
        assertEquals(AM_NEQ, SOME_NUMBER, cfg.maxEchoAttempts());
    }

    @Test
    public void strictMessageParsing() {
        print(EOL + "strictMessageParsing()");
        cfg = bld().strictMessageParsing().build();
        print(cfg);
        assertTrue(AM_HUH, cfg.strictMessageParsing());
    }
    @Test
    public void hybridMode() {
        print(EOL + "hybridMode()");
        cfg = bld().hybridMode(true).build();
        print(cfg);
        assertTrue(AM_HUH, cfg.hybridMode());
        cfg = bld().hybridMode(false).build();
        print(cfg);
        assertFalse(AM_HUH, cfg.hybridMode());
    }

}
