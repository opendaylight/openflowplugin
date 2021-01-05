/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * OF13SetFieldActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF13SetFieldActionDeserializer extends AbstractActionCaseDeserializer<SetFieldCase>
        implements DeserializerRegistryInjector {
    private DeserializerRegistry registry = null;

    public OF13SetFieldActionDeserializer() {
        super(new SetFieldCaseBuilder().build());
    }

    @Override
    public SetFieldCase deserializeAction(final ByteBuf input) {
        final int startIndex = input.readerIndex();

        input.skipBytes(2 * Short.BYTES);
        int oxmClass = input.getUnsignedShort(input.readerIndex());
        // get oxm_field & hasMask byte and extract the field value
        int oxmField = input.getUnsignedByte(input.readerIndex() + Short.BYTES) >>> 1;
        MatchEntryDeserializerKey key = new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID,
                oxmClass, oxmField);
        if (oxmClass == EncodeConstants.EXPERIMENTER_VALUE) {
            long expId = input.getUnsignedInt(input.readerIndex() + Short.BYTES + 2 * Byte.BYTES);
            key.setExperimenterId(Uint32.valueOf(expId));
        }

        final OFDeserializer<MatchEntry> matchDeserializer = requireNonNull(registry).getDeserializer(key);
        final var entry = matchDeserializer.deserialize(input);

        int paddingRemainder = (input.readerIndex() - startIndex) % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            input.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        return new SetFieldCaseBuilder()
            .setSetFieldAction(new SetFieldActionBuilder().setMatchEntry(List.of(entry)).build())
            .build();
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        this.registry = requireNonNull(deserializerRegistry);
    }
}
