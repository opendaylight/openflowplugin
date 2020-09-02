/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.action;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerLookup;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MatchEntryDeserializer;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.key.MessageCodeMatchKey;
import org.opendaylight.openflowplugin.impl.util.MatchUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

public class SetFieldActionDeserializer extends AbstractActionDeserializer {
    private static final MessageCodeKey MATCH_KEY = new MessageCodeMatchKey(
            EncodeConstants.OF13_VERSION_ID,
            EncodeConstants.EMPTY_LENGTH,
            Match.class,
            MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);

    private final DeserializerLookup registry;

    public SetFieldActionDeserializer(final DeserializerLookup registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public Action deserialize(final ByteBuf message) {
        final MatchEntryDeserializer deserializer = Preconditions.checkNotNull(registry).getDeserializer(MATCH_KEY);
        final MatchBuilder builder = new MatchBuilder();

        final int startIndex = message.readerIndex();
        processHeader(message);
        deserializer.deserializeEntry(message, builder);

        int paddingRemainder = (message.readerIndex() - startIndex) % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            message.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        return new SetFieldCaseBuilder()
                .setSetField(MatchUtil.transformMatch(builder.build(), SetField.class))
                .build();
    }

    @Override
    public Action deserializeHeader(final ByteBuf message) {
        processHeader(message);
        return new SetFieldCaseBuilder().build();
    }
}
