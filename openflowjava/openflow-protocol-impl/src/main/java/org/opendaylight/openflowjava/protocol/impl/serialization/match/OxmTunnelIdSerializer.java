/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TunnelIdCase;

/**
 * OxmTunnelId match entry serializer.
 *
 * @author michal.polkorab
 */
public class OxmTunnelIdSerializer extends AbstractOxmMatchEntrySerializer {
    @Override
    public void serialize(final MatchEntry entry, final ByteBuf outBuffer) {
        super.serialize(entry, outBuffer);
        TunnelIdCase entryValue = (TunnelIdCase) entry.getMatchEntryValue();
        outBuffer.writeBytes(entryValue.getTunnelId().getTunnelId());
        if (entry.getHasMask()) {
            writeMask(entryValue.getTunnelId().getMask(), outBuffer, Long.BYTES);
        }
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.TUNNEL_ID;
    }

    @Override
    protected int getValueLength() {
        return Long.BYTES;
    }
}
