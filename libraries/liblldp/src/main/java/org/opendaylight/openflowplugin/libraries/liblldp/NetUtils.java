/*
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.libraries.liblldp;

/**
 * Utility class containing the common utility functions needed for operating on networking data structures.
 */
final class NetUtils {
    /**
     * Constant holding the number of bytes in MAC Address.
     */
    private static final int MAC_ADDR_LENGTH_IN_BYTES = 6;

    /**
     * Constant holding the broadcast MAC address.
     */
    private static final byte[] BROADCAST_MAC_ADDR = {-1, -1, -1, -1, -1, -1};

    private NetUtils() {
        // Hidden on purpose
    }

    /**
     * Returns true if the MAC address is the broadcast MAC address and false otherwise.
     */
    public static boolean isBroadcastMACAddr(final byte[] macAddress) {
        if (macAddress.length == MAC_ADDR_LENGTH_IN_BYTES) {
            for (int i = 0; i < 6; i++) {
                if (macAddress[i] != BROADCAST_MAC_ADDR[i]) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Returns true if the MAC address is a multicast MAC address and false
     * otherwise. Note that this explicitly returns false for the broadcast MAC
     * address.
     */
    public static boolean isMulticastMACAddr(final byte[] macAddress) {
        if (macAddress.length == MAC_ADDR_LENGTH_IN_BYTES && !isBroadcastMACAddr(macAddress)) {
            return (macAddress[0] & 1) != 0;
        }
        return false;
    }
}
