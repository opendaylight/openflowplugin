/*
 * Copyright (c) 2019 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;

public abstract class AbstractPrimitiveEntrySerializer<E> extends AbstractMatchEntrySerializer<E, Void> {
    protected AbstractPrimitiveEntrySerializer(final HeaderWriter<E, Void> headerWriter) {
        super(headerWriter);
    }

    protected AbstractPrimitiveEntrySerializer(final int oxmClassCode, final int oxmFieldCode, final int valueLength) {
        super(oxmClassCode, oxmFieldCode, valueLength);
    }

    @Override
    protected final Void extractEntryMask(final E entry) {
        return null;
    }

    @Override
    protected final void serializeEntry(final E entry, final Void mask, final ByteBuf outBuffer) {
        serializeEntry(entry, outBuffer);
    }

    protected abstract void serializeEntry(@NonNull E entry, @NonNull ByteBuf outBuffer);
}
