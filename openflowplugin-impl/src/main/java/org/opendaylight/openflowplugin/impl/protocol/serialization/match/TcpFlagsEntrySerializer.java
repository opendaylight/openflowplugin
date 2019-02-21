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

public class TcpFlagsEntrySerializer extends AbstractExperimenterMatchEntrySerializer<TcpFlagsMatch, Integer> {
    public TcpFlagsEntrySerializer() {
        super(EncodeConstants.ONFOXM_ET_TCP_FLAGS, EncodeConstants.SIZE_OF_SHORT_IN_BYTES,
            EncodeConstants.ONF_EXPERIMENTER_ID);
    }

    @Override
    protected TcpFlagsMatch extractEntry(Match match) {
        final TcpFlagsMatch flagsMatch = match.getTcpFlagsMatch();
        return flagsMatch == null || flagsMatch.getTcpFlags() == null ? null : flagsMatch;
    }

    @Override
    protected Integer extractEntryMask(TcpFlagsMatch entry) {
        return entry.getTcpFlagsMask();
    }

    @Override
    protected void serializeEntryContent(TcpFlagsMatch entry, Integer mask, ByteBuf outBuffer) {
        outBuffer.writeShort(entry.getTcpFlags());
        if (mask != null) {
            writeMask(ByteUtil.unsignedShortToBytes(mask), outBuffer, EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        }
    }
}
