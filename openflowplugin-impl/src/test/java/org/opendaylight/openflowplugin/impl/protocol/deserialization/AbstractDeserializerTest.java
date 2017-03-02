/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization;

import org.junit.Before;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterActionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterInstructionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.keys.TypeToClassKey;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.property.header.QueueProperty;

public abstract class AbstractDeserializerTest {
    private DeserializerExtensionProvider provider;
    private DeserializerRegistry registry;
    private DeserializationFactory factory;

    @Before
    public void setUp() throws Exception {
        registry = new DeserializerRegistryImpl();
        registry.init();
        factory = new DeserializationFactory();
        factory.setRegistry(registry);
        provider = new DeserializerExtensionProviderImpl(registry, factory);
        DeserializerInjector.injectDeserializers(provider);
        MessageDeserializerInjector.injectLegacyDeserializers(provider);
        init();
    }

    protected DeserializationFactory getFactory() {
        return factory;
    }

    protected DeserializerRegistry getRegistry() {
        return registry;
    }

    protected DeserializerExtensionProvider getProvider() {
        return provider;
    }

    protected abstract void init();

    static class DeserializerExtensionProviderImpl implements DeserializerExtensionProvider {

        private final DeserializerRegistry registry;
        private final DeserializationFactory factory;

        public DeserializerExtensionProviderImpl(final DeserializerRegistry registry, DeserializationFactory factory) {
            this.registry = registry;
            this.factory = factory;
        }

        @Override
        public void registerDeserializer(MessageCodeKey messageCodeKey, OFGeneralDeserializer ofGeneralDeserializer) {
            registry.registerDeserializer(messageCodeKey, ofGeneralDeserializer);
        }

        @Override
        public boolean unregisterDeserializer(ExperimenterDeserializerKey key) {
            return false;
        }

        @Override
        public void registerActionDeserializer(ExperimenterActionDeserializerKey key, OFGeneralDeserializer deserializer) {

        }

        @Override
        public void registerInstructionDeserializer(ExperimenterInstructionDeserializerKey key, OFGeneralDeserializer deserializer) {

        }

        @Override
        public void registerMatchEntryDeserializer(MatchEntryDeserializerKey key, OFGeneralDeserializer deserializer) {

        }

        @Override
        public void registerErrorDeserializer(ExperimenterIdDeserializerKey key, OFDeserializer<ErrorMessage> deserializer) {

        }

        @Override
        public void registerExperimenterMessageDeserializer(ExperimenterIdDeserializerKey key, OFDeserializer<? extends ExperimenterDataOfChoice> deserializer) {

        }

        @Override
        public void registerMultipartReplyMessageDeserializer(ExperimenterIdDeserializerKey key, OFDeserializer<? extends ExperimenterDataOfChoice> deserializer) {

        }

        @Override
        public void registerMultipartReplyTFDeserializer(ExperimenterIdDeserializerKey key, OFGeneralDeserializer deserializer) {

        }

        @Override
        public void registerMeterBandDeserializer(ExperimenterIdDeserializerKey key, OFDeserializer<MeterBandExperimenterCase> deserializer) {

        }

        @Override
        public void registerQueuePropertyDeserializer(ExperimenterIdDeserializerKey key, OFDeserializer<QueueProperty> deserializer) {

        }

        @Override
        public void registerDeserializerMapping(TypeToClassKey typeToClassKey, Class<?> aClass) {
            factory.registerMapping(typeToClassKey, aClass);
        }

        @Override
        public boolean unregisterDeserializerMapping(TypeToClassKey typeToClassKey) {
            return factory.unregisterMapping(typeToClassKey);
        }
    }

}
