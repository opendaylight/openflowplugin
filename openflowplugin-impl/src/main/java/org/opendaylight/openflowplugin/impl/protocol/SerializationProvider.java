/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.ActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdMeterSubTypeSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.InstructionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yangtools.yang.binding.DataObject;

public class SerializationProvider extends SerializationFactory implements SerializerExtensionProvider {
    private final SerializerRegistry serializerRegistry;
    private final SerializationFactory serializationFactory;
    private final boolean enabled;
    private final boolean useSingleLayer;

    public SerializationProvider(final boolean enabled, final boolean useSingleLayer) {
        super();
        this.enabled = enabled;
        this.useSingleLayer = useSingleLayer;
        serializerRegistry = new SerializerRegistryImpl();
        serializerRegistry.init();
        serializationFactory = new SerializationFactory();
        serializationFactory.setSerializerTable(serializerRegistry);
    }

    public boolean isAvailable() {
        return useSingleLayer && enabled;
    }

    public byte[] serialize(final short version, final DataObject message) {
        final ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        serializationFactory.messageToBuffer(version, byteBuf, message);
        final byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);
        byteBuf.release();
        return bytes;
    }

    @Override
    public <K> void registerSerializer(final MessageTypeKey<K> key, final OFGeneralSerializer serializer) {
        serializerRegistry.registerSerializer(key, serializer);
    }

    @Override
    public boolean unregisterSerializer(final ExperimenterSerializerKey key) {
        return false;
    }

    @Override
    public void registerActionSerializer(final ActionSerializerKey<?> key, final OFGeneralSerializer serializer) {

    }

    @Override
    public void registerInstructionSerializer(final InstructionSerializerKey<?> key, final OFGeneralSerializer serializer) {

    }

    @Override
    public <C extends OxmClassBase, F extends MatchField> void registerMatchEntrySerializer(final MatchEntrySerializerKey<C, F> key, final OFGeneralSerializer serializer) {

    }

    @Override
    public void registerExperimenterMessageSerializer(final ExperimenterIdSerializerKey<? extends ExperimenterDataOfChoice> key, final OFSerializer<? extends ExperimenterDataOfChoice> serializer) {

    }

    @Override
    public void registerMultipartRequestSerializer(final ExperimenterIdSerializerKey<? extends ExperimenterDataOfChoice> key, final OFSerializer<? extends ExperimenterDataOfChoice> serializer) {

    }

    @Override
    public void registerMultipartRequestTFSerializer(final ExperimenterIdSerializerKey<TableFeatureProperties> key, final OFGeneralSerializer serializer) {

    }

    @Override
    public void registerMeterBandSerializer(final ExperimenterIdSerializerKey<MeterBandExperimenterCase> key, final OFSerializer<MeterBandExperimenterCase> serializer) {

    }

    @Override
    public void registerMeterBandSerializer(final ExperimenterIdMeterSubTypeSerializerKey<MeterBandExperimenterCase> key, final OFSerializer<MeterBandExperimenterCase> serializer) {

    }
}