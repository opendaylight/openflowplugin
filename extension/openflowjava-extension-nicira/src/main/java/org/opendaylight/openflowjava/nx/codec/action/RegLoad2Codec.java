/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.action;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.Objects;
import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.experimenter.id.match.entry.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionRegLoad2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.aug.nx.action.ActionRegLoad2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load2.grouping.NxActionRegLoad2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load2.grouping.NxActionRegLoad2Builder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class RegLoad2Codec
        extends AbstractActionCodec
        implements SerializerRegistryInjector, DeserializerRegistryInjector {

    public static final byte SUBTYPE = 33; // NXAST_REG_LOAD2
    public static final NiciraActionSerializerKey SERIALIZER_KEY = new NiciraActionSerializerKey(
            EncodeConstants.OF_VERSION_1_3, ActionRegLoad2.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY = new NiciraActionDeserializerKey(
            EncodeConstants.OF_VERSION_1_3, SUBTYPE);
    private SerializerRegistry serializerRegistry;
    private DeserializerRegistry deserializerRegistry;

    @Override
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't recognize Objects.requireNonNull
    public Action deserialize(final ByteBuf message) {
        Objects.requireNonNull(deserializerRegistry);

        final int startIndex = message.readerIndex();
        ActionBuilder actionBuilder = deserializeHeader(message);

        int oxmClass = message.getUnsignedShort(message.readerIndex());
        int oxmField = message.getUnsignedByte(message.readerIndex() + Short.BYTES) >>> 1;
        MatchEntryDeserializerKey key = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3,
                oxmClass,
                oxmField);
        if (oxmClass == EncodeConstants.EXPERIMENTER_VALUE) {
            long expId = message.getUnsignedInt(message.readerIndex() + Short.BYTES + Byte.BYTES * 2);
            key.setExperimenterId(Uint32.valueOf(expId));
        }
        OFDeserializer<MatchEntry> matchDeserializer = deserializerRegistry.getDeserializer(key);
        MatchEntry matchEntry = matchDeserializer.deserialize(message);

        skipPadding(message, startIndex);

        NxActionRegLoad2 nxActionRegLoad2 = new NxActionRegLoad2Builder()
                .setMatchEntry(Collections.singletonList(matchEntry))
                .build();
        ActionRegLoad2 actionRegLoad2 = new ActionRegLoad2Builder().setNxActionRegLoad2(nxActionRegLoad2).build();
        return actionBuilder.setActionChoice(actionRegLoad2).build();
    }

    @Override
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't recognize Objects.requireNonNull
    public void serialize(final Action input, final ByteBuf outBuffer) {
        Objects.requireNonNull(serializerRegistry);

        final int startIndex = outBuffer.writerIndex();
        serializeHeader(EncodeConstants.EMPTY_LENGTH, SUBTYPE, outBuffer);

        ActionRegLoad2 actionRegLoad2 = (ActionRegLoad2) input.getActionChoice();
        NxActionRegLoad2 nxActionRegLoad2 = actionRegLoad2.getNxActionRegLoad2();
        MatchEntry matchEntry = nxActionRegLoad2.getMatchEntry().get(0);
        MatchEntrySerializerKey<?, ?> key = new MatchEntrySerializerKey<>(
                EncodeConstants.OF_VERSION_1_3,
                matchEntry.getOxmClass(),
                matchEntry.getOxmMatchField());
        if (ExperimenterClass.VALUE.equals(matchEntry.getOxmClass())) {
            ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) matchEntry.getMatchEntryValue();
            key.setExperimenterId(experimenterIdCase.getExperimenter().getExperimenter().getValue());
        }
        OFSerializer<MatchEntry> serializer = serializerRegistry.getSerializer(key);
        serializer.serialize(matchEntry, outBuffer);

        writePaddingAndSetLength(outBuffer, startIndex);
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry registry) {
        serializerRegistry = registry;
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry registry) {
        deserializerRegistry = registry;
    }
}
