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
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;

public abstract class AbstractMatchFieldSerializer implements OFSerializer<SetFieldMatch> {
    private final int oxmClass;
    private final int oxmField;
    private final int valueLength;

    protected AbstractMatchFieldSerializer(final int oxmField, final int valueLength) {
        this(OxmMatchConstants.OPENFLOW_BASIC_CLASS, oxmField, valueLength);
    }

    protected AbstractMatchFieldSerializer(final int oxmClass, final int oxmField, final int valueLength) {
        this.oxmClass = oxmClass;
        this.oxmField = oxmField;
        this.valueLength = valueLength;
    }

    @Override
    public final void serialize(final SetFieldMatch setFieldMatch, final ByteBuf byteBuf) {
        byteBuf.writeShort(oxmClass);

        int fieldAndMask = oxmField << 1;
        int length = valueLength;

        if (setFieldMatch.isHasMask()) {
            fieldAndMask |= 1;
            length *= 2;
        }

        byteBuf.writeByte(fieldAndMask);
        byteBuf.writeByte(length);
    }
}
