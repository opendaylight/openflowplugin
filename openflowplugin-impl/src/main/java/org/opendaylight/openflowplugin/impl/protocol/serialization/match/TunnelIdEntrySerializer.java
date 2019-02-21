/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Tunnel;

public class TunnelIdEntrySerializer extends AbstractMatchEntrySerializer<Tunnel, BigInteger> {
    public TunnelIdEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.TUNNEL_ID,
            EncodeConstants.SIZE_OF_LONG_IN_BYTES);
    }

    @Override
    protected Tunnel extractEntry(Match match) {
        final Tunnel tunnel = match.getTunnel();
        return tunnel == null || tunnel.getTunnelId() == null ? null : tunnel;
    }

    @Override
    protected BigInteger extractEntryMask(Tunnel entry) {
        return entry.getTunnelMask();
    }

    @Override
    protected void serializeEntry(Tunnel entry, BigInteger mask, ByteBuf outBuffer) {
        outBuffer.writeBytes(ByteUtil.convertBigIntegerToNBytes(entry.getTunnelId(),
            EncodeConstants.SIZE_OF_LONG_IN_BYTES));
        if (mask != null) {
            writeMask(ByteUtil.convertBigIntegerToNBytes(mask, EncodeConstants.SIZE_OF_LONG_IN_BYTES), outBuffer,
                EncodeConstants.SIZE_OF_LONG_IN_BYTES);
        }
    }
}
