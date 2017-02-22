/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MatchEntryDeserializer;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.AbstractDeserializerTest;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.key.MessageCodeMatchKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

public abstract class AbstractMatchEntryDeserializerTest extends AbstractDeserializerTest {

    private MatchEntryDeserializer deserializer;

    @Override
    protected void init() {
        deserializer = getRegistry().getDeserializer(new MessageCodeMatchKey(EncodeConstants.OF13_VERSION_ID,
                    EncodeConstants.EMPTY_LENGTH,
                    Match.class,
                    MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH));
    }

    protected Match deserialize(ByteBuf inBuffer) {
        final MatchBuilder builder = new MatchBuilder();
        deserializer.deserializeEntry(inBuffer, builder);
        return builder.build();
    }

    protected void writeHeader(ByteBuf inBuffer, boolean hasMask) {
        inBuffer.writeShort(getOxmClassCode());

        int fieldAndMask = getOxmFieldCode() << 1;
        int length = getValueLength();

        if (hasMask) {
            fieldAndMask |= 1;
            length *= 2;
        }

        inBuffer.writeByte(fieldAndMask);
        inBuffer.writeByte(length);
    }

    protected abstract int getOxmClassCode();
    protected abstract int getOxmFieldCode();
    protected abstract int getValueLength();

}
