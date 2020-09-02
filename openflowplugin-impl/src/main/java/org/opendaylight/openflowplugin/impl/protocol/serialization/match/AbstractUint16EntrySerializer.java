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
import org.opendaylight.yangtools.yang.common.Uint16;

public abstract class AbstractUint16EntrySerializer extends AbstractPrimitiveEntrySerializer<Uint16> {
    protected AbstractUint16EntrySerializer(final int oxmClassCode, final int oxmFieldCode) {
        super(oxmClassCode, oxmFieldCode, Short.BYTES);
    }

    @Override
    protected final void serializeEntry(final Uint16 entry, final ByteBuf outBuffer) {
        outBuffer.writeShort(entry.shortValue());
    }
}
