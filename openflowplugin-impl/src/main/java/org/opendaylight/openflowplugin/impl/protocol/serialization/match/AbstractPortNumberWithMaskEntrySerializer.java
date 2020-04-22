/*
 * Copyright (c) 2020 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

public abstract class AbstractPortNumberWithMaskEntrySerializer extends AbstractMatchEntrySerializer<PortNumber, PortNumber> {

    protected AbstractPortNumberWithMaskEntrySerializer(final int oxmClassCode, final int oxmFieldCode) {
        super(new ConstantHeaderWriter<>(oxmClassCode, oxmFieldCode, EncodeConstants.SIZE_OF_SHORT_IN_BYTES));
    }

    protected @Nullable PortNumber extractEntry(Match match) {
        return extractPort(match);
    }

    protected abstract @Nullable PortNumber extractPort(Match match);

    protected @Nullable PortNumber extractEntryMask(@NonNull PortNumber entry) {
        return null;
    }

    protected  final void serializeEntry(@NonNull PortNumber entry, @Nullable PortNumber mask, @NonNull ByteBuf outBuffer) {
        outBuffer.writeShort(entry.getValue().shortValue());
        if (mask != null) {
            outBuffer.writeShort(mask.getValue().shortValue());
        }
    }
}
