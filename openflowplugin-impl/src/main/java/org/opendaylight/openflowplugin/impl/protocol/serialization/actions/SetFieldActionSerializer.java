/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

public class SetFieldActionSerializer extends AbstractActionSerializer implements SerializerRegistryInjector {
    private SerializerRegistry registry;

    @Override
    public void serialize(Action action, ByteBuf outBuffer) {
        // Serialize field type and save position
        int startIndex = outBuffer.writerIndex();
        outBuffer.writeShort(getType());
        int lengthIndex = outBuffer.writerIndex();
        outBuffer.writeShort(EncodeConstants.EMPTY_LENGTH);

        // Serialize match (using small workaround with serializeHeader method to serialize only match entries)
        final SetField setField = SetFieldCase.class.cast(action).getSetField();
        final HeaderSerializer<Match> serializer = registry
                .getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, Match.class));
        serializer.serializeHeader(setField, outBuffer);

        // Serialize padding based on match length
        int paddingRemainder = (outBuffer.writerIndex() - startIndex) % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            outBuffer.writeZero(EncodeConstants.PADDING - paddingRemainder);
        }
        outBuffer.setShort(lengthIndex, outBuffer.writerIndex() - startIndex);
    }

    @Override
    protected int getLength() {
        return ActionConstants.GENERAL_ACTION_LENGTH;
    }

    @Override
    protected int getType() {
        return ActionConstants.SET_FIELD_CODE;
    }

    @Override
    public void injectSerializerRegistry(SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }
}
