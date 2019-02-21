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

public abstract class AbstractUint16EntrySerializer extends AbstractPrimitiveEntrySerializer<Integer> {
    protected AbstractUint16EntrySerializer(final int oxmClassCode, final int oxmFieldCode) {
        super(oxmClassCode, oxmFieldCode, EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
    }

    @Override
    protected final void serializeEntry(final Integer entry, final ByteBuf outBuffer) {
        outBuffer.writeShort(entry);
    }
}
