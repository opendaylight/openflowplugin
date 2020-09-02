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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata;
import org.opendaylight.yangtools.yang.common.Uint64;

public class MetadataEntrySerializer extends AbstractMatchEntrySerializer<Metadata, Uint64> {
    public MetadataEntrySerializer() {
        super(OxmMatchConstants.OPENFLOW_BASIC_CLASS, OxmMatchConstants.METADATA,
            Long.BYTES);
    }

    @Override
    protected Metadata extractEntry(Match match) {
        return match.getMetadata();
    }

    @Override
    protected Uint64 extractEntryMask(Metadata entry) {
        return entry.getMetadataMask();
    }

    @Override
    protected void serializeEntry(Metadata entry, Uint64 mask, ByteBuf outBuffer) {
        outBuffer.writeBytes(ByteUtil.uint64toBytes(entry.getMetadata()));
        if (mask != null) {
            writeMask(ByteUtil.uint64toBytes(mask), outBuffer, Long.BYTES);
        }
    }
}
