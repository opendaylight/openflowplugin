/*
 * Copyright (c) 2019 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.PortNumberWithMask;
import org.opendaylight.yangtools.yang.common.Uint16;

public abstract class AbstractPortNumberWithMaskEntrySerializer extends AbstractMatchEntrySerializer<PortNumberWithMask, PortNumber> {
    protected AbstractPortNumberWithMaskEntrySerializer(final int oxmClassCode, final int oxmFieldCode) {
        super(new ConstantHeaderWriter<>(oxmClassCode, oxmFieldCode, EncodeConstants.SIZE_OF_SHORT_IN_BYTES));
    }

    @Override
    protected abstract PortNumberWithMask extractEntry(final Match match);

    @Override
    protected final PortNumber extractEntryMask(final PortNumberWithMask entry) {
        return entry.getMask();
    }

    @Override
    protected final void serializeEntry(final PortNumberWithMask entry, final PortNumber mask, final ByteBuf outBuffer) {
        outBuffer.writeShort(entry.getPort().getValue().shortValue());
        if (mask != null) {
            outBuffer.writeShort(mask.getValue().shortValue());
        }
    }
}
