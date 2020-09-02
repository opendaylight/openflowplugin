/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6ExtHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yangtools.yang.common.Uint16;

public class Ipv6ExtHeaderEntrySerializer extends AbstractMatchEntrySerializer<Ipv6ExtHeader, Uint16> {
    public Ipv6ExtHeaderEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.IPV6_EXTHDR,
            Short.BYTES);
    }

    @Override
    protected Ipv6ExtHeader extractEntry(final Match match) {
        final Layer3Match l3Match = match.getLayer3Match();
        return l3Match instanceof Ipv6Match ? ((Ipv6Match) l3Match).getIpv6ExtHeader() : null;
    }

    @Override
    protected Uint16 extractEntryMask(final Ipv6ExtHeader entry) {
        return entry.getIpv6ExthdrMask();
    }

    @Override
    protected void serializeEntry(final Ipv6ExtHeader entry, final Uint16 mask, final ByteBuf outBuffer) {
        outBuffer.writeShort(entry.getIpv6Exthdr().shortValue());
        if (mask != null) {
            writeMask(ByteUtil.unsignedShortToBytes(mask), outBuffer, Short.BYTES);
        }
    }
}
