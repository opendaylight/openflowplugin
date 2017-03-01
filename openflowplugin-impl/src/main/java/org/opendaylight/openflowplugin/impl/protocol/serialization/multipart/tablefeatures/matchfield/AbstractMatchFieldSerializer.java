/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures.matchfield;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;

public abstract class AbstractMatchFieldSerializer implements OFSerializer<SetFieldMatch> {

    @Override
    public void serialize(final SetFieldMatch setFieldMatch, final ByteBuf byteBuf) {
        byteBuf.writeShort(getOxmClassCode());

        int fieldAndMask = getOxmFieldCode() << 1;
        int length = getValueLength();

        if (setFieldMatch.isHasMask()) {
            fieldAndMask |= 1;
            length *= 2;
        }

        byteBuf.writeByte(fieldAndMask);
        byteBuf.writeByte(length);
    }

    protected abstract int getOxmClassCode();
    protected abstract int getOxmFieldCode();
    protected abstract int getValueLength();

}
