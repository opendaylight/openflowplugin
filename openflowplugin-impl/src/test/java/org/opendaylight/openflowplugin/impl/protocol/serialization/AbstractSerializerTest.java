/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization;

import org.junit.Before;
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
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;

public abstract class AbstractSerializerTest {
    private SerializerExtensionProvider provider;
    private SerializerRegistry registry;

    @Before
    public void setUp() throws Exception {
        registry = new SerializerRegistryImpl();
        registry.init();
        provider = new SerializerExtensionProviderImpl(registry);
        SerializerInjector.injectSerializers(provider);
        init();
    }

    protected SerializerRegistry getRegistry() {
        return registry;
    }

    protected SerializerExtensionProvider getProvider() {
        return provider;
    }

    protected abstract void init();

    private static class SerializerExtensionProviderImpl implements SerializerExtensionProvider {

        private final SerializerRegistry registry;

        SerializerExtensionProviderImpl(final SerializerRegistry registry) {
            this.registry = registry;
        }

        @Override
        public <K> void registerSerializer(MessageTypeKey<K> messageTypeKey, OFGeneralSerializer ofGeneralSerializer) {
           registry.registerSerializer(messageTypeKey, ofGeneralSerializer);
        }

        @Override
        public boolean unregisterSerializer(ExperimenterSerializerKey key) {
            return false;
        }

        @Override
        public void registerActionSerializer(ActionSerializerKey<?> key, OFGeneralSerializer serializer) {

        }

        @Override
        public void registerInstructionSerializer(InstructionSerializerKey<?> key, OFGeneralSerializer serializer) {

        }

        @Override
        public <C extends OxmClassBase, F extends MatchField> void registerMatchEntrySerializer(MatchEntrySerializerKey<C, F> key, OFGeneralSerializer serializer) {

        }

        @Override
        public void registerExperimenterMessageSerializer(ExperimenterIdSerializerKey<? extends ExperimenterDataOfChoice> key, OFSerializer<? extends ExperimenterDataOfChoice> serializer) {

        }

        @Override
        public void registerMultipartRequestSerializer(ExperimenterIdSerializerKey<? extends ExperimenterDataOfChoice> key, OFSerializer<? extends ExperimenterDataOfChoice> serializer) {

        }

        @Override
        public void registerMultipartRequestTFSerializer(ExperimenterIdSerializerKey<TableFeatureProperties> key, OFGeneralSerializer serializer) {

        }

        @Override
        public void registerMeterBandSerializer(ExperimenterIdSerializerKey<MeterBandExperimenterCase> key, OFSerializer<MeterBandExperimenterCase> serializer) {

        }

        @Override
        public void registerMeterBandSerializer(ExperimenterIdMeterSubTypeSerializerKey<MeterBandExperimenterCase> key, OFSerializer<MeterBandExperimenterCase> serializer) {

        }
    }

}