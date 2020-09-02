/*
 * Copyright (c) 2019 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yangtools.yang.common.Uint32;

public abstract class AbstractUint32EntrySerializer extends AbstractPrimitiveEntrySerializer<Uint32> {
    protected AbstractUint32EntrySerializer(final int oxmClassCode, final int oxmFieldCode) {
        super(oxmClassCode, oxmFieldCode, Integer.BYTES);
    }

    @Override
    protected final void serializeEntry(final Uint32 entry, final ByteBuf outBuffer) {
        outBuffer.writeInt(entry.intValue());
    }
}
