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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata;

public class MetadataEntrySerializer extends AbstractMatchEntrySerializer<Metadata, BigInteger> {
    @Override
    protected Metadata extractEntry(Match match) {
        return match.getMetadata();
    }

    @Override
    protected BigInteger extractEntryMask(Metadata entry) {
        return entry.getMetadataMask();
    }

    @Override
    protected void serializeEntry(Metadata entry, BigInteger mask, ByteBuf outBuffer) {
        outBuffer.writeBytes(ByteUtil.convertBigIntegerToNBytes(entry.getMetadata(), getValueLength()));
        if (mask != null) {
            writeMask(ByteUtil.convertBigIntegerToNBytes(mask, getValueLength()), outBuffer, getValueLength());
        }
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.METADATA;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getValueLength() {
        return EncodeConstants.SIZE_OF_LONG_IN_BYTES;
    }
}
