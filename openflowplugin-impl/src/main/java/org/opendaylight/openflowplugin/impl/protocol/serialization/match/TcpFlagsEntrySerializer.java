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
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagsMatch;
import org.opendaylight.yangtools.yang.common.Uint16;

public class TcpFlagsEntrySerializer extends AbstractExperimenterMatchEntrySerializer<TcpFlagsMatch, Uint16> {
    public TcpFlagsEntrySerializer() {
        super(EncodeConstants.ONFOXM_ET_TCP_FLAGS, Short.BYTES,
            EncodeConstants.ONF_EXPERIMENTER_ID.toJava());
    }

    @Override
    protected TcpFlagsMatch extractEntry(Match match) {
        final TcpFlagsMatch flagsMatch = match.getTcpFlagsMatch();
        return flagsMatch == null || flagsMatch.getTcpFlags() == null ? null : flagsMatch;
    }

    @Override
    protected Uint16 extractEntryMask(TcpFlagsMatch entry) {
        return entry.getTcpFlagsMask();
    }

    @Override
    protected void serializeEntryContent(TcpFlagsMatch entry, Uint16 mask, ByteBuf outBuffer) {
        outBuffer.writeShort(entry.getTcpFlags().shortValue());
        if (mask != null) {
            writeMask(ByteUtil.unsignedShortToBytes(mask), outBuffer, Short.BYTES);
        }
    }
}
