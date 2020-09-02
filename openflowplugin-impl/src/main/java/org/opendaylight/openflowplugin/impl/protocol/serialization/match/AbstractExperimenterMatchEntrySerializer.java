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
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;

public abstract class AbstractExperimenterMatchEntrySerializer<E, M> extends AbstractMatchEntrySerializer<E, M> {
    private final int experimenterId;

    protected AbstractExperimenterMatchEntrySerializer(final HeaderWriter<E, M> headerWriter,
            final long experimenterId) {
        super(headerWriter);
        this.experimenterId = Long.valueOf(experimenterId).intValue();
    }

    protected AbstractExperimenterMatchEntrySerializer(final int oxmFieldCode, final int valueLength,
            final long experimenterId) {
        this(new ConstantHeaderWriter<>(OxmMatchConstants.EXPERIMENTER_CLASS, oxmFieldCode, valueLength,
                Integer.BYTES), experimenterId);
    }

    @Override
    protected final void serializeEntry(final E entry, final M mask, final ByteBuf outBuffer) {
        outBuffer.writeInt(experimenterId);
        serializeEntryContent(entry, mask, outBuffer);
    }

    /**
     * Extract the corresponding entry from a match.
     *
     * @param entry entry to serialize
     * @param outBuffer output buffer
     */
    protected abstract void serializeEntryContent(@NonNull E entry, @Nullable M mask, @NonNull ByteBuf outBuffer);
}
