/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Tunnel;
import org.opendaylight.yangtools.yang.common.Uint64;

public class TunnelIdEntrySerializer extends AbstractMatchEntrySerializer<Tunnel, Uint64> {
    public TunnelIdEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.TUNNEL_ID, Long.BYTES);
    }

    @Override
    protected Tunnel extractEntry(Match match) {
        final Tunnel tunnel = match.getTunnel();
        return tunnel == null || tunnel.getTunnelId() == null ? null : tunnel;
    }

    @Override
    protected Uint64 extractEntryMask(Tunnel entry) {
        return entry.getTunnelMask();
    }

    @Override
    protected void serializeEntry(Tunnel entry, Uint64 mask, ByteBuf outBuffer) {
        outBuffer.writeBytes(ByteUtil.uint64toBytes(entry.getTunnelId()));
        if (mask != null) {
            writeMask(ByteUtil.uint64toBytes(mask), outBuffer, Long.BYTES);
        }
    }
}
