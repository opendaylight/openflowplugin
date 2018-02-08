/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.action;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;

/**
 * Serializes OF 1.3 SetField actions.
 *
 * @author michal.polkorab
 */
public class OF13SetFieldActionSerializer implements OFSerializer<Action>,
        HeaderSerializer<Action>, SerializerRegistryInjector {

    private SerializerRegistry registry;

    @Override
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't recognize Objects.requireNonNull
    public void serialize(Action action, ByteBuf outBuffer) {
        Objects.requireNonNull(registry);

        final int startIndex = outBuffer.writerIndex();
        outBuffer.writeShort(ActionConstants.SET_FIELD_CODE);
        final int lengthIndex = outBuffer.writerIndex();
        outBuffer.writeShort(EncodeConstants.EMPTY_LENGTH);
        MatchEntry entry = ((SetFieldCase) action.getActionChoice()).getSetFieldAction()
                .getMatchEntry().get(0);
        MatchEntrySerializerKey<?, ?> key = new MatchEntrySerializerKey<>(
                EncodeConstants.OF13_VERSION_ID, entry.getOxmClass(), entry.getOxmMatchField());
        if (entry.getOxmClass().equals(ExperimenterClass.class)) {
            ExperimenterIdCase experimenterIdCase = (ExperimenterIdCase) entry.getMatchEntryValue();
            key.setExperimenterId(experimenterIdCase.getExperimenter().getExperimenter().getValue());
        } else {
            key.setExperimenterId(null);
        }
        OFSerializer<MatchEntry> serializer = registry.getSerializer(key);
        serializer.serialize(entry, outBuffer);
        int paddingRemainder = (outBuffer.writerIndex() - startIndex) % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            outBuffer.writeZero(EncodeConstants.PADDING - paddingRemainder);
        }
        outBuffer.setShort(lengthIndex, outBuffer.writerIndex() - startIndex);
    }

    @Override
    public void serializeHeader(Action input, ByteBuf outBuffer) {
        outBuffer.writeShort(ActionConstants.SET_FIELD_CODE);
        outBuffer.writeShort(ActionConstants.ACTION_IDS_LENGTH);
    }

    @Override
    public void injectSerializerRegistry(SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }

}
