/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;

public abstract class AbstractExperimenterMatchEntrySerializer<E, M> extends AbstractMatchEntrySerializer<E, M> {

    @Override
    protected void serializeHeader(boolean hasMask, ByteBuf outBuffer) {
        outBuffer.writeShort(getOxmClassCode());

        int fieldAndMask = getOxmFieldCode() << 1;
        int length = getValueLength();

        if (hasMask) {
            fieldAndMask |= 1;
            length *= 2;
        }

        outBuffer.writeByte(fieldAndMask);

        // Add length of experimenter ID to total length
        outBuffer.writeByte(length + EncodeConstants.SIZE_OF_INT_IN_BYTES);
    }

    @Override
    protected final void serializeEntry(E entry, M mask, ByteBuf outBuffer) {
        outBuffer.writeInt(Long.valueOf(getExperimenterId()).intValue());
        serializeEntryContent(entry, mask, outBuffer);
    }

    /**
     * Extract the corresponding entry from a match.
     *
     * @param entry entry to serialize
     * @param outBuffer output buffer
     */
    protected abstract void serializeEntryContent(@NonNull E entry, @Nullable M mask, @NonNull ByteBuf outBuffer);

    /**
     * Get experimenter id.
     *
     * @return experimenter match entry id
     */
    protected abstract long getExperimenterId();
}
