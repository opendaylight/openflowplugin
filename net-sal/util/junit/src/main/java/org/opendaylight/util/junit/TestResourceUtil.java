/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

/**
 * Test resource utility methods.
 * 
 * @author Fabiel Zuniga
 */
public final class TestResourceUtil {

    static final int RESERVED_PORTS_COUNT = 10;
    private static final int BASE_PORT = 9990;
    private static int[] TEST_PORTS = new int[RESERVED_PORTS_COUNT];

    // NOTE: PortNumber would've been a better type for ports (instead of
    // int), however this module is a dependency of all the other modules,
    // including the one where PortNumber is defined at.

    static {
        for (int i = 0; i < RESERVED_PORTS_COUNT; i++) {
            TEST_PORTS[i] = BASE_PORT + i;
        }
    }

    private TestResourceUtil() {

    }

    /**
     * Gets a free port to bind services under test to (Server sockets for
     * example). If the test is properly written it won't hold resources and
     * thus the same port may be used among tests. Example:
     * 
     * <pre>
     * ServerSocket notCloseable = new ServerSocket(TestResourceUtil.getPort());
     * try {
     *     Assert.assertSomething(...);
     * } finally {
     *     serverSocket.close()
     * }
     * 
     * // or
     * 
     * try(ServerSocket closeable = new ServerSocket(TestResourceUtil.getPort())) {
     *     Assert.assertSomething(...);
     * }
     * </pre>
     * 
     * @return a test port
     */
    public static int getAvailablePort() {
        return TEST_PORTS[0];
    }

    /**
     * Gets a set of free ports to bind services under test to (Server sockets
     * for example). If the test is properly written it won't hold resources
     * and thus the same ports may be used among tests. Example:
     * 
     * <pre>
     * int[] ports = TestResourceUtil.getPorts(1);
     * ServerSocket notCloseable = new ServerSocket(ports[0]);
     * try {
     *     Assert.assertSomething(...);
     * } finally {
     *     serverSocket.close()
     * }
     * 
     * // or
     * 
     * Port[] ports = TestResourceUtil.getPorts(1);
     * try(ServerSocket closeable = new ServerSocket(ports[0])) {
     *     Assert.assertSomething(...);
     * }
     * </pre>
     * 
     * @param count number of ports needed by the test case
     * @return a set of test ports
     * @throws IllegalArgumentException if {@code count} is less or equals to
     *         zero
     * @throws UnsupportedOperationException if {@code count} is greater than
     *         the maximum ports reserved for tests
     */
    public static int[] getAvailablePorts(int count)
                                                    throws IllegalArgumentException,
                                           UnsupportedOperationException {
        if (count <= 0) {
            throw new IllegalArgumentException("At least one port must be reserved");
        }

        if (count > TEST_PORTS.length) {
            throw new IllegalArgumentException("At most " + TEST_PORTS.length
                    + " ports can be reserved");
        }

        int[] ports = new int[count];
        System.arraycopy(TEST_PORTS, 0, ports, 0, count);
        return ports;
    }
}
