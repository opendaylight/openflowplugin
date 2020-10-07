/*
 * Copyright (c) 2020 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yangtools.yang.common.Uint16;

public abstract class AbstractPortNumberWithMaskEntrySerilizer
        extends AbstractMatchEntrySerializer<Layer4Match, Uint16> {
    protected AbstractPortNumberWithMaskEntrySerilizer(final int oxmClassCode, final int oxmFieldCode) {
        super(oxmClassCode, oxmFieldCode, Short.BYTES);
    }

    @Override
    protected void serializeEntry(final Layer4Match entry, final Uint16 mask, final ByteBuf outBuffer) {
        Uint16 portMask = extractMask(entry);
        Uint16 port = extractPort(entry);
        if (port != null) {
            outBuffer.writeShort(port.shortValue());
        }
        if (portMask != null) {
            outBuffer.writeShort(portMask.shortValue());
        }
    }

    @Override
    protected Uint16 extractEntryMask(final Layer4Match entry) {
        return extractMask(entry);
    }

    protected abstract Uint16 extractPort(Layer4Match entry);

    protected abstract Uint16 extractMask(Layer4Match entry);
}
