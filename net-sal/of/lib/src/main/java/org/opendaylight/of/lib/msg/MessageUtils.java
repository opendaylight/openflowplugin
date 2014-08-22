/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

/**
 * Provides utility methods for working with OpenFlow messages.
 *
 * @author Simon Hunt
 */
public class MessageUtils {

    /**
     * Returns a reference to the backing byte array of packet data in a
     * mutable packet out message. Note that this renders null and void
     * the guarantee of immutability of the {@link OfmPacketOut} message,
     * since we are handing out a reference to the backing byte array of
     * the packet data.
     *
     * @param mpo mutable packet out
     * @return a reference to the backing data
     */
    public static byte[] getPacketBytes(OfmMutablePacketOut mpo) {
        return mpo.getPacketBytes();
    }
}
