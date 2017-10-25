/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4DstCase;

/**
 * @author michal.polkorab
 *
 */
public class OxmIpv4DstSerializer extends AbstractOxmIpv4AddressSerializer {

    @Override
    public void serialize(final MatchEntry entry, final ByteBuf outBuffer) {
        super.serialize(entry, outBuffer);
        Ipv4DstCase entryValue = (Ipv4DstCase) entry.getMatchEntryValue();
        writeIpv4Address(entryValue.getIpv4Dst().getIpv4Address(), outBuffer);
        if (entry.isHasMask()) {
            writeMask(entryValue.getIpv4Dst().getMask(), outBuffer,
                    EncodeConstants.GROUPS_IN_IPV4_ADDRESS);
        }
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.IPV4_DST;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_INT_IN_BYTES;
    }
}
