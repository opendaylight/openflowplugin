/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.action;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MatchEntryDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

import io.netty.buffer.ByteBuf;

public class SetFieldActionDeserializer extends AbstractActionDeserializer
    implements DeserializerRegistryInjector {

    private static final MessageCodeKey MATCH_KEY = new MessageCodeKey(
            EncodeConstants.OF13_VERSION_ID,
            EncodeConstants.EMPTY_LENGTH,
            Match.class);

    private DeserializerRegistry registry;

    @Override
    public Action deserialize(ByteBuf message) {
        final MatchEntryDeserializer deserializer = registry.getDeserializer(MATCH_KEY);
        final MatchBuilder builder = new MatchBuilder();

        final int startIndex = message.readerIndex();
        processHeader(message);
        deserializer.deserializeEntry(message, builder);

        int paddingRemainder = (message.readerIndex() - startIndex) % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            message.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        return new SetFieldCaseBuilder()
            .setSetField(new SetFieldBuilder(builder.build()).build())
            .build();
    }

    @Override
    public Action deserializeHeader(ByteBuf message) {
        processHeader(message);
        return new SetFieldCaseBuilder().build();
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

}
