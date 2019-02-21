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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.protocol.match.fields.Pbb;

public class PbbEntrySerializer extends AbstractMatchEntrySerializer<Pbb, Long> {
    public PbbEntrySerializer() {
        super(OxmMatchConstants.PBB_ISID, OxmMatchConstants.OPENFLOW_BASIC_CLASS, EncodeConstants.SIZE_OF_3_BYTES);
    }

    @Override
    protected Pbb extractEntry(Match match) {
        final ProtocolMatchFields protoFields = match.getProtocolMatchFields();
        return protoFields == null ? null : protoFields.getPbb();
    }

    @Override
    protected Long extractEntryMask(Pbb entry) {
        return entry.getPbbMask();
    }

    @Override
    protected void serializeEntry(Pbb entry, Long mask, ByteBuf outBuffer) {
        outBuffer.writeMedium(entry.getPbbIsid().intValue());
        if (mask != null) {
            writeMask(ByteUtil.unsignedMediumToBytes(mask), outBuffer, getValueLength());
        }
    }
}
