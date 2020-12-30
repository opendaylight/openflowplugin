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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCase;

/**
 * OxmIpv4Src match entry serializer.
 *
 * @author michal.polkorab
 */
public class OxmIpv4SrcSerializer extends AbstractOxmIpv4AddressSerializer {

    @Override
    public void serialize(final MatchEntry entry, final ByteBuf outBuffer) {
        super.serialize(entry, outBuffer);
        Ipv4SrcCase entryValue = (Ipv4SrcCase) entry.getMatchEntryValue();
        writeIpv4Address(entryValue.getIpv4Src().getIpv4Address(), outBuffer);
        if (entry.getHasMask()) {
            writeMask(entryValue.getIpv4Src().getMask(), outBuffer, EncodeConstants.GROUPS_IN_IPV4_ADDRESS);
        }
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.IPV4_SRC;
    }

    @Override
    protected int getValueLength() {
        return Integer.BYTES;
    }
}
