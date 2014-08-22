/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.net.MacAddress;



/**
 * BDDP data store (immutable) and associated {@link Lldp.Builder} (mutable).
 * This protocol delegates to LLDP for decoding and encoding.
 * <p>
 * There are no OpenFlow match fields that reference this protocol.
 *
 * @author Frank Wood
 */
public class Bddp extends Lldp {
 
    /** Standards-defined destination MAC address used by this protocol.
     * This value explicitly overrides the value for the LLDP protocol. */
    @SuppressWarnings("hiding")
    public static final MacAddress DST_MAC = MacAddress.BROADCAST;

    /**
     * Constructor used to create a BDDP instance from LLDP.
     * 
     * @param lldp LLDP protocol instance
     */
    public Bddp(Lldp lldp) {
        super(lldp);
    }
    
    @Override
    public ProtocolId id() {
        return ProtocolId.BDDP;
    }
}
