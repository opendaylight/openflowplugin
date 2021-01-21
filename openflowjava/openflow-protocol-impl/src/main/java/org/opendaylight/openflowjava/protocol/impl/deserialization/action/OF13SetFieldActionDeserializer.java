/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.MatchEntryDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * OF13SetFieldActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF13SetFieldActionDeserializer extends AbstractActionCaseDeserializer<SetFieldCase> {
    // FIXME: @MetaInfServices somehow
    @Singleton
    public static final class Provider implements OFProvider {
        private final MatchEntryDeserializer.OFRegistry.Versioned ofRegistry;
        private final MatchEntryDeserializer.ExperimenterRegistry.Versioned expRegistry;

        @Inject
        public Provider(final MatchEntryDeserializer.OFRegistry.Versioned ofRegistry,
                final MatchEntryDeserializer.ExperimenterRegistry.Versioned expRegistry) {
            this.ofRegistry = requireNonNull(ofRegistry);
            this.expRegistry = requireNonNull(expRegistry);
        }

        @Override
        public Uint8 version() {
            return EncodeConstants.OF_VERSION_1_3;
        }

        @Override
        public Uint16 type() {
            return Uint16.valueOf(ActionConstants.SET_FIELD_CODE);
        }

        @Override
        public ActionDeserializer deserializer() {
            return new OF13SetFieldActionDeserializer(ofRegistry, expRegistry);
        }
    }

    @Component
    // FIXME: merge with provider once we have OSGi R7
    public static final class OSGiProvider implements OFProvider {
        @Reference
        MatchEntryDeserializer.OFRegistry.Versioned ofRegistry;
        @Reference
        MatchEntryDeserializer.ExperimenterRegistry.Versioned expRegistry;

        @Override
        public Uint8 version() {
            return EncodeConstants.OF_VERSION_1_3;
        }

        @Override
        public Uint16 type() {
            return Uint16.valueOf(ActionConstants.SET_FIELD_CODE);
        }

        @Override
        public ActionDeserializer deserializer() {
            return new OF13SetFieldActionDeserializer(ofRegistry, expRegistry);
        }
    }

    private final MatchEntryDeserializer.OFRegistry ofRegistry;
    private final MatchEntryDeserializer.ExperimenterRegistry expRegistry;

    public OF13SetFieldActionDeserializer(final MatchEntryDeserializer.OFRegistry.Versioned ofRegistry,
            final MatchEntryDeserializer.ExperimenterRegistry.Versioned expRegistry) {
        super(new SetFieldCaseBuilder().build());
        this.ofRegistry = requireNonNull(ofRegistry.lookupMatchRegistry(EncodeConstants.OF_VERSION_1_3));
        this.expRegistry = requireNonNull(expRegistry.lookupMatchRegistry(EncodeConstants.OF_VERSION_1_3));
    }

    @Override
    public SetFieldCase deserializeAction(final ByteBuf input) {
        final int startIndex = input.readerIndex();

        input.skipBytes(2 * Short.BYTES);
        final int oxmClass = input.getUnsignedShort(input.readerIndex());
        // get oxm_field & hasMask byte and extract the field value
        final int oxmField = input.getUnsignedByte(input.readerIndex() + Short.BYTES) >>> 1;

        final MatchEntryDeserializer matchDeserializer;
        if (oxmClass == EncodeConstants.EXPERIMENTER_VALUE) {
            final Uint32 expId =
                Uint32.valueOf(input.getUnsignedInt(input.readerIndex() + Short.BYTES + 2 * Byte.BYTES));
            matchDeserializer = expRegistry.lookupMatch(oxmField, expId);
            checkState(matchDeserializer != null, "No deserializer for experimenter %s field %s", expId, oxmField);
        } else {
            matchDeserializer = ofRegistry.lookupMatch(oxmClass, oxmField);
            checkState(matchDeserializer != null, "No deserializer for class %s field %s", oxmClass, oxmField);
        }

        final var entry = matchDeserializer.deserialize(input);
        int paddingRemainder = (input.readerIndex() - startIndex) % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            input.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        return new SetFieldCaseBuilder()
            .setSetFieldAction(new SetFieldActionBuilder().setMatchEntry(List.of(entry)).build())
            .build();
    }
}
