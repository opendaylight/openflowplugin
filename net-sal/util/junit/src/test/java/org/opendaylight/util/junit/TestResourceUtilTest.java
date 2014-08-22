/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import java.io.IOException;
import java.net.ServerSocket;

import junit.framework.Assert;

import org.junit.Test;

import org.opendaylight.util.junit.ThrowableTester.Instruction;

/**
 * {@link org.opendaylight.util.junit.TestResourceUtil} tests.
 * 
 * @author Fabiel Zuniga
 */
public class TestResourceUtilTest {

    @Test
    public void testGetAvailablePort() {
        int port = TestResourceUtil.getAvailablePort();
        Assert.assertTrue(isValidPort(port));
    }

    @Test
    public void testGetAvailablePorts() {
        int count = 5;
        int[] ports = TestResourceUtil.getAvailablePorts(count);
        Assert.assertNotNull(ports);
        Assert.assertEquals(count, ports.length);
        for (int i = 0; i < count; i++) {
            int port = ports[i];
            Assert.assertTrue(isValidPort(port));
            for (int j = i + 1; j < count; j++) {
                Assert.assertFalse(port == ports[j]);
            }
        }
    }

    @Test
    public void testGetAvailablePortsInvalid() {
        final int invalidCountTooSmall = 0;
        final int invalidCountTooBig = TestResourceUtil.RESERVED_PORTS_COUNT + 1;

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                TestResourceUtil.getAvailablePorts(invalidCountTooSmall);
            }
        });

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                TestResourceUtil.getAvailablePorts(invalidCountTooBig);
            }
        });
    }

    @Test
    public void testPortsAvailability() {
        Assert.assertTrue("Test port " + TestResourceUtil.getAvailablePort() 
                        + " is already in use and cannot be used for testing",
                        isAvailable(TestResourceUtil.getAvailablePort()));
        for (int port : TestResourceUtil.getAvailablePorts(TestResourceUtil.RESERVED_PORTS_COUNT)) {
            Assert.assertTrue("Test port " + port 
                              + " is already in use and cannot be used for testing",
                              isAvailable(port));
        }
    }

    private boolean isValidPort(int port) {
        // NOTE: Using PortNumber the validation is for free
        return port >= 0 && port <= 65535;
    }

    private boolean isAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
