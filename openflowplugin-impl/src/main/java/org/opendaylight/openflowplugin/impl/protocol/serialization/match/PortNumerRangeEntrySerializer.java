/*
 * Copyright (c) 2020 Ericsson Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.PortNumberRange;
import org.opendaylight.yangtools.yang.common.Uint16;

public abstract class PortNumerRangeEntrySerializer extends AbstractMatchEntrySerializer<PortNumberRange, Uint16> {

    protected PortNumerRangeEntrySerializer(final int oxmClassCode, final int oxmFieldCode) {
        super(oxmClassCode, oxmFieldCode, EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
    }

    @Override
    protected PortNumberRange extractEntry(final Match match) {
        final PortNumberRange portNumberRange = extractPortNumberRange(match);
        return portNumberRange == null ? null : portNumberRange;
    }

    protected abstract @Nullable PortNumberRange extractPortNumberRange(Match match);

    @Override
    protected final Uint16 extractEntryMask(final PortNumberRange entry) {
        String portNumberRange = entry.getValue();
        if (portNumberRange.contains("/")) {
            return Uint16.ZERO;
        }
        return null;
    }

    @Override
    protected void serializeEntry(@NonNull PortNumberRange entry, @Nullable Uint16 mask, @NonNull ByteBuf outBuffer) {
        String portNumberRange = entry.getValue();
        if (portNumberRange.contains("/")) {
            int slashIndex = portNumberRange.indexOf("/");
            outBuffer.writeShort(Integer.parseInt(portNumberRange.substring(0, slashIndex)));
            outBuffer.writeShort(Integer.parseInt(portNumberRange.substring(slashIndex + 1)));
        } else {
            outBuffer.writeShort(Integer.parseInt(portNumberRange));
        }
    }
}
